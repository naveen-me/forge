// C1.5 — WPE 1080p30 Performance Gate (v5)
//
// Uses SHM buffer fingerprinting with "settling" detection.
// A frame is counted when the fingerprint STABILIZES after a change,
// meaning the web process has finished writing a complete frame.
//
// This avoids double-counting mid-render writes.

#include <glib.h>
#include <glib-object.h>
#include <wpe/webkit.h>
#include <wpe/wpe-platform.h>
#include <wpe/headless/wpe-headless.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cmath>
#include <atomic>
#include <vector>
#include <algorithm>
#include <chrono>
#include <sys/resource.h>
#include <signal.h>

static constexpr int TARGET_W = 1920;
static constexpr int TARGET_H = 1080;
static constexpr int WARMUP_MS = 2000;
static constexpr int MEASURE_MS = 15000;
static constexpr int SETTLE_MS = 16; // wait 16ms for frame to stabilize
static constexpr int POLL_US = 2000; // poll every 2ms
static constexpr int MAX_WAIT_SEC = 30;

static std::atomic<bool> done{false};

static const uint8_t* held_ptr = nullptr;
static gsize held_size = 0;
static int held_w = 0, held_h = 0;

static int64_t process_start_ns = 0;
static int64_t first_content_ns = 0;

// Frame timestamps (ns from process start) — one per settled frame
static std::vector<int64_t> frame_ts;

static double get_cpu_sec() {
    struct rusage ru;
    getrusage(RUSAGE_SELF, &ru);
    return ru.ru_utime.tv_sec + ru.ru_utime.tv_usec / 1e6 +
           ru.ru_stime.tv_sec + ru.ru_stime.tv_usec / 1e6;
}
static long get_rss_kb() {
    struct rusage ru;
    getrusage(RUSAGE_SELF, &ru);
    return ru.ru_maxrss;
}
static int64_t now_ns() {
    return std::chrono::duration_cast<std::chrono::nanoseconds>(
        std::chrono::steady_clock::now().time_since_epoch()).count();
}

static uint32_t fingerprint(const uint8_t* p, gsize sz) {
    uint32_t h = 0;
    gsize step = (sz > 65536) ? sz / 2048 : 1;
    for (gsize i = 0; i < sz; i += step) {
        h = h * 31 + p[i];
    }
    return h;
}

static void on_buffers_changed(WPEView* view, WPEBuffer** buffers, guint n_buffers, gpointer) {
    if (!buffers || n_buffers == 0 || held_ptr) return;

    for (guint i = 0; i < n_buffers; i++) {
        WPEBuffer* buf = buffers[i];
        if (!buf || !WPE_IS_BUFFER_SHM(buf)) continue;

        WPEBufferSHM* shm = WPE_BUFFER_SHM(buf);
        GBytes* data = wpe_buffer_shm_get_data(shm);
        if (!data) continue;

        gsize sz = 0;
        const uint8_t* p = (const uint8_t*)g_bytes_get_data(data, &sz);
        int w = wpe_buffer_get_width(buf);
        int h = wpe_buffer_get_height(buf);

        if (p && sz > 0 && !held_ptr) {
            held_ptr = p;
            held_size = sz;
            held_w = w;
            held_h = h;
            fprintf(stderr, "  Buffer held: %dx%d %zu bytes\n", w, h, sz);
            return;
        }
    }
    for (guint i = 0; i < n_buffers; i++) {
        if (buffers[i]) wpe_view_buffer_rendered(view, buffers[i]);
    }
}

static void on_load_changed(WebKitWebView*, WebKitLoadEvent event, gpointer) {
    if (event == WEBKIT_LOAD_FINISHED)
        fprintf(stderr, "  Page loaded\n");
}

static const char* HTML_1080P =
    "<!DOCTYPE html><html><head><style>"
    "*{margin:0;padding:0;}"
    "body{background:#1a1a2e;overflow:hidden;width:1920px;height:1080px;}"
    "#c{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);"
    "color:#e94560;font-size:120px;font-family:monospace;font-weight:bold;}"
    ".b{position:absolute;width:200px;height:200px;border-radius:50%;}"
    "</style></head><body>"
    "<div id='c'>0</div>"
    "<script>"
    "var F=0;const e=document.getElementById('c');"
    "const balls=[];"
    "for(let i=0;i<20;i++){"
    "  const d=document.createElement('div');"
    "  d.style.background='hsl('+(i*18)+',60%,50%)';"
    "  d.style.opacity='0.3';"
    "  d.style.left=Math.random()*1700+'px';"
    "  d.style.top=Math.random()*880+'px';"
    "  document.body.appendChild(d);"
    "  balls.push({el:d,x:Math.random()*1700,y:Math.random()*880,"
    "  dx:3+Math.random()*5,dy:3+Math.random()*5});}"
    "function t(){F++;e.textContent=F;"
    "e.style.color='hsl('+F%360+',80%,60%)';"
    "for(const b of balls){b.x+=b.dx;b.y+=b.dy;"
    "if(b.x<0||b.x>1700)b.dx=-b.dx;"
    "if(b.y<0||b.y>880)b.dy=-b.dy;"
    "b.el.style.left=b.x+'px';b.el.style.top=b.y+'px';}"
    "requestAnimationFrame(t);}"
    "requestAnimationFrame(t);"
    "</script></body></html>";

