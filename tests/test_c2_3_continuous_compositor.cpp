// C2.3.1 — FIX GPAC DYNAMIC RAW FRAME REFRESH
//
// Verification for GPAC Compositor Dynamic Frame Refresh using real-time
// zero-buffer configuration (`compositor:drv=no:opfmt=rgba:osize=1920x1080:fps=30:buffer=0:rbuffer=0`)
//
// STEP 1 — Explicit source timing (Timescale=90000, CTS=N*3000, duration=3000)
// STEP 2 — Fresh packet memory with unique solid RGBA value per frame (10, 20, 30...)
// STEP 3 — Instrument both SOURCE and SINK sides logging frame_id, CTS, first_pixel
// STEP 4/5 — Configure zero-rebuffer playout so compositor updates textures dynamically without initial buffering delay

#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <vector>
#include <atomic>
#include <chrono>
#include <thread>
#include <set>
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
static constexpr int TARGET_FRAMES = 60; // 60 frames
static constexpr u32 TIMESCALE = 90000;
static constexpr u32 FRAME_DUR = TIMESCALE / TARGET_FPS; // 3000 ts units per frame

// ============================================================
// Shared Instrumentation State
// ============================================================
struct SinkRecord {
    int frame_id;
    u64 cts;
    uint8_t first_pixel;
};

struct SharedStats {
    std::atomic<int> src_frames_injected{0};
    std::atomic<int> sink_frames_received{0};
    std::atomic<int64_t> src_total_inject_ns{0};
    std::atomic<int64_t> sink_total_process_ns{0};
    std::vector<SinkRecord> sink_log;
};

static SharedStats g_stats;

static int64_t now_ns() {
    return std::chrono::duration_cast<std::chrono::nanoseconds>(
        std::chrono::steady_clock::now().time_since_epoch()).count();
}

// Custom SINK configure callback
static GF_Err custom_sink_configure_pid(GF_Filter* filter, GF_FilterPid* pid, Bool is_remove) {
    return GF_OK;
}

// Custom SINK filter callback
static GF_Err custom_sink_process(GF_Filter* filter) {
    u32 num_ipids = gf_filter_get_ipid_count(filter);
    for (u32 i = 0; i < num_ipids; i++) {
        GF_FilterPid* pid = gf_filter_get_ipid(filter, i);
        if (!pid) continue;

        GF_FilterPacket* pck = gf_filter_pid_get_packet(pid);
        if (!pck) continue;

        auto t0 = now_ns();

        u32 size = 0;
        const u8* data = gf_filter_pck_get_data(pck, &size);

        if (data && size >= W * H * 4) {
            int sink_frame_id = g_stats.sink_frames_received.fetch_add(1, std::memory_order_relaxed);
            u64 cts = gf_filter_pck_get_cts(pck);
            uint8_t first_pixel = data[0];

            fprintf(stderr, "SINK frame_id=%2d CTS=%llu first_pixel=%3d\n",
                    sink_frame_id, (unsigned long long)cts, first_pixel);

            g_stats.sink_log.push_back({sink_frame_id, cts, first_pixel});

            auto t1 = now_ns();
            g_stats.sink_total_process_ns.fetch_add(t1 - t0, std::memory_order_relaxed);
        }

        gf_filter_pid_drop_packet(pid);
    }
    return GF_OK;
}

