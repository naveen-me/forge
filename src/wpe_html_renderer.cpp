#include "wpe_html_renderer.h"
#include "logger.h"
#include <gtk/gtk.h>
#include <cstring>
#include <chrono>
#include <thread>

namespace tarva {

static bool gtk_initialized = false;
static void ensure_gtk_init() {
    if (!gtk_initialized) {
        int argc = 0;
        char** argv = nullptr;
        gtk_init_check(&argc, &argv);
        gtk_initialized = true;
    }
}

static void on_load_changed(WebKitWebView* web_view, WebKitLoadEvent load_event, gpointer user_data) {
    if (load_event == WEBKIT_LOAD_FINISHED) {
        bool* is_loaded = static_cast<bool*>(user_data);
        if (is_loaded) {
            *is_loaded = true;
        }
    }
}

static void fdo_export_shm_buffer_cb(void* data, struct wpe_fdo_shm_exported_buffer* buffer) {
    WpeHtmlRenderer* renderer = static_cast<WpeHtmlRenderer*>(data);
    if (renderer) {
        renderer->on_shm_buffer_exported(buffer);
    }
}

WpeHtmlRenderer::WpeHtmlRenderer(int width, int height)
    : width_(width), height_(height) {}

WpeHtmlRenderer::~WpeHtmlRenderer() {
    std::lock_guard<std::mutex> lock(render_mutex_);
    if (exportable_fdo_) {
        wpe_view_backend_exportable_fdo_destroy(exportable_fdo_);
        exportable_fdo_ = nullptr;
    }
    if (offscreen_surface_) {
        cairo_surface_destroy(offscreen_surface_);
        offscreen_surface_ = nullptr;
    }
    if (web_view_) {
        g_object_unref(web_view_);
        web_view_ = nullptr;
    }
}

void WpeHtmlRenderer::on_shm_buffer_exported(struct wpe_fdo_shm_exported_buffer* buffer) {
    if (!buffer) return;
    std::lock_guard<std::mutex> lock(render_mutex_);

    struct wl_shm_buffer* shm_buf = wpe_fdo_shm_exported_buffer_get_shm_buffer(buffer);
    if (shm_buf) {
        void* data = wl_shm_buffer_get_data(shm_buf);
        int32_t buf_w = wl_shm_buffer_get_width(shm_buf);
        int32_t buf_h = wl_shm_buffer_get_height(shm_buf);
        size_t size = buf_w * buf_h * 4;

        if (data && size > 0) {
            latest_shm_frame_.resize(size);
            std::memcpy(latest_shm_frame_.data(), data, size);
            // Only flag valid non-transparent rendered frame
            uint8_t a = latest_shm_frame_[3];
            uint8_t r = latest_shm_frame_[2];
            if (a > 0 || r > 0) {
                has_shm_frame_ = true;
            }
        }
    }

    struct wl_resource* res = wpe_fdo_shm_exported_buffer_get_resource(buffer);
    if (res && exportable_fdo_) {
        wpe_view_backend_exportable_fdo_dispatch_release_buffer(exportable_fdo_, res);
    }
}

bool WpeHtmlRenderer::initialize() {
    std::lock_guard<std::mutex> lock(render_mutex_);
    ensure_gtk_init();

    // Initialize WPEBackend-fdo SHM export
    wpe_loader_init("libWPEBackend-fdo-1.0.so");
    fdo_initialized_ = wpe_fdo_initialize_shm();

    static struct wpe_view_backend_exportable_fdo_client fdo_client = {};
    fdo_client.export_shm_buffer = fdo_export_shm_buffer_cb;

    exportable_fdo_ = wpe_view_backend_exportable_fdo_create(&fdo_client, this, width_, height_);
    if (exportable_fdo_) {
        view_backend_ = wpe_view_backend_exportable_fdo_get_view_backend(exportable_fdo_);
        LOG_INFO("WpeHtmlRenderer: WPEBackend-fdo exportable view backend created (" + std::to_string(width_) + "x" + std::to_string(height_) + ")");
    }

    WebKitWebContext* context = webkit_web_context_get_default();
    web_view_ = WEBKIT_WEB_VIEW(webkit_web_view_new_with_context(context));
    if (!web_view_) {
        LOG_ERROR("Failed to create WebKitWebView");
        return false;
    }

    g_object_ref_sink(web_view_);

    GtkWidget* window = gtk_offscreen_window_new();
    gtk_container_add(GTK_CONTAINER(window), GTK_WIDGET(web_view_));
    gtk_window_set_default_size(GTK_WINDOW(window), width_, height_);
    gtk_widget_set_size_request(GTK_WIDGET(web_view_), width_, height_);
    gtk_widget_show_all(window);

    WebKitSettings* settings = webkit_web_view_get_settings(web_view_);
    webkit_settings_set_enable_javascript(settings, TRUE);
    webkit_settings_set_enable_webgl(settings, FALSE);
    webkit_settings_set_allow_file_access_from_file_urls(settings, TRUE);

    offscreen_surface_ = cairo_image_surface_create(CAIRO_FORMAT_ARGB32, width_, height_);
    if (cairo_surface_status(offscreen_surface_) != CAIRO_STATUS_SUCCESS) {
        LOG_ERROR("Failed to create Cairo offscreen surface");
        return false;
    }

    LOG_INFO("WpeHtmlRenderer initialized successfully (" + std::to_string(width_) + "x" + std::to_string(height_) + ")");
    return true;
}

bool WpeHtmlRenderer::load_url(const std::string& url) {
    if (!web_view_) return false;

    current_url_ = url;
    bool loaded = false;
    gulong handler_id = g_signal_connect(web_view_, "load-changed", G_CALLBACK(on_load_changed), &loaded);

    webkit_web_view_load_uri(web_view_, url.c_str());

    auto start = std::chrono::steady_clock::now();
    while (!loaded) {
        while (gtk_events_pending()) {
            gtk_main_iteration_do(FALSE);
        }

        auto elapsed = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::steady_clock::now() - start).count();
        if (elapsed > 5000) {
            LOG_WARN("Timeout loading HTML URL: " + url);
            break;
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(10));
    }

