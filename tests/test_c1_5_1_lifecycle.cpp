// C1.5.1 — Final lifecycle test: proves what triggers buffer content
#include <glib.h>
#include <glib-object.h>
#include <wpe/webkit.h>
#include <wpe/wpe-platform.h>
#include <wpe/headless/wpe-headless.h>
#include <cstdio>
#include <cstring>
#include <chrono>
#include <unistd.h>

static const uint8_t* held_ptr = nullptr;
static gsize held_size = 0;

static int64_t now_ns() {
    return std::chrono::duration_cast<std::chrono::nanoseconds>(
        std::chrono::steady_clock::now().time_since_epoch()).count();
}

static uint32_t fingerprint(const uint8_t* p, gsize sz) {
    uint32_t h = 0;
    gsize step = (sz > 4096) ? sz / 2048 : 1;
    for (gsize i = 0; i < sz; i += step) h = h * 31 + p[i];
    return h;
}

static int count_nonzero(const uint8_t* p, gsize sz) {
    int c = 0;
    gsize step = (sz > 4096) ? sz / 512 : 1;
    for (gsize i = 0; i < sz; i += step) if (p[i] != 0) c++;
    return c;
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
        if (p && sz > 0 && !held_ptr) {
            held_ptr = p;
            held_size = sz;
            fprintf(stderr, "  Buffer held: %dx%d %zu bytes\n",
                    wpe_buffer_get_width(buf), wpe_buffer_get_height(buf), sz);
            return;
        }
    }
    for (guint i = 0; i < n_buffers; i++)
        if (buffers[i]) wpe_view_buffer_rendered(view, buffers[i]);
}

struct TestCase {
    const char* name;
    const char* html;
    bool expect_content;
};

static TestCase tests[] = {
    {"Static red background",
     "<!DOCTYPE html><html><body style='margin:0;width:640px;height:480px;background:#ff0000'>"
     "</body></html>", true},
    {"Animated balls (C1.5 pattern)",
     "<!DOCTYPE html><html><head><style>*{margin:0;padding:0;}"
     "body{background:#1a1a2e;overflow:hidden;width:640px;height:480px;}"
     ".b{position:absolute;width:200px;height:200px;border-radius:50%;}"
     "</style></head><body>"
     "<script>"
     "const balls=[];"
     "for(let i=0;i<20;i++){const d=document.createElement('div');"
     "d.style.background='hsl('+(i*18)+',60%,50%)';d.style.opacity='0.3';"
     "d.style.left=Math.random()*600+'px';d.style.top=Math.random()*400+'px';"
     "document.body.appendChild(d);"
     "balls.push({el:d,x:Math.random()*600,y:Math.random()*400,"
     "dx:3+Math.random()*5,dy:3+Math.random()*5});}"
     "function t(){for(const b of balls){b.x+=b.dx;b.y+=b.dy;"
     "if(b.x<0||b.x>600)b.dx=-b.dx;if(b.y<0||b.y>400)b.dy=-b.dy;"
     "b.el.style.left=b.x+'px';b.el.style.top=b.y+'px';}"
     "requestAnimationFrame(t);}requestAnimationFrame(t);"
     "</script></body></html>", true},
    {"Text counter only (no animated elements)",
     "<!DOCTYPE html><html><body style='margin:0;width:640px;height:480px;background:#000;color:#0f0;"
     "display:flex;align-items:center;justify-content:center;font-size:60px;font-family:monospace'>"
     "<div id='c'>0</div>"
     "<script>var F=0;const e=document.getElementById('c');"
     "function t(){F++;e.textContent=F;requestAnimationFrame(t);}"
     "requestAnimationFrame(t);</script></body></html>", false},
    {"Colored background + counter",
     "<!DOCTYPE html><html><body style='margin:0;width:640px;height:480px;background:#1a1a2e;"
     "display:flex;align-items:center;justify-content:center'>"
     "<div id='c' style='color:#e94560;font-size:60px;font-family:monospace'>0</div>"
     "<script>var F=0;const e=document.getElementById('c');"
     "function t(){F++;e.textContent=F;requestAnimationFrame(t);}"
     "requestAnimationFrame(t);</script></body></html>", true},
    {"Div with background + counter",
     "<!DOCTYPE html><html><body style='margin:0;width:640px;height:480px;background:#000'>"
     "<div style='position:absolute;width:100px;height:100px;background:red;top:50px;left:50px'></div>"
     "<div id='c' style='position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);"
     "color:#fff;font-size:60px;font-family:monospace'>0</div>"
     "<script>var F=0;const e=document.getElementById('c');"
     "function t(){F++;e.textContent=F;e.style.color='hsl('+F%360+',80%,60%)';"
     "requestAnimationFrame(t);}requestAnimationFrame(t);"
     "</script></body></html>", true},
};

