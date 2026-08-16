#ifndef TARVA_WPE_HTML_RENDERER_H
#define TARVA_WPE_HTML_RENDERER_H

#include <string>
#include <vector>
#include <mutex>
#include <atomic>
#include <cstdint>

#include <wpe/wpe.h>
#include <wpe/fdo.h>
#include <wpe/unstable/fdo-shm.h>
#include <wayland-server-core.h>

#include <webkit2/webkit2.h>
#include <cairo.h>

namespace tarva {

class WpeHtmlRenderer {
public:
    WpeHtmlRenderer(int width, int height);
    ~WpeHtmlRenderer();

    bool initialize();
    bool load_url(const std::string& url);
    bool load_html(const std::string& html, const std::string& base_uri = "http://localhost");

    // Renders current page into dest_buffer as raw RGBA bytes.
    // dest_buffer must be pre-allocated with size width * height * 4.
    bool capture_frame_rgba(uint8_t* dest_buffer, int target_w, int target_h);

    int width() const { return width_; }
    int height() const { return height_; }
    bool is_loaded() const { return is_loaded_; }
    bool is_wpe_fdo_active() const { return fdo_initialized_; }

    void on_shm_buffer_exported(struct wpe_fdo_shm_exported_buffer* buffer);

private:
    int width_;
    int height_;
    std::string current_url_;
    std::atomic<bool> is_loaded_{false};
    bool fdo_initialized_ = false;
    std::mutex render_mutex_;

    struct wpe_view_backend_exportable_fdo* exportable_fdo_ = nullptr;
    struct wpe_view_backend* view_backend_ = nullptr;

    WebKitWebView* web_view_ = nullptr;
    cairo_surface_t* offscreen_surface_ = nullptr;
    std::vector<uint8_t> latest_shm_frame_;
    bool has_shm_frame_ = false;
};

} // namespace tarva

#endif // TARVA_WPE_HTML_RENDERER_H