// Custom SOURCE process callback
static GF_Err custom_src_process(GF_Filter* filter) {
    GF_FilterPid* pid = gf_filter_get_opid(filter, 0);
    if (!pid) return GF_OK;

    int frame_num = g_stats.src_frames_injected.load(std::memory_order_relaxed);
    if (frame_num >= TARGET_FRAMES) {
        gf_filter_pid_set_eos(pid);
        return GF_EOS;
    }

    auto t0_inj = now_ns();

    u8* data = nullptr;
    // Allocate fresh GPAC packet memory for EVERY frame
    GF_FilterPacket* pck = gf_filter_pck_new_alloc(pid, W * H * 4, &data);
    if (pck && data) {
        // Unique solid RGBA value per frame (10, 20, 30, ... 210)
        uint8_t color_val = static_cast<uint8_t>((frame_num * 10 + 10) % 256);
        std::memset(data, color_val, W * H * 4);

        // Explicit monotonic timing
        u64 cts = static_cast<u64>(frame_num) * FRAME_DUR; // 0, 3000, 6000...
        gf_filter_pck_set_cts(pck, cts);
        gf_filter_pck_set_dts(pck, cts);
        gf_filter_pck_set_duration(pck, FRAME_DUR);
        gf_filter_pck_send(pck);

        auto t1_inj = now_ns();
        g_stats.src_total_inject_ns.fetch_add(t1_inj - t0_inj, std::memory_order_relaxed);
        g_stats.src_frames_injected.fetch_add(1, std::memory_order_relaxed);

        fprintf(stderr, "SOURCE frame_id=%2d CTS=%llu first_pixel=%3d\n",
                frame_num, (unsigned long long)cts, color_val);
    }

    // Schedule next frame in 33,000 microseconds (~30 FPS pacing)
    gf_filter_ask_rt_reschedule(filter, 33000);
    return GF_OK;
}