int main() {
    process_start_ns = now_ns();
    signal(SIGPIPE, SIG_IGN);

    fprintf(stderr, "=== C1.5 WPE 1080p30 Performance Gate ===\n");
    fprintf(stderr, "Resolution: %dx%d, settle %dms, measure %ds after %ds warmup\n",
            TARGET_W, TARGET_H, SETTLE_MS, MEASURE_MS/1000, WARMUP_MS/1000);

    WPEDisplay* display = wpe_display_headless_new();
    GError* err = nullptr;
    if (!wpe_display_connect(display, &err)) {
        fprintf(stderr, "FAIL: %s\n", err->message); return 1;
    }
    wpe_display_set_primary(display);

    WebKitWebView* wv = WEBKIT_WEB_VIEW(g_object_new(WEBKIT_TYPE_WEB_VIEW, nullptr));
    WPEView* wv_view = webkit_web_view_get_wpe_view(wv);

    g_signal_connect(wv_view, "buffers-changed", G_CALLBACK(on_buffers_changed), nullptr);
    g_signal_connect(wv, "load-changed", G_CALLBACK(on_load_changed), nullptr);

    wpe_view_set_visible(wv_view, TRUE);
    wpe_view_resized(wv_view, TARGET_W, TARGET_H);
    wpe_view_map(wv_view);

    double cpu_start = get_cpu_sec();
    long rss_start = get_rss_kb();

    fprintf(stderr, "  Loading HTML...\n");
    webkit_web_view_load_html(wv, HTML_1080P, "http://c15/");

    // Wait for first buffer
    int64_t wait_start = now_ns();
    while (!held_ptr) {
        g_main_context_iteration(NULL, FALSE);
        if ((now_ns() - wait_start) / 1e9 > MAX_WAIT_SEC) {
            fprintf(stderr, "FAIL: No buffer after %ds\n", MAX_WAIT_SEC);
            g_object_unref(wv);
            return 1;
        }
        usleep(1000);
    }

    int64_t first_buf_ns = now_ns();
    fprintf(stderr, "  First buffer in %.1f ms\n", (first_buf_ns - process_start_ns) / 1e6);

    // Capture initial fingerprint
    uint32_t last_fp = fingerprint(held_ptr, held_size);
    fprintf(stderr, "  Initial fp: 0x%08x\n", last_fp);

    // === MAIN MEASUREMENT LOOP ===
    // Detect frames by: fingerprint changes, then stabilizes for SETTLE_MS
    int64_t measure_start = now_ns();
    int64_t warmup_end = measure_start + WARMUP_MS * 1000000LL;
    int64_t measure_end = measure_start + (WARMUP_MS + MEASURE_MS) * 1000000LL;

    bool changing = false;
    int64_t change_start = 0;
    uint32_t changing_fp = 0;

    fprintf(stderr, "  Measuring...\n");
    while (now_ns() < measure_end && !done) {
        uint32_t fp = fingerprint(held_ptr, held_size);
        int64_t t = now_ns();

        if (fp != changing_fp) {
            // Fingerprint changed from what we were tracking
            changing = true;
            changing_fp = fp;
            change_start = t;
        } else if (changing) {
            // Fingerprint matches what we last saw — is it stable enough?
            int64_t settle_time = t - change_start;
            if (settle_time >= SETTLE_MS * 1000000LL) {
                // Frame fully settled
                if (t >= warmup_end) {
                    frame_ts.push_back(change_start);
                }
                last_fp = changing_fp;
                changing = false;

                if (!first_content_ns) first_content_ns = change_start;
            }
        }

        g_main_context_iteration(NULL, FALSE);
        usleep(POLL_US);
    }

    int64_t actual_end = now_ns();
    double total_sec = (actual_end - process_start_ns) / 1e9;
    double cpu_end = get_cpu_sec();
    long rss_end = get_rss_kb();

    // === ANALYZE ===
    fprintf(stderr, "\n========================================\n");
    fprintf(stderr, "  C1.5 RESULTS\n");
    fprintf(stderr, "========================================\n");
    fprintf(stderr, "\n--- Configuration ---\n");
    fprintf(stderr, "Resolution:       %dx%d\n", TARGET_W, TARGET_H);
    fprintf(stderr, "Total duration:   %.3f s\n", total_sec);
    fprintf(stderr, "Frame timestamps: %zu\n", frame_ts.size());

    if (frame_ts.size() < 10) {
        fprintf(stderr, "\nFAIL: Only %zu settled frames detected\n", frame_ts.size());
        fprintf(stderr, "Possible: settle time too long or page not animating\n");
        g_object_unref(wv);
        return 1;
    }

    // Compute frame intervals from settled frame timestamps
    std::vector<double> intervals_ms;
    for (size_t i = 1; i < frame_ts.size(); i++) {
        double dt = (frame_ts[i] - frame_ts[i-1]) / 1e6;
        if (dt > 1.0 && dt < 500) intervals_ms.push_back(dt);
    }

    if (intervals_ms.size() < 5) {
        fprintf(stderr, "\nFAIL: Not enough intervals (%zu)\n", intervals_ms.size());
        g_object_unref(wv);
        return 1;
    }

    std::sort(intervals_ms.begin(), intervals_ms.end());
    double sum = 0;
    for (double v : intervals_ms) sum += v;
    double mean_ms = sum / intervals_ms.size();
    double fps = 1000.0 / mean_ms;

    double p50 = intervals_ms[(size_t)(intervals_ms.size() * 0.50)];
    double p95 = intervals_ms[(size_t)(intervals_ms.size() * 0.95)];
    double p99 = intervals_ms[(size_t)(intervals_ms.size() * 0.99)];
    double max_ms = intervals_ms.back();
    double min_ms = intervals_ms.front();

    // Count frames that missed the 33.33ms budget (>2x = dropped)
    int dropped = 0;
    for (double v : intervals_ms) if (v > 33.33 * 2) dropped++;

    fprintf(stderr, "\n--- Startup ---\n");
    fprintf(stderr, "Buffer ready:     %.1f ms\n", (first_buf_ns - process_start_ns) / 1e6);
    if (first_content_ns)
        fprintf(stderr, "Content ready:    %.1f ms (after buffer)\n",
                (first_content_ns - first_buf_ns) / 1e6);

    fprintf(stderr, "\n--- Steady-State Performance ---\n");
    fprintf(stderr, "Settled frames:   %zu\n", intervals_ms.size());
    fprintf(stderr, "Average FPS:      %.2f\n", fps);
    fprintf(stderr, "Mean frame time:  %.2f ms\n", mean_ms);
    fprintf(stderr, "Min frame time:   %.2f ms\n", min_ms);
    fprintf(stderr, "Max frame time:   %.2f ms\n", max_ms);
    fprintf(stderr, "P50 frame time:   %.2f ms\n", p50);
    fprintf(stderr, "P95 frame time:   %.2f ms\n", p95);
    fprintf(stderr, "P99 frame time:   %.2f ms\n", p99);
    fprintf(stderr, "Dropped (>67ms):  %d / %zu (%.1f%%)\n",
            dropped, intervals_ms.size(), 100.0 * dropped / intervals_ms.size());

    fprintf(stderr, "\n--- Resources ---\n");
    fprintf(stderr, "CPU time:         %.3f s\n", cpu_end - cpu_start);
    fprintf(stderr, "Peak RSS:         %ld KB (%.1f MB)\n", rss_end, rss_end / 1024.0);
    fprintf(stderr, "CPU per frame:    %.3f ms\n",
            (cpu_end - cpu_start) * 1000.0 / intervals_ms.size());
    fprintf(stderr, "Buffer:           %dx%d SHM RGBA8888\n", held_w, held_h);

    fprintf(stderr, "\n--- Hardware ---\n");
    FILE* f = fopen("/proc/cpuinfo", "r");
    if (f) {
        char line[256];
        while (fgets(line, sizeof(line), f)) {
            if (strstr(line, "model name")) {
                line[strcspn(line, "\n")] = 0;
                fprintf(stderr, "CPU: %s\n", strchr(line, ':') + 2);
                break;
            }
        }
        fclose(f);
    }
    f = fopen("/proc/meminfo", "r");
    if (f) {
        char line[256];
        while (fgets(line, sizeof(line), f)) {
            if (strstr(line, "MemTotal")) {
                line[strcspn(line, "\n")] = 0;
                fprintf(stderr, "RAM: %s\n", strchr(line, ':') + 1);
                break;
            }
        }
        fclose(f);
    }

    fprintf(stderr, "\n========================================\n");
    bool fps_ok = fps >= 30.0;
    bool drop_ok = (100.0 * dropped / intervals_ms.size()) < 2.0;
    bool p95_ok = p95 < 33.33;
    bool verdict = fps_ok && drop_ok && p95_ok;

    fprintf(stderr, "  C1.5 VERDICT: %s\n", verdict ? "PASS" : "FAIL");
    fprintf(stderr, "  FPS >= 30:      %s (%.2f)\n", fps_ok ? "YES" : "NO", fps);
    fprintf(stderr, "  Drops < 2%%:     %s (%.1f%%)\n", drop_ok ? "YES" : "NO",
            100.0 * dropped / intervals_ms.size());
    fprintf(stderr, "  P95 < 33.3ms:   %s (%.2f ms)\n", p95_ok ? "YES" : "NO", p95);
    fprintf(stderr, "========================================\n");

    g_object_unref(wv);
    return verdict ? 0 : 1;
}
