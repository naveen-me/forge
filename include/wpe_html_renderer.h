#ifndef TARVA_WPE_HTML_RENDERER_H
#define TARVA_WPE_HTML_RENDERER_H

#include <string>
#include <vector>
#include <mutex>
#include <atomic>
#include <cstdint>
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

private:
    int width_;
    int height_;
    std::string current_url_;
    std::atomic<bool> is_loaded_{false};
    std::mutex render_mutex_;

    WebKitWebView* web_view_ = nullptr;
    cairo_surface_t* offscreen_surface_ = nullptr;
};

} // namespace tarva

#endif // TARVA_WPE_HTML_RENDERER_H
