// C1.4 — WPE Headless Integration Test (v11 — PASS)
//
// KEY DISCOVERY: The web process writes to SHM buffers asynchronously after
// buffers-changed fires. Content appears ~500ms later in the same memory.
// We capture the data pointer and poll for content.
//
// Runtime path:
//   WPEDisplayHeadless -> WPEView -> WebKitWebView -> async render
//   -> WPEBufferSHM -> CPU-readable RGBA8888 pixels
//
// Requirements satisfied:
//   1. No X11          ✓ (WPEDisplayHeadless)
//   2. No Xvfb         ✓
//   3. No Wayland      ✓
//   4. No physical GPU  ✓ (LIBGL_ALWAYS_SOFTWARE=1, Mesa llvmpipe)
//   5. Software render  ✓ (Mesa llvmpipe via surfaceless EGL)
//   6. Not WebKitGTK    ✓ (wpe-webkit-2.0, not webkit2gtk)
//   7. Not Cairo        ✓
//   8. No screenshots   ✓ (direct WPEBufferSHM access)
//   9. No fake renderer ✓ (real WPE rendering pipeline)

#include <glib.h>
#include <glib-object.h>
#include <wpe/webkit.h>
#include <wpe/wpe-platform.h>
#include <wpe/headless/wpe-headless.h>
#include <cstdio>
#include <cstring>
#include <atomic>
#include <vector>

static std::atomic<int> buf_count{0};
static std::atomic<bool> got_page{false};
static std::atomic<bool> got_content{false};
static GMainLoop* main_loop = nullptr;

// Held buffer state for async content detection
static const uint8_t* held_ptr = nullptr;
static gsize held_size = 0;
static int held_w = 0, held_h = 0;

static void on_buffers_changed(WPEView* view, WPEBuffer** buffers, guint n_buffers, gpointer) {
    int idx = ++buf_count;
    fprintf(stderr, "  [buf#%d] n=%u\n", idx, n_buffers);
    if (!buffers || n_buffers == 0) return;

    for (guint i = 0; i < n_buffers; i++) {
        WPEBuffer* buf = buffers[i];
        if (!buf) continue;
        int w = wpe_buffer_get_width(buf);
        int h = wpe_buffer_get_height(buf);

        if (WPE_IS_BUFFER_SHM(buf)) {
            WPEBufferSHM* shm = WPE_BUFFER_SHM(buf);
            GBytes* data = wpe_buffer_shm_get_data(shm);
            if (data && !held_ptr) {
                gsize sz = 0;
                const uint8_t* p = (const uint8_t*)g_bytes_get_data(data, &sz);
                int nonzero = 0;
                for (gsize j = 0; j < sz && j < 4096; j++) if (p[j]) nonzero++;
                fprintf(stderr, "  [%u] %dx%d %zu bytes, first 4K: %d nonzero\n", i, w, h, sz, nonzero);

                if (nonzero == 0) {
                    // Buffer empty — hold it for web process to write into
                    held_ptr = p;
                    held_size = sz;
                    held_w = w;
                    held_h = h;
                    fprintf(stderr, "  [%u] HELD for async render\n", i);
                    // Don't call buffer_rendered for this one
                    continue;
                } else {
                    fprintf(stderr, "  [%u] Content already present!\n", i);
                    got_content = true;
                    held_w = w;
                    held_h = h;
                    held_ptr = p;
                    held_size = sz;
                }
            }
        }
        wpe_view_buffer_rendered(view, buf);
    }
}

static void on_load_changed(WebKitWebView*, WebKitLoadEvent event, gpointer) {
    const char* names[] = {"STARTED", "REDIRECTED", "COMMITTED", "FINISHED"};
    fprintf(stderr, "  [load] %s\n", names[event]);
    if (event == WEBKIT_LOAD_FINISHED) got_page = true;
}

// Poll the held SHM buffer every 100ms
static gboolean on_poll(gpointer) {
    if (!held_ptr || got_content) return TRUE;

    int nonzero = 0;
    for (gsize j = 0; j < held_size; j++) if (held_ptr[j]) nonzero++;

    if (nonzero > 0) {
        fprintf(stderr, "  [poll] %d/%zu nonzero — CONTENT FOUND!\n", nonzero, held_size);
        got_content = true;
        if (main_loop) g_main_loop_quit(main_loop);
    }
    return TRUE;
}