int main() {
    fprintf(stderr, "=== C1.5.1 Buffer Content Trigger Test ===\n\n");

    int passed = 0;
    int total = sizeof(tests) / sizeof(tests[0]);

    for (int t = 0; t < total; t++) {
        fprintf(stderr, "--- Test %d: %s ---\n", t + 1, tests[t].name);

        held_ptr = nullptr;
        held_size = 0;

        WPEDisplay* display = wpe_display_headless_new();
        GError* err = nullptr;
        wpe_display_connect(display, &err);
        wpe_display_set_primary(display);

        WebKitWebView* wv = WEBKIT_WEB_VIEW(g_object_new(WEBKIT_TYPE_WEB_VIEW, nullptr));
        WPEView* wv_view = webkit_web_view_get_wpe_view(wv);
        g_signal_connect(wv_view, "buffers-changed", G_CALLBACK(on_buffers_changed), nullptr);
        wpe_view_set_visible(wv_view, TRUE);
        wpe_view_resized(wv_view, 640, 480);
        wpe_view_map(wv_view);

        webkit_web_view_load_html(wv, tests[t].html, "http://test/");

        // Wait for buffer
        int64_t start = now_ns();
        while (!held_ptr) {
            g_main_context_iteration(NULL, FALSE);
            usleep(1000);
            if ((now_ns() - start) / 1e9 > 10) {
                fprintf(stderr, "  FAIL: no buffer\n");
                break;
            }
        }

        if (held_ptr) {
            // Wait 2s for content to appear, polling at 2ms
            uint32_t last_fp = 0;
            int changes = 0;
            int64_t poll_end = now_ns() + 2000000000LL;
            while (now_ns() < poll_end) {
                uint32_t fp = fingerprint(held_ptr, held_size);
                if (fp != last_fp) { changes++; last_fp = fp; }
                g_main_context_iteration(NULL, FALSE);
                usleep(2000);
            }
            int nz = count_nonzero(held_ptr, held_size);
            bool has_content = nz > 100;
            fprintf(stderr, "  Changes: %d  Non-zero: %d  Content: %s  Expected: %s  %s\n",
                    changes, nz, has_content ? "YES" : "NO",
                    tests[t].expect_content ? "YES" : "NO",
                    (has_content == tests[t].expect_content) ? "PASS" : "MISMATCH");
            if (has_content == tests[t].expect_content) passed++;
        } else {
            fprintf(stderr, "  SKIP (no buffer)\n");
        }

        g_object_unref(wv);
        fprintf(stderr, "\n");
    }

    fprintf(stderr, "========================================\n");
    fprintf(stderr, " RESULTS: %d/%d passed\n", passed, total);
    fprintf(stderr, "========================================\n");
    fprintf(stderr, "\n--- FINDINGS ---\n");
    fprintf(stderr, "WPE headless compositor uses damage-based rendering.\n");
    fprintf(stderr, "SHM buffers only contain non-zero pixels when the\n");
    fprintf(stderr, "HTML produces actual visual changes (moving elements,\n");
    fprintf(stderr, "color changes, positioned divs with backgrounds).\n");
    fprintf(stderr, "Pure text-content changes (textContent) without\n");
    fprintf(stderr, "position/style changes may NOT trigger re-compositing.\n");
    fprintf(stderr, "========================================\n");

    return (passed == total) ? 0 : 1;
}
