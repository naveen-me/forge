#ifndef TARVA_GPAC_COMPOSITOR_H
#define TARVA_GPAC_COMPOSITOR_H

#include <vector>
#include <memory>
#include <mutex>
#include <cstdint>
#include <cairo.h>

extern "C" {
#include <gpac/filters.h>
#include <gpac/compositor.h>
#include <gpac/constants.h>
}

#include "scene_schema.h"
#include "media_sources.h"

namespace tarva {

struct RenderableLayer {
    Layer layer_config;
    std::shared_ptr<MediaSource> source;
};

class GpacCompositor {
public:
    GpacCompositor(int canvas_w = 1920, int canvas_h = 1080, int fps = 30);
    ~GpacCompositor();

    void set_canvas_size(int w, int h, int fps);
    void set_background_color(double r, double g, double b, double a = 1.0);

    // Composites active layers into output_rgba_buffer using GPAC CPU 2D software pipeline
    bool render_frame(const std::vector<RenderableLayer>& active_layers,
                      int64_t current_pts_ns,
                      uint8_t* output_rgba_buffer);

    int canvas_width() const { return width_; }
    int canvas_height() const { return height_; }
    int fps() const { return fps_; }
    bool is_gpac_session_active() const { return gpac_session_active_; }

private:
    int width_ = 1920;
    int height_ = 1080;
    int fps_ = 30;

    double bg_r_ = 0.0;
    double bg_g_ = 0.0;
    double bg_b_ = 0.0;
    double bg_a_ = 1.0;

    GF_FilterSession* gpac_fs_ = nullptr;
    GF_Filter* gpac_compositor_filter_ = nullptr;
    bool gpac_session_active_ = false;

    cairo_surface_t* canvas_surface_ = nullptr;
    std::vector<uint8_t> scratch_layer_buf_;
    std::mutex render_mutex_;

    bool init_gpac_filter_session();
    void cleanup_gpac_filter_session();
};

} // namespace tarva

#endif // TARVA_GPAC_COMPOSITOR_H
