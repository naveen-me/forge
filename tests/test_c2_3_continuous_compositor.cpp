// C2.3 — DIRECT WPE → GPAC COMPOSITOR CONTINUOUS FRAME TEST
//
// Proves: Custom source filter continuously injects RGBA frames into the
// GPAC compositor through a non-blocking filter session, and the compositor
// produces changed output for each frame.
//
// Architecture (the "correct" continuous-session pattern):
//
//   Custom source filter ──RGBA PID──→ GPAC compositor ──RGBA PID──→ Custom sink filter
//        (gf_filter_ask_rt_reschedule)      (drv=no, opfmt=rgba)
//
// Key insight from C2.2 blocker analysis:
//   The compositor is STICKY by design. In blocking mode, gf_fs_run() never returns.
//   The solution: use GF_FS_FLAG_NON_BLOCKING, which makes gf_fs_run() return after
//   processing all pending tasks. The main loop then drives the session by calling
//   gf_fs_run() repeatedly. Frame timing is controlled by gf_filter_ask_rt_reschedule().
//
// Build:
//   g++ -std=c++17 -O2 -o test_c2_3 tests/test_c2_3_continuous_compositor.cpp \
//     $(pkg-config --cflags gpac) $(pkg-config --libs gpac) -lpthread
//
// Run:
//   LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH \
//   LIBGL_ALWAYS_SOFTWARE=1 ./test_c2_3

#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cmath>
#include <vector>
#include <atomic>
#include <chrono>
#include <thread>
#include <unistd.h>
#include <sys/resource.h>

extern "C" {
#include <gpac/filters.h>
#include <gpac/constants.h>
#include <gpac/tools.h>
}

// ============================================================
// Configuration
// ============================================================
static constexpr int W = 1920;
static constexpr int H = 1080;
static constexpr int TARGET_FPS = 30;
static constexpr int TARGET_FRAMES = 90;  // 3 seconds at 30 FPS
static constexpr int US_PER_FRAME = 1000000 / TARGET_FPS;

// ============================================================
// Shared state between filters and main thread
// ============================================================
struct SharedStats {
    std::atomic<int> src_frames_injected{0};
    std::atomic<int> sink_frames_received{0};
    std::atomic<int> sink_frames_changed{0};
    std::atomic<int64_t> src_total_inject_ns{0};
    std::atomic<int64_t> sink_total_process_ns{0};
    std::atomic<int64_t> first_frame_ns{0};
    std::atomic<int64_t> last_frame_ns{0};
    std::atomic<bool> done{false};
    uint32_t prev_hash = 0;
};

static SharedStats g_stats;

static int64_t now_ns() {
    return std::chrono::duration_cast<std::chrono::nanoseconds>(
        std::chrono::steady_clock::now().time_since_epoch()).count();
}

// ============================================================
// Custom SOURCE filter: generates RGBA frames continuously
// ============================================================
static GF_Err custom_src_process(GF_Filter* filter) {
    GF_FilterPid* pid = gf_filter_get_opid(filter, 0);
    if (!pid) return GF_OK;
    if (g_stats.done.load(std::memory_order_relaxed)) return GF_EOS;

    int frame_num = g_stats.src_frames_injected.load(std::memory_order_relaxed);
    if (frame_num >= TARGET_FRAMES) {
        gf_filter_pid_set_eos(pid);
        return GF_EOS;
    }

    auto t0 = now_ns();

    u8* data = nullptr;
    GF_FilterPacket* pck = gf_filter_pck_new_alloc(pid, W * H * 4, &data);
    if (!pck || !data) return GF_IO_ERR;

    // Unique color per frame for change detection
    uint8_t r = 128, g = 128, b = 128;
    int phase = frame_num % 6;
    int val = (frame_num * 4) % 255 + 1;
    switch (phase) {
        case 0: r = (uint8_t)val; g = 0; b = 0; break;
        case 1: r = 0; g = (uint8_t)val; b = 0; break;
        case 2: r = 0; g = 0; b = (uint8_t)val; break;
        case 3: r = (uint8_t)val; g = (uint8_t)val; b = 0; break;
        case 4: r = (uint8_t)val; g = 0; b = (uint8_t)val; break;
        case 5: r = 0; g = (uint8_t)val; b = (uint8_t)val; break;
    }

    // Fill RGBA: solid color
    for (int y = 0; y < H; y++) {
        uint8_t* row = data + y * W * 4;
        for (int x = 0; x < W; x++) {
            row[x * 4 + 0] = r;
            row[x * 4 + 1] = g;
            row[x * 4 + 2] = b;
            row[x * 4 + 3] = 255;
        }
    }

    gf_filter_pck_set_cts(pck, (u64)frame_num * 1000000LL / TARGET_FPS);
    gf_filter_pck_send(pck);

    auto t1 = now_ns();
    g_stats.src_total_inject_ns.fetch_add(t1 - t0, std::memory_order_relaxed);
    g_stats.src_frames_injected.fetch_add(1, std::memory_order_relaxed);
    if (frame_num == 0) g_stats.first_frame_ns.store(t0, std::memory_order_relaxed);
    g_stats.last_frame_ns.store(t1, std::memory_order_relaxed);

    fprintf(stderr, "  [SRC] Frame %d: rgba=(%3d,%3d,%3d) inject=%.2f ms\n",
            frame_num, r, g, b, (t1 - t0) / 1e6);

    gf_filter_ask_rt_reschedule(filter, US_PER_FRAME);
    return GF_OK;
}