int main(int argc, char** argv) {
    int64_t wall_start = now_ns();

    fprintf(stderr, "================================================\n");
    fprintf(stderr, " C2.3.1 — GPAC Dynamic Raw Frame Refresh Test\n");
    fprintf(stderr, "================================================\n");

    gf_sys_init(GF_MemTrackerNone, NULL);

    GF_FilterSession* fs = gf_fs_new_defaults(GF_FS_FLAG_NON_BLOCKING);
    if (!fs) {
        fprintf(stderr, "FAIL: Could not create GPAC session\n");
        return 1;
    }

    GF_Err err = GF_OK;

    // Source filter
    GF_Filter* src = gf_fs_new_filter(fs, "c23src", GF_FS_REG_MAIN_THREAD, &err);
    gf_filter_set_process_ckb(src, custom_src_process);

    GF_PropertyValue pv;
    pv.type = GF_PROP_UINT;
    pv.value.uint = GF_STREAM_VISUAL;
    gf_filter_push_caps(src, GF_PROP_PID_STREAM_TYPE, &pv, NULL, GF_CAPS_OUTPUT, 0);
    pv.value.uint = GF_CODECID_RAW;
    gf_filter_push_caps(src, GF_PROP_PID_CODECID, &pv, NULL, GF_CAPS_OUTPUT, 0);
    pv.value.uint = GF_PIXEL_RGBA;
    gf_filter_push_caps(src, GF_PROP_PID_PIXFMT, &pv, NULL, GF_CAPS_OUTPUT, 0);

    GF_FilterPid* src_pid = gf_filter_pid_new(src);
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
    pv.value.uint = TIMESCALE;
    gf_filter_pid_set_property(src_pid, GF_PROP_PID_TIMESCALE, &pv);

    GF_Fraction fps_frac = {TARGET_FPS, 1};
    pv.type = GF_PROP_FRACTION;
    pv.value.frac = fps_frac;
    gf_filter_pid_set_property(src_pid, GF_PROP_PID_FPS, &pv);

    // Compositor filter configured with zero buffering (buffer=0:rbuffer=0) for real-time dynamic texture refresh
    const char* comp_args = (argc > 1) ? argv[1] : "compositor:drv=no:opfmt=rgba:osize=1920x1080:fps=30:buffer=0:rbuffer=0";
    fprintf(stderr, "Loading compositor: %s\n", comp_args);
    GF_Filter* comp = gf_fs_load_filter(fs, comp_args, &err);

    // Sink filter
    GF_Filter* sink = gf_fs_new_filter(fs, "c23sink", GF_FS_REG_MAIN_THREAD, &err);
    gf_filter_set_process_ckb(sink, custom_sink_process);
    gf_filter_set_configure_ckb(sink, custom_sink_configure_pid);
    gf_filter_act_as_sink(sink);

    pv.type = GF_PROP_UINT;
    pv.value.uint = GF_STREAM_VISUAL;
    gf_filter_push_caps(sink, GF_PROP_PID_STREAM_TYPE, &pv, NULL, GF_CAPS_INPUT, 0);
    pv.value.uint = GF_CODECID_RAW;
    gf_filter_push_caps(sink, GF_PROP_PID_CODECID, &pv, NULL, GF_CAPS_INPUT, 0);
    pv.type = GF_PROP_UINT;
    pv.value.uint = GF_PIXEL_RGBA;
    gf_filter_push_caps(sink, GF_PROP_PID_PIXFMT, &pv, NULL, GF_CAPS_INPUT, 0);

    // Connect graph
    gf_filter_set_source(comp, src, NULL);
    gf_filter_set_source(sink, comp, NULL);

    gf_filter_post_process_task(src);

    int64_t session_start = now_ns();
    int timeout_ms = 10000;
    int64_t deadline_ns = session_start + static_cast<int64_t>(timeout_ms) * 1000000LL;

    while (g_stats.sink_frames_received.load(std::memory_order_relaxed) < TARGET_FRAMES) {
        gf_fs_run(fs);

        if (now_ns() > deadline_ns) {
            fprintf(stderr, "Timeout after %d ms!\n", timeout_ms);
            break;
        }

        usleep(10000);
    }

    int64_t session_end = now_ns();
    double session_ms = (session_end - session_start) / 1e6;

    gf_fs_del(fs);
    gf_sys_close();

    // Analyze results
    int sent = g_stats.src_frames_injected.load();
    int rec = g_stats.sink_frames_received.load();

    std::set<uint8_t> unique_pixels;
    int dup_count = 0;
    uint8_t last_px = 255;

    for (size_t i = 0; i < g_stats.sink_log.size(); i++) {
        uint8_t px = g_stats.sink_log[i].first_pixel;
        unique_pixels.insert(px);
        if (i > 0 && px == last_px) dup_count++;
        last_px = px;
    }

    double achieved_fps = (rec > 1 && session_ms > 0) ? (rec - 1) * 1000.0 / session_ms : 0;
    int dropped = (sent > rec) ? (sent - rec) : 0;

    struct rusage ru;
    getrusage(RUSAGE_SELF, &ru);
    long rss_kb = ru.ru_maxrss;
    double cpu_sec = ru.ru_utime.tv_sec + ru.ru_utime.tv_usec / 1e6
                   + ru.ru_stime.tv_sec + ru.ru_stime.tv_usec / 1e6;

    fprintf(stderr, "\n================================================\n");
    fprintf(stderr, " C2.3.1 FINAL REPORT\n");
    fprintf(stderr, "================================================\n");
    fprintf(stderr, " frames_sent:                     %d\n", sent);
    fprintf(stderr, " frames_received:                 %d\n", rec);
    fprintf(stderr, " unique_pixel_values_received:    %zu\n", unique_pixels.size());
    fprintf(stderr, " duplicate_count:                 %d\n", dup_count);
    fprintf(stderr, " FPS:                             %.1f\n", achieved_fps);
    fprintf(stderr, " dropped_frames:                  %d\n", dropped);
    fprintf(stderr, " CPU:                             %.3f s\n", cpu_sec);
    fprintf(stderr, " RSS:                             %ld KB (%.1f MB)\n", rss_kb, rss_kb / 1024.0);

    bool pass = (rec >= 60) && (unique_pixels.size() >= 10);
    fprintf(stderr, "\n================================================\n");
    fprintf(stderr, " C2.3.1 %s\n", pass ? "PASS" : "FAIL");
    fprintf(stderr, "================================================\n\n");

    return pass ? 0 : 1;
}