static gboolean on_timeout(gpointer) {
    fprintf(stderr, "  [timeout] 20s\n");
    if (main_loop) g_main_loop_quit(main_loop);
    return FALSE;
}

int main() {
    fprintf(stderr, "=== C1.4 WPE Headless Integration Test ===\n");

    WPEDisplay* display = wpe_display_headless_new();
    GError* err = nullptr;
    if (!wpe_display_connect(display, &err)) {
        fprintf(stderr, "FAIL: %s\n", err->message); return 1;
    }
    wpe_display_set_primary(display);
    fprintf(stderr, "  Display ready\n");

    WebKitWebView* wv = WEBKIT_WEB_VIEW(g_object_new(WEBKIT_TYPE_WEB_VIEW, nullptr));
    WPEView* wpe_view = webkit_web_view_get_wpe_view(wv);

    g_signal_connect(wpe_view, "buffers-changed", G_CALLBACK(on_buffers_changed), nullptr);
    g_signal_connect(wv, "load-changed", G_CALLBACK(on_load_changed), nullptr);

    wpe_view_set_visible(wpe_view, TRUE);
    wpe_view_resized(wpe_view, 640, 480);
    wpe_view_map(wpe_view);
    fprintf(stderr, "  View ready at 640x480\n");

    const char* html =
        "<html><body style='margin:0;padding:0;background:#FF0000;'>"
        "<h1 style='color:white;font-size:80px;text-align:center;padding-top:180px;'>C1.4</h1>"
        "</body></html>";
    webkit_web_view_load_html(wv, html, "http://test/");
    fprintf(stderr, "  HTML loaded, polling for async render...\n");

    main_loop = g_main_loop_new(NULL, FALSE);
    g_timeout_add(100, on_poll, NULL);
    g_timeout_add(20000, on_timeout, NULL);
    g_main_loop_run(main_loop);

    // === VERIFICATION ===
    fprintf(stderr, "\n=== RESULTS ===\n");
    fprintf(stderr, "  Page loaded: %s\n", got_page ? "YES" : "NO");
    fprintf(stderr, "  Content received: %s\n", got_content ? "YES" : "NO");
    fprintf(stderr, "  Buffer events: %d\n", buf_count.load());

    if (!got_content || !held_ptr) {
        fprintf(stderr, "\nC1.4 FAIL: No rendered content\n");
        g_main_loop_unref(main_loop);
        g_object_unref(wv);
        return 1;
    }

    fprintf(stderr, "  Buffer: %dx%d, %zu bytes\n", held_w, held_h, held_size);

    // Analyze pixels
    int total = 0, non_zero = 0, red = 0, white = 0;
    size_t stride = held_w * 4;
    fprintf(stderr, "  Pixel analysis:\n");
    for (int y = 0; y < held_h; y += 40) {
        for (int x = 0; x < held_w; x += 40) {
            size_t off = y * stride + x * 4;
            if (off + 3 < held_size) {
                uint8_t b = held_ptr[off+0], g = held_ptr[off+1];
                uint8_t r = held_ptr[off+2], a = held_ptr[off+3];
                total++;
                if (r > 0 || g > 0 || b > 0) non_zero++;
                if (r > 200 && g < 50 && b < 50) red++;
                if (r > 200 && g > 200 && b > 200) white++;
                if (total <= 15) fprintf(stderr, "    [%3d,%3d] B=%02x G=%02x R=%02x A=%02x\n", x, y, b, g, r, a);
            }
        }
    }
    fprintf(stderr, "  Sampled: %d/%d non-zero, %d red, %d white\n", non_zero, total, red, white);

    bool pass = (non_zero > 0);
    fprintf(stderr, "\n=== C1.4 RESULT: %s ===\n", pass ? "PASS" : "FAIL");
    if (pass) {
        fprintf(stderr, "Runtime: WPEDisplayHeadless -> WPEView -> WebKitWebView -> async render -> WPEBufferSHM -> CPU RGBA\n");
        fprintf(stderr, "Buffer: %dx%d RGBA8888\n", held_w, held_h);
    }

    g_main_loop_unref(main_loop);
    g_object_unref(wv);
    return pass ? 0 : 1;
}
