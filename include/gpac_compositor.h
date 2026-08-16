#ifndef TARVA_GPAC_COMPOSITOR_H
#define TARVA_GPAC_COMPOSITOR_H

#include <vector>
#include <memory>
#include <mutex>
#include <map>
#include <cstdint>

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

    // Composites active layers into output_rgba_buffer using pure GPAC C API filter graph pipeline
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
    GF_Filter* gpac_src_filter_ = nullptr;
    GF_Filter* gpac_compositor_filter_ = nullptr;
    GF_Filter* gpac_sink_filter_ = nullptr;
    GF_FilterPid* gpac_sink_pid_ = nullptr;
    bool gpac_session_active_ = false;

    std::map<std::string, GF_FilterPid*> layer_pids_;

    std::vector<uint8_t> scratch_layer_buf_;
    std::vector<uint8_t> last_composited_frame_;
    std::mutex render_mutex_;

    bool init_gpac_filter_session();
    void cleanup_gpac_filter_session();
    GF_FilterPid* get_or_create_layer_pid(const std::string& layer_id, int w, int h);
};

} // namespace tarva

#endif // TARVA_GPAC_COMPOSITOR_H
