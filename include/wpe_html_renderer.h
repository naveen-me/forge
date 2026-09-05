#ifndef TARVA_WPE_HTML_RENDERER_H
#define TARVA_WPE_HTML_RENDERER_H

#include <string>
#include <vector>
#include <mutex>
#include <atomic>
#include <cstdint>

#include <glib.h>
#include <glib-object.h>
#include <wpe/webkit.h>
#include <wpe/wpe-platform.h>
#include <wpe/headless/wpe-headless.h>

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
    // Returns true when a CPU-readable frame is available.
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

    // WPEPlatform headless objects
    WPEDisplay* display_ = nullptr;
    WebKitWebView* web_view_ = nullptr;
    WPEView* wpe_view_ = nullptr;

    // Retained latest buffer state for capture_frame_rgba().
    // Owned by WPE; we only retain the GBytes reference.
    GBytes* latest_buffer_ = nullptr;
    int latest_w_ = 0;
    int latest_h_ = 0;
    int latest_stride_ = 0;
    int latest_format_ = 0;

    static void on_buffers_changed(WPEView* view, WPEBuffer** buffers, guint n_buffers, gpointer user_data);
    void release_latest_buffer();
};

} // namespace tarva

#endif // TARVA_WPE_HTML_RENDERER_H