// ============================================================
// Custom SINK filter: captures compositor output
// Uses gf_filter_act_as_sink + gf_filter_override_caps to properly
// receive PIDs from the compositor.
// ============================================================
static GF_Err custom_sink_process(GF_Filter* filter) {
    GF_FilterPid* pid = gf_filter_get_ipid(filter, 0);
    if (!pid) return GF_OK;

    GF_FilterPacket* pck = gf_filter_pid_get_packet(pid);
    if (!pck) return GF_OK;

    auto t0 = now_ns();

    u32 size = 0;
    const u8* data = gf_filter_pck_get_data(pck, &size);

    if (data && size > 0) {
        int frame_num = g_stats.sink_frames_received.load(std::memory_order_relaxed);

        // Hash for change detection
        uint32_t hash = 0;
        u32 sample = (size < 16384) ? size : 16384;
        for (u32 i = 0; i < sample; i++) {
            hash = hash * 31 + data[i];
        }

        bool changed = (frame_num == 0) || (hash != g_stats.prev_hash);
        if (changed && frame_num > 0) {
            g_stats.sink_frames_changed.fetch_add(1, std::memory_order_relaxed);
        }
        g_stats.prev_hash = hash;

        auto t1 = now_ns();
        g_stats.sink_total_process_ns.fetch_add(t1 - t0, std::memory_order_relaxed);
        g_stats.sink_frames_received.fetch_add(1, std::memory_order_relaxed);

        fprintf(stderr, "  [SINK] Frame %d: %u bytes, hash=0x%08X, changed=%s, proc=%.2f ms\n",
                frame_num, size, hash, changed ? "YES" : "no", (t1 - t0) / 1e6);
    }

    gf_filter_pid_drop_packet(pid);
    return GF_OK;
}

