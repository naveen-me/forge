#include "wpe_html_renderer.h"
#include "logger.h"
#include <cstring>
#include <chrono>
#include <thread>

namespace tarva {

// Static load notification helper
struct LoadNotifier {
    bool* loaded;
};

static void on_load_changed(WebKitWebView*,
                            WebKitLoadEvent event,
                            gpointer user_data) {
    if (event == WEBKIT_LOAD_FINISHED) {
        auto* notifier = static_cast<LoadNotifier*>(user_data);
        if (notifier && notifier->loaded) {
            *notifier->loaded = true;
        }
    }
}

WpeHtmlRenderer::WpeHtmlRenderer(int width, int height)
    : width_(width), height_(height) {}

WpeHtmlRenderer::~WpeHtmlRenderer() {
    std::lock_guard<std::mutex> lock(render_mutex_);

    if (web_view_) {
        g_object_unref(web_view_);
        web_view_ = nullptr;
    }
    if (latest_buffer_) {
        g_bytes_unref(latest_buffer_);
        latest_buffer_ = nullptr;
    }
    latest_w_ = 0;
    latest_h_ = 0;
    latest_stride_ = 0;
    latest_format_ = 0;
    if (display_) {
        g_object_unref(display_);
        display_ = nullptr;
    }
}

void WpeHtmlRenderer::on_buffers_changed(WPEView* view,
                                         WPEBuffer** buffers,
                                         guint n_buffers,
                                         gpointer user_data) {
    WpeHtmlRenderer* renderer = static_cast<WpeHtmlRenderer*>(user_data);
    if (!renderer) return;

    std::lock_guard<std::mutex> lock(renderer->render_mutex_);

    if (!buffers || n_buffers == 0) {
        return;
    }

    for (guint i = 0; i < n_buffers; i++) {
        WPEBuffer* buf = buffers[i];
        if (!buf) continue;

        if (!WPE_IS_BUFFER_SHM(buf)) {
            wpe_view_buffer_rendered(view, buf);
            continue;
        }

        WPEBufferSHM* shm = WPE_BUFFER_SHM(buf);
        GBytes* data = wpe_buffer_shm_get_data(shm);
        if (!data) {
            wpe_view_buffer_rendered(view, buf);
            continue;
        }

        if (!renderer->latest_buffer_) {
            renderer->latest_buffer_ = data;
            renderer->latest_w_ = wpe_buffer_get_width(buf);
            renderer->latest_h_ = wpe_buffer_get_height(buf);
            renderer->latest_format_ = wpe_buffer_shm_get_format(shm);
            renderer->latest_stride_ = wpe_buffer_shm_get_stride(shm);

            gsize sz = 0;
            g_bytes_get_data(data, &sz);
            LOG_INFO(std::string("WpeHtmlRenderer: held WPEBufferSHM ") +
                     std::to_string(renderer->latest_w_) + "x" +
                     std::to_string(renderer->latest_h_) + " " +
                     std::to_string(sz) + " bytes");
        }

        wpe_view_buffer_rendered(view, buf);
    }
}

void WpeHtmlRenderer::release_latest_buffer() {
    if (latest_buffer_) {
        g_bytes_unref(latest_buffer_);
        latest_buffer_ = nullptr;
    }
    latest_w_ = 0;
    latest_h_ = 0;
    latest_stride_ = 0;
    latest_format_ = 0;
}

bool WpeHtmlRenderer::initialize() {
    std::lock_guard<std::mutex> lock(render_mutex_);

    if (web_view_) {
        return true;
    }

    WPEDisplay* display = wpe_display_headless_new();
    if (!display) {
        LOG_ERROR("WpeHtmlRenderer: failed to create WPEDisplayHeadless");
        return false;
    }

    GError* err = nullptr;
    if (!wpe_display_connect(display, &err)) {
        LOG_ERROR(std::string("WpeHtmlRenderer: failed to connect display: ") +
                   (err ? err->message : "unknown"));
        if (err) g_error_free(err);
        return false;
    }

    wpe_display_set_primary(display);
    display_ = display;

    web_view_ = WEBKIT_WEB_VIEW(g_object_new(WEBKIT_TYPE_WEB_VIEW, nullptr));
    if (!web_view_) {
        LOG_ERROR("WpeHtmlRenderer: failed to create WebKitWebView");
        return false;
    }

    wpe_view_ = webkit_web_view_get_wpe_view(web_view_);
    if (!wpe_view_) {
        LOG_ERROR("WpeHtmlRenderer: failed to obtain WPEView");
        return false;
    }

    g_signal_connect(wpe_view_, "buffers-changed",
                     G_CALLBACK(WpeHtmlRenderer::on_buffers_changed), this);

    wpe_view_set_visible(wpe_view_, TRUE);
    wpe_view_resized(wpe_view_, width_, height_);
    wpe_view_map(wpe_view_);

    LOG_INFO(std::string("WpeHtmlRenderer: initialized ") +
             std::to_string(width_) + "x" + std::to_string(height_));
    return true;
}

bool WpeHtmlRenderer::load_url(const std::string& url) {
    std::lock_guard<std::mutex> lock(render_mutex_);

    if (!web_view_) return false;

    current_url_ = url;
    bool loaded = false;
    LoadNotifier notifier{&loaded};

    gulong handler_id = g_signal_connect_data(
        web_view_, "load-changed",
        G_CALLBACK(on_load_changed), &notifier, nullptr, GConnectFlags(0));

    webkit_web_view_load_uri(web_view_, url.c_str());

    auto start = std::chrono::steady_clock::now();
    while (!loaded) {
        g_main_context_iteration(nullptr, FALSE);

        auto elapsed =
            std::chrono::duration_cast<std::chrono::milliseconds>(
                std::chrono::steady_clock::now() - start)
                .count();
        if (elapsed > 5000) {
            LOG_WARN(std::string("Timeout loading URL: ") + url);
            break;
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(10));
    }

    g_signal_handler_disconnect(web_view_, handler_id);
    is_loaded_ = true;
    return true;
}

bool WpeHtmlRenderer::load_html(const std::string& html,
                                const std::string& base_uri) {
    std::lock_guard<std::mutex> lock(render_mutex_);

    if (!web_view_) return false;

    bool loaded = false;
    LoadNotifier notifier{&loaded};

    gulong handler_id = g_signal_connect_data(
        web_view_, "load-changed",
        G_CALLBACK(on_load_changed), &notifier, nullptr, GConnectFlags(0));

    webkit_web_view_load_html(web_view_, html.c_str(), base_uri.c_str());

    auto start = std::chrono::steady_clock::now();
    while (!loaded) {
        g_main_context_iteration(nullptr, FALSE);

        auto elapsed =
            std::chrono::duration_cast<std::chrono::milliseconds>(
                std::chrono::steady_clock::now() - start)
                .count();
        if (elapsed > 3000) {
            break;
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(5));
    }

    g_signal_handler_disconnect(web_view_, handler_id);
    is_loaded_ = true;
    return true;
}

static bool convert_buffer_to_rgba(const uint8_t* src,
                                   int src_stride,
                                   int w,
                                   int h,
                                   int src_format,
                                   uint8_t* dst,
                                   int dst_stride) {
    if (src_format == 0) {
        // Format unknown/missing: assume BGRA in memory on this host.
        for (int y = 0; y < h; y++) {
            const uint8_t* src_row = src + y * src_stride;
            uint8_t* dst_row = dst + y * dst_stride;
            for (int x = 0; x < w; x++) {
                uint8_t b = src_row[x * 4 + 0];
                uint8_t g = src_row[x * 4 + 1];
                uint8_t r = src_row[x * 4 + 2];
                uint8_t a = src_row[x * 4 + 3];
                dst_row[x * 4 + 0] = r;
                dst_row[x * 4 + 1] = g;
                dst_row[x * 4 + 2] = b;
                dst_row[x * 4 + 3] = a;
            }
        }
        return true;
    }

    if (src_format == 3) {
        // WPE_BUFFER_FORMAT_RGBA8888 -> copy RGBA bytes as-is.
        for (int y = 0; y < h; y++) {
            const uint8_t* src_row = src + y * src_stride;
            uint8_t* dst_row = dst + y * dst_stride;
            for (int x = 0; x < w; x++) {
                dst_row[x * 4 + 0] = src_row[x * 4 + 0];
                dst_row[x * 4 + 1] = src_row[x * 4 + 1];
                dst_row[x * 4 + 2] = src_row[x * 4 + 2];
                dst_row[x * 4 + 3] = src_row[x * 4 + 3];
            }
        }
        return true;
    }

    if (src_format == 4) {
        // WPE_BUFFER_FORMAT_BGRA8888 -> swap to RGBA.
        for (int y = 0; y < h; y++) {
            const uint8_t* src_row = src + y * src_stride;
            uint8_t* dst_row = dst + y * dst_stride;
            for (int x = 0; x < w; x++) {
                uint8_t b = src_row[x * 4 + 0];
                uint8_t g = src_row[x * 4 + 1];
                uint8_t r = src_row[x * 4 + 2];
                uint8_t a = src_row[x * 4 + 3];
                dst_row[x * 4 + 0] = r;
                dst_row[x * 4 + 1] = g;
                dst_row[x * 4 + 2] = b;
                dst_row[x * 4 + 3] = a;
            }
        }
        return true;
    }

    LOG_WARN(std::string("WpeHtmlRenderer: unknown WPE buffer format ") +
             std::to_string(src_format));
    return false;
}

bool WpeHtmlRenderer::capture_frame_rgba(uint8_t* dest_buffer,
                                         int target_w,
                                         int target_h) {
    if (!dest_buffer || !web_view_) return false;

    std::lock_guard<std::mutex> lock(render_mutex_);

    g_main_context_iteration(nullptr, FALSE);

    if (!latest_buffer_) {
        return false;
    }

    if (latest_w_ == 0 || latest_h_ == 0) {
        return false;
    }

    int out_w = target_w;
    int out_h = target_h;
    if (out_w <= 0) out_w = width_;
    if (out_h <= 0) out_h = height_;

    int src_w = latest_w_;
    int src_h = latest_h_;
    if (src_w > out_w) src_w = out_w;
    if (src_h > out_h) src_h = out_h;

    gsize sz = 0;
    const uint8_t* src_data =
        (const uint8_t*)g_bytes_get_data(latest_buffer_, &sz);
    if (!src_data || sz == 0) {
        return false;
    }

    int src_stride = latest_stride_;
    if (src_stride <= 0) {
        src_stride = src_w * 4;
    }

    return convert_buffer_to_rgba(src_data,
                                 src_stride,
                                 src_w,
                                 src_h,
                                 latest_format_,
                                 dest_buffer,
                                 out_w * 4);
}

} // namespace tarva