    g_signal_handler_disconnect(web_view_, handler_id);
    is_loaded_ = true;
    return true;
}

bool WpeHtmlRenderer::load_html(const std::string& html, const std::string& base_uri) {
    if (!web_view_) return false;

    bool loaded = false;
    gulong handler_id = g_signal_connect(web_view_, "load-changed", G_CALLBACK(on_load_changed), &loaded);

    webkit_web_view_load_html(web_view_, html.c_str(), base_uri.c_str());

    auto start = std::chrono::steady_clock::now();
    while (!loaded) {
        while (gtk_events_pending()) {
            gtk_main_iteration_do(FALSE);
        }

        auto elapsed = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::steady_clock::now() - start).count();
        if (elapsed > 3000) {
            break;
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(5));
    }

    g_signal_handler_disconnect(web_view_, handler_id);
    is_loaded_ = true;
    return true;
}

bool WpeHtmlRenderer::capture_frame_rgba(uint8_t* dest_buffer, int target_w, int target_h) {
    if (!dest_buffer || !offscreen_surface_ || !web_view_) return false;

    std::lock_guard<std::mutex> lock(render_mutex_);

    // If direct WPEBackend SHM exported frame buffer is available, convert ARGB32 -> RGBA directly from RAM
    if (has_shm_frame_ && !latest_shm_frame_.empty()) {
        const uint32_t* src_data = reinterpret_cast<const uint32_t*>(latest_shm_frame_.data());
        for (int y = 0; y < target_h && y < height_; ++y) {
            const uint32_t* src_row = src_data + y * width_;
            uint8_t* dst_row = dest_buffer + y * target_w * 4;
            for (int x = 0; x < target_w && x < width_; ++x) {
                uint32_t pixel = src_row[x];
                dst_row[x * 4 + 0] = (pixel >> 16) & 0xFF; // R
                dst_row[x * 4 + 1] = (pixel >> 8) & 0xFF;  // G
                dst_row[x * 4 + 2] = (pixel >> 0) & 0xFF;  // B
                dst_row[x * 4 + 3] = (pixel >> 24) & 0xFF; // A
            }
        }
        return true;
    }

    while (gtk_events_pending()) {
        gtk_main_iteration_do(FALSE);
    }

    cairo_t* cr = cairo_create(offscreen_surface_);

    cairo_set_operator(cr, CAIRO_OPERATOR_CLEAR);
    cairo_paint(cr);
    cairo_set_operator(cr, CAIRO_OPERATOR_OVER);

    // Fill viewport default background
    cairo_set_source_rgba(cr, 1.0, 0.0, 0.0, 1.0);
    cairo_rectangle(cr, 0, 0, width_, height_);
    cairo_fill(cr);

    gtk_widget_draw(GTK_WIDGET(web_view_), cr);
    cairo_surface_flush(offscreen_surface_);
    cairo_destroy(cr);

    unsigned char* src_data = cairo_image_surface_get_data(offscreen_surface_);
    int src_stride = cairo_image_surface_get_stride(offscreen_surface_);

    if (!src_data) return false;

    for (int y = 0; y < target_h && y < height_; ++y) {
        const uint32_t* src_row = reinterpret_cast<const uint32_t*>(src_data + y * src_stride);
        uint8_t* dst_row = dest_buffer + y * target_w * 4;

        for (int x = 0; x < target_w && x < width_; ++x) {
            uint32_t pixel = src_row[x];
            uint8_t b = (pixel >> 0) & 0xFF;
            uint8_t g = (pixel >> 8) & 0xFF;
            uint8_t r = (pixel >> 16) & 0xFF;
            uint8_t a = (pixel >> 24) & 0xFF;

            dst_row[x * 4 + 0] = r;
            dst_row[x * 4 + 1] = g;
            dst_row[x * 4 + 2] = b;
            dst_row[x * 4 + 3] = a;
        }
    }

    return true;
}

} // namespace tarva
