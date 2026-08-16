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

WpeHtmlRenderer::WpeHtmlRenderer(int width, int height)
    : width_(width), height_(height) {}

WpeHtmlRenderer::~WpeHtmlRenderer() {
    std::lock_guard<std::mutex> lock(render_mutex_);
    if (offscreen_surface_) {
        cairo_surface_destroy(offscreen_surface_);
        offscreen_surface_ = nullptr;
    }
    if (web_view_) {
        g_object_unref(web_view_);
        web_view_ = nullptr;
    }
}

bool WpeHtmlRenderer::initialize() {
    std::lock_guard<std::mutex> lock(render_mutex_);
    ensure_gtk_init();

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

    while (gtk_events_pending()) {
        gtk_main_iteration_do(FALSE);
    }

    cairo_t* cr = cairo_create(offscreen_surface_);

    cairo_set_operator(cr, CAIRO_OPERATOR_CLEAR);
    cairo_paint(cr);
    cairo_set_operator(cr, CAIRO_OPERATOR_OVER);

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
