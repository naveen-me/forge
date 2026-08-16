#ifndef TARVA_GPAC_COMPOSITOR_H
#define TARVA_GPAC_COMPOSITOR_H

#include <vector>
#include <memory>
#include <mutex>
#include <cstdint>
#include <cairo.h>
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

    // Composites active layers into output_rgba_buffer (size = canvas_w * canvas_h * 4 bytes)
    bool render_frame(const std::vector<RenderableLayer>& active_layers,
                      int64_t current_pts_ns,
                      uint8_t* output_rgba_buffer);

    int canvas_width() const { return width_; }
    int canvas_height() const { return height_; }
    int fps() const { return fps_; }

private:
    int width_ = 1920;
    int height_ = 1080;
    int fps_ = 30;

    double bg_r_ = 0.0;
    double bg_g_ = 0.0;
    double bg_b_ = 0.0;
    double bg_a_ = 1.0;

    cairo_surface_t* canvas_surface_ = nullptr;
    std::vector<uint8_t> scratch_layer_buf_;
    std::mutex render_mutex_;
};

} // namespace tarva

#endif // TARVA_GPAC_COMPOSITOR_H