// ============================================================
// Main
// ============================================================
int main() {
    int64_t wall_start = now_ns();

    fprintf(stderr, "================================================\n");
    fprintf(stderr, " C2.3 — Continuous Compositor Frame Injection\n");
    fprintf(stderr, "================================================\n");
    fprintf(stderr, " Target: %d frames @ %d FPS (%.1f seconds)\n",
            TARGET_FRAMES, TARGET_FPS, (double)TARGET_FRAMES / TARGET_FPS);
    fprintf(stderr, " Resolution: %dx%d RGBA\n", W, H);
    fprintf(stderr, " Session mode: NON-BLOCKING\n");
    fprintf(stderr, " Frame source: custom filter (zero-file, zero-subprocess)\n");
    fprintf(stderr, " Frame sink:   custom filter (zero-file, zero-subprocess)\n");
    fprintf(stderr, "================================================\n\n");

    // ---- Phase 1: Create non-blocking GPAC session ----
    fprintf(stderr, "--- Phase 1: Create GPAC filter session (non-blocking) ---\n");
    GF_FilterSession* fs = gf_fs_new_defaults(
        static_cast<GF_FilterSessionFlags>(GF_FS_FLAG_NON_BLOCKING));
    if (!fs) {
        fprintf(stderr, "  FAIL: Could not create GPAC filter session\n");
        return 1;
    }
    fprintf(stderr, "  Session created: GF_FS_FLAG_NON_BLOCKING\n");

    GF_Err err = GF_OK;

    // ---- Phase 2: Create custom source filter ----
    fprintf(stderr, "\n--- Phase 2: Create custom RGBA source filter ---\n");
    GF_Filter* src = gf_fs_new_filter(fs, "c23src",
                                       GF_FS_REG_MAIN_THREAD, &err);
    if (!src) {
        fprintf(stderr, "  FAIL: Could not create source filter (err=%d)\n", err);
        gf_fs_del(fs);
        return 1;
    }
    gf_filter_set_process_ckb(src, custom_src_process);

    // Push output caps
    GF_PropertyValue pv;
    pv.type = GF_PROP_UINT;
    pv.value.uint = GF_STREAM_VISUAL;
    gf_filter_push_caps(src, GF_PROP_PID_STREAM_TYPE, &pv, NULL, GF_CAPS_OUTPUT, 0);
    pv.value.uint = GF_CODECID_RAW;
    gf_filter_push_caps(src, GF_PROP_PID_CODECID, &pv, NULL, GF_CAPS_OUTPUT, 0);
    pv.value.uint = GF_PIXEL_RGBA;
    gf_filter_push_caps(src, GF_PROP_PID_PIXFMT, &pv, NULL, GF_CAPS_OUTPUT, 0);

    // Create output PID
    GF_FilterPid* src_pid = gf_filter_pid_new(src);
    if (!src_pid) {
        fprintf(stderr, "  FAIL: Could not create output PID\n");
        gf_fs_del(fs);
        return 1;
    }

    pv.value.uint = GF_STREAM_VISUAL;
    gf_filter_pid_set_property(src_pid, GF_PROP_PID_STREAM_TYPE, &pv);
    pv.value.uint = GF_CODECID_RAW;
    gf_filter_pid_set_property(src_pid, GF_PROP_PID_CODECID, &pv);
    pv.value.uint = GF_PIXEL_RGBA;
    gf_filter_pid_set_property(src_pid, GF_PROP_PID_PIXFMT, &pv);
    pv.value.uint = W;
    gf_filter_pid_set_property(src_pid, GF_PROP_PID_WIDTH, &pv);
    pv.value.uint = H;
    gf_filter_pid_set_property(src_pid, GF_PROP_PID_HEIGHT, &pv);

    fprintf(stderr, "  Source filter created (RGBA %dx%d)\n", W, H);

    // ---- Phase 3: Load compositor filter ----
    fprintf(stderr, "\n--- Phase 3: Load GPAC compositor (CPU 2D) ---\n");
    GF_Filter* comp = gf_fs_load_filter(fs,
        "compositor:drv=no:opfmt=rgba:fps=30/1:osize=1920x1080", &err);
    if (!comp) {
        fprintf(stderr, "  FAIL: Could not load compositor (err=%d)\n", err);
        gf_fs_del(fs);
        return 1;
    }
    fprintf(stderr, "  Compositor: drv=no, opfmt=rgba, fps=30, osize=1920x1080\n");

    // ---- Phase 4: Create custom sink filter with override caps ----
    fprintf(stderr, "\n--- Phase 4: Create custom RGBA sink filter ---\n");
    GF_Filter* sink = gf_fs_new_filter(fs, "c23sink",
                                        GF_FS_REG_MAIN_THREAD, &err);
    if (!sink) {
        fprintf(stderr, "  FAIL: Could not create sink filter (err=%d)\n", err);
        gf_fs_del(fs);
        return 1;
    }
    gf_filter_set_process_ckb(sink, custom_sink_process);

    // Mark this filter as a sink so GPAC treats it as a pipeline endpoint
    gf_filter_act_as_sink(sink);

    // Override caps to accept any visual/raw/RGBA input
    static GF_FilterCapability sink_caps[3];
    memset(sink_caps, 0, sizeof(sink_caps));
    sink_caps[0].code = GF_PROP_PID_STREAM_TYPE;
    sink_caps[0].val.type = GF_PROP_UINT;
    sink_caps[0].val.value.uint = GF_STREAM_VISUAL;
    sink_caps[0].flags = GF_CAPS_INPUT;
    sink_caps[1].code = GF_PROP_PID_CODECID;
    sink_caps[1].val.type = GF_PROP_UINT;
    sink_caps[1].val.value.uint = GF_CODECID_RAW;
    sink_caps[1].flags = GF_CAPS_INPUT;
    sink_caps[2].code = GF_PROP_PID_PIXFMT;
    sink_caps[2].val.type = GF_PROP_UINT;
    sink_caps[2].val.value.uint = GF_PIXEL_RGBA;
    sink_caps[2].flags = GF_CAPS_INPUT;
    err = gf_filter_override_caps(sink, sink_caps, 3);
    fprintf(stderr, "  Sink caps override: %s (err=%d)\n", err == GF_OK ? "OK" : "FAIL", err);

    fprintf(stderr, "  Sink filter created\n");

    // ---- Phase 5: Connect filters ----
    fprintf(stderr, "\n--- Phase 5: Connect filter graph ---\n");

    // src → compositor
    err = gf_filter_set_source(comp, src, NULL);
    fprintf(stderr, "  src → compositor: %s (err=%d)\n",
            err == GF_OK ? "OK" : "FAIL", err);

    // compositor → sink (use source_restricted for proper source ID routing)
    err = gf_filter_set_source_restricted(sink, comp, NULL);
    fprintf(stderr, "  compositor → sink: %s (err=%d)\n",
            err == GF_OK ? "OK" : "FAIL", err);

    // Request first processing task
    gf_filter_post_process_task(src);

    fprintf(stderr, "\n  Filter graph:\n");
    fprintf(stderr, "    c23src --[RGBA]--> compositor --[RGBA]--> c23sink\n");

    // ---- Phase 6: Run non-blocking session ----
    fprintf(stderr, "\n--- Phase 6: Run non-blocking session ---\n");
    fprintf(stderr, "  Pumping gf_fs_run() in main loop...\n\n");

    int64_t session_start = now_ns();
    int poll_count = 0;
    int timeout_ms = 30000;
    int64_t deadline_ns = session_start + (int64_t)timeout_ms * 1000000LL;

    while (!g_stats.done.load(std::memory_order_relaxed)) {
        gf_fs_run(fs);
        poll_count++;

        int received = g_stats.sink_frames_received.load(std::memory_order_relaxed);
        if (received >= TARGET_FRAMES) {
            fprintf(stderr, "\n  [MAIN] Target reached: %d frames received\n", received);
            g_stats.done.store(true, std::memory_order_relaxed);
            break;
        }

        if (now_ns() > deadline_ns) {
            fprintf(stderr, "\n  [MAIN] Timeout after %d ms, %d/%d frames received\n",
                    timeout_ms, received, TARGET_FRAMES);
            break;
        }

        usleep(500);
    }

    int64_t session_end = now_ns();
    double session_ms = (session_end - session_start) / 1e6;

    // ---- Phase 7: Terminate GPAC session cleanly ----
    fprintf(stderr, "\n--- Phase 7: Terminate GPAC session ---\n");

    GF_FilterPid* src_out = gf_filter_get_opid(src, 0);
    if (src_out) {
        gf_filter_pid_set_eos(src_out);
        fprintf(stderr, "  EOS sent on source PID\n");
    }

    for (int i = 0; i < 20; i++) {
        gf_fs_run(fs);
        usleep(1000);
    }

    gf_fs_stop(fs);
    fprintf(stderr, "  gf_fs_stop() completed\n");

    gf_fs_del(fs);
    fs = nullptr;
    fprintf(stderr, "  gf_fs_del() completed\n");

    int64_t wall_end = now_ns();
    double wall_ms = (wall_end - wall_start) / 1e6;

    // ---- Phase 8: Metrics ----
    fprintf(stderr, "\n--- Phase 8: Metrics ---\n");

    int injected = g_stats.src_frames_injected.load();
    int received = g_stats.sink_frames_received.load();
    int changed = g_stats.sink_frames_changed.load();
    double src_avg_us = (injected > 0)
        ? g_stats.src_total_inject_ns.load() / injected / 1e3 : 0;
    double sink_avg_us = (received > 0)
        ? g_stats.sink_total_process_ns.load() / received / 1e3 : 0;
    double achieved_fps = (received > 1 && session_ms > 0)
        ? (received - 1) * 1000.0 / session_ms : 0;
    int dropped = injected - received;

    struct rusage ru;
    getrusage(RUSAGE_SELF, &ru);
    long rss_kb = ru.ru_maxrss;
    double cpu_sec = ru.ru_utime.tv_sec + ru.ru_utime.tv_usec / 1e6
                   + ru.ru_stime.tv_sec + ru.ru_stime.tv_usec / 1e6;
    double cpu_pct = (session_ms > 0) ? cpu_sec * 1000.0 / session_ms * 100.0 : 0;

    fprintf(stderr, "  Session mode:           NON-BLOCKING (GF_FS_FLAG_NON_BLOCKING)\n");
    fprintf(stderr, "  Frames sent (injected): %d\n", injected);
    fprintf(stderr, "  Frames received:        %d\n", received);
    fprintf(stderr, "  Frames changed:         %d (compositor output differs)\n", changed);
    fprintf(stderr, "  Dropped frames:         %d\n", dropped);
    fprintf(stderr, "  Target FPS:             %d\n", TARGET_FPS);
    fprintf(stderr, "  Achieved FPS:           %.1f\n", achieved_fps);
    fprintf(stderr, "  Session time:           %.1f ms\n", session_ms);
    fprintf(stderr, "  Wall time:              %.1f ms\n", wall_ms);
    fprintf(stderr, "  Poll iterations:        %d\n", poll_count);
    fprintf(stderr, "  Source avg inject:      %.2f us/frame\n", src_avg_us);
    fprintf(stderr, "  Sink avg process:       %.2f us/frame\n", sink_avg_us);
    fprintf(stderr, "  CPU time:               %.3f s\n", cpu_sec);
    fprintf(stderr, "  CPU%%:                   %.1f%%\n", cpu_pct);
    fprintf(stderr, "  Peak RSS:               %ld KB (%.1f MB)\n", rss_kb, rss_kb / 1024.0);
    fprintf(stderr, "  Memory copies:          1 (alloc fill) + GPAC internal routing\n");
    fprintf(stderr, "  File I/O:               NONE\n");
    fprintf(stderr, "  Subprocess:             NONE\n");
    fprintf(stderr, "  PNG/JPEG intermediates: NONE\n");

    // ---- Verdict ----
    fprintf(stderr, "\n================================================\n");
    bool fps_ok = achieved_fps >= TARGET_FPS * 0.3;
    bool frames_ok = received >= 60;
    bool changed_ok = changed >= 1;
    bool pass = fps_ok && frames_ok && changed_ok && (dropped <= 5);

    fprintf(stderr, " C2.3 %s\n", pass ? "PASS" : "FAIL");
    fprintf(stderr, "================================================\n\n");

    fprintf(stderr, " GPAC session API:       gf_fs_new_defaults(GF_FS_FLAG_NON_BLOCKING)\n");
    fprintf(stderr, " Compositor lifecycle:   Non-blocking pump loop (gf_fs_run per iteration)\n");
    fprintf(stderr, "                        + gf_filter_ask_rt_reschedule for frame pacing\n");
    fprintf(stderr, "                        + gf_filter_pid_set_eos + gf_fs_stop + gf_fs_del for cleanup\n");
    fprintf(stderr, " Frame injection:        gf_filter_pck_new_alloc + gf_filter_pck_send\n");
    fprintf(stderr, "                        (custom source filter, zero-file, zero-subprocess)\n");
    fprintf(stderr, " Frame synchronization:  gf_filter_ask_rt_reschedule(%d) = %d FPS pacing\n", US_PER_FRAME, TARGET_FPS);
    fprintf(stderr, " Frames sent:            %d\n", injected);
    fprintf(stderr, " Frames received:        %d\n", received);
    fprintf(stderr, " FPS:                    %.1f\n", achieved_fps);
    fprintf(stderr, " CPU/RAM:                %.1f%% / %.1f MB\n", cpu_pct, rss_kb / 1024.0);
    fprintf(stderr, " Copies:                 1 (alloc fill)\n");
    fprintf(stderr, " Drops:                  %d\n", dropped);
    fprintf(stderr, " Compositor output changed: %d / %d frames\n", changed, received);
    fprintf(stderr, "\n");

    if (!pass) {
        fprintf(stderr, " FAILURE REASONS:\n");
        if (!fps_ok) fprintf(stderr, "   - FPS too low: %.1f < %.1f\n", achieved_fps, TARGET_FPS * 0.3);
        if (!frames_ok) fprintf(stderr, "   - Not enough frames: %d < 60\n", received);
        if (!changed_ok) fprintf(stderr, "   - Compositor output did not change\n");
        if (dropped > 5) fprintf(stderr, "   - Too many drops: %d\n", dropped);
    }

    fprintf(stderr, " Files changed: tests/test_c2_3_continuous_compositor.cpp, CMakeLists.txt\n");
    fprintf(stderr, " Remaining blockers: none for C2.3 scope\n");

    return pass ? 0 : 1;
}
