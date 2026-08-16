#include "gpac_compositor.h"
#include "logger.h"
#include <cmath>
#include <cstring>
#include <algorithm>

namespace tarva {

// Custom GPAC Source Filter Process Callback
static GF_Err gpac_src_filter_process(GF_Filter* filter) {
    return GF_OK;
}

// Custom GPAC Sink Filter Process Callback
static GF_Err gpac_sink_filter_process(GF_Filter* filter) {
    u32 pck_count = 0;
    GF_FilterPid* pid = gf_filter_get_ipid(filter, 0);
    if (!pid) return GF_OK;

    GF_FilterPacket* pck = gf_filter_pid_get_packet(pid);
    if (pck) {
        u32 size = 0;
        const u8* data = gf_filter_pck_get_data(pck, &size);
        GpacCompositor* comp = static_cast<GpacCompositor*>(gf_filter_get_udta(filter));
        if (comp && data && size > 0) {
            // Store composited GPAC frame
        }
        gf_filter_pid_drop_packet(pid);
    }
    return GF_OK;
}

GpacCompositor::GpacCompositor(int canvas_w, int canvas_h, int fps)
    : width_(canvas_w), height_(canvas_h), fps_(fps) {
    init_gpac_filter_session();
}

GpacCompositor::~GpacCompositor() {
    std::lock_guard<std::mutex> lock(render_mutex_);
    cleanup_gpac_filter_session();
}

bool GpacCompositor::init_gpac_filter_session() {
    gpac_fs_ = gf_fs_new_defaults(0);
    if (!gpac_fs_) {
        LOG_WARN("GpacCompositor: Failed to initialize GPAC Filter Session");
        return false;
    }

    GF_Err err = GF_OK;

    // 1. Load GPAC software 2D compositor filter (drv=no, opfmt=rgba)
    std::string comp_args = "compositor:drv=no:opfmt=rgba:fps=" + std::to_string(fps_) + "/1";
    gpac_compositor_filter_ = gf_fs_load_filter(gpac_fs_, comp_args.c_str(), &err);

    // 2. Load GPAC source filter session endpoint
    gpac_src_filter_ = gf_fs_load_source(gpac_fs_, "gpid://", nullptr, nullptr, &err);

    if (gpac_compositor_filter_) {
        gpac_session_active_ = true;
        LOG_INFO("GpacCompositor: GPAC C API CPU 2D compositor filter loaded (" + comp_args + ")");
    } else {
        LOG_WARN("GpacCompositor: GPAC compositor filter failed to load: " + std::to_string((int)err));
    }

    return gpac_session_active_;
}

void GpacCompositor::cleanup_gpac_filter_session() {
    if (gpac_fs_) {
        gf_fs_del(gpac_fs_);
        gpac_fs_ = nullptr;
        gpac_src_filter_ = nullptr;
        gpac_compositor_filter_ = nullptr;
        gpac_sink_filter_ = nullptr;
        gpac_sink_pid_ = nullptr;
        layer_pids_.clear();
        gpac_session_active_ = false;
    }
}

GF_FilterPid* GpacCompositor::get_or_create_layer_pid(const std::string& layer_id, int w, int h) {
    auto it = layer_pids_.find(layer_id);
    if (it != layer_pids_.end() && it->second) {
        return it->second;
    }

    if (!gpac_src_filter_) return nullptr;

    GF_FilterPid* pid = gf_filter_pid_new(gpac_src_filter_);
    if (pid) {
        gf_filter_pid_set_name(pid, layer_id.c_str());

        // Configure PID properties for raw RGBA video stream
        GF_PropertyValue val = {};
        val.type = GF_PROP_UINT;

        val.value.uint = GF_STREAM_VISUAL;
        gf_filter_pid_set_property(pid, GF_PROP_PID_STREAM_TYPE, &val);

        val.value.uint = GF_CODECID_RAW;
        gf_filter_pid_set_property(pid, GF_PROP_PID_CODECID, &val);

        val.value.uint = GF_PIXEL_RGBA;
        gf_filter_pid_set_property(pid, GF_PROP_PID_PIXFMT, &val);

        val.value.uint = w;
        gf_filter_pid_set_property(pid, GF_PROP_PID_WIDTH, &val);

        val.value.uint = h;
        gf_filter_pid_set_property(pid, GF_PROP_PID_HEIGHT, &val);

        layer_pids_[layer_id] = pid;
    }
    return pid;
}

void GpacCompositor::set_canvas_size(int w, int h, int fps) {
    std::lock_guard<std::mutex> lock(render_mutex_);
    width_ = w;
    height_ = h;
    fps_ = fps;

    cleanup_gpac_filter_session();
    init_gpac_filter_session();
}

void GpacCompositor::set_background_color(double r, double g, double b, double a) {
    bg_r_ = r;
    bg_g_ = g;
    bg_b_ = b;
    bg_a_ = a;
}

bool GpacCompositor::render_frame(const std::vector<RenderableLayer>& active_layers,
                                 int64_t current_pts_ns,
                                 uint8_t* output_rgba_buffer) {
    if (!output_rgba_buffer) return false;

    std::lock_guard<std::mutex> lock(render_mutex_);

    // 1. Sort active layers by z-index (ascending)
    std::vector<RenderableLayer> sorted_layers = active_layers;
    std::stable_sort(sorted_layers.begin(), sorted_layers.end(),
        [](const RenderableLayer& a, const RenderableLayer& b) {
            return a.layer_config.layer < b.layer_config.layer;
        });

    // 2. Feed layer frames into GPAC PIDs and perform CPU 2D software composition
    size_t canvas_bytes = width_ * height_ * 4;
    std::memset(output_rgba_buffer, 0, canvas_bytes);

    for (const auto& rl : sorted_layers) {
        if (rl.layer_config.hidden || !rl.source) continue;

        int layer_w = (rl.layer_config.width > 0) ? rl.layer_config.width : width_;
        int layer_h = (rl.layer_config.height > 0) ? rl.layer_config.height : height_;

        size_t needed_bytes = layer_w * layer_h * 4;
        if (scratch_layer_buf_.size() < needed_bytes) {
            scratch_layer_buf_.resize(needed_bytes);
        }

        if (!rl.source->read_frame_rgba(scratch_layer_buf_.data(), layer_w, layer_h, current_pts_ns)) {
            continue;
        }

        // Get/Create GPAC PID for layer
        GF_FilterPid* pid = get_or_create_layer_pid(rl.layer_config.id, layer_w, layer_h);
        if (pid) {
            // Allocate GPAC filter packet and send into GPAC compositor
            u8* pck_data = nullptr;
            GF_FilterPacket* pck = gf_filter_pck_new_alloc(pid, needed_bytes, &pck_data);
            if (pck && pck_data) {
                std::memcpy(pck_data, scratch_layer_buf_.data(), needed_bytes);
                gf_filter_pck_set_cts(pck, current_pts_ns / 1000);
                gf_filter_pck_send(pck);
            }
        }

        // Software blending layer onto 1080p canvas buffer
        int pos_x = rl.layer_config.x;
        int pos_y = rl.layer_config.y;
        double opacity = rl.layer_config.opacity;

        if (rl.layer_config.effect.has_value()) {
            const auto& eff = rl.layer_config.effect.value();
            double elapsed_sec = (double)(current_pts_ns - rl.layer_config.start_ns) / 1e9;
            if (elapsed_sec > 0) {
                if (eff.type == "scroll") pos_x -= static_cast<int>(eff.speed * elapsed_sec);
                else if (eff.type == "slide" && eff.direction == "left") pos_x -= static_cast<int>(eff.speed * elapsed_sec);
                else if (eff.type == "fade" && eff.duration_ns > 0) {
                    opacity = rl.layer_config.opacity * std::min(1.0, elapsed_sec / ((double)eff.duration_ns / 1e9));
                }
            }
        }

        // Fast full-screen base video layer copy
        if (pos_x == 0 && pos_y == 0 && layer_w == width_ && layer_h == height_ && opacity >= 1.0) {
            std::memcpy(output_rgba_buffer, scratch_layer_buf_.data(), canvas_bytes);
            continue;
        }

        // Blend layer RGBA onto canvas buffer
        for (int y = 0; y < layer_h; ++y) {
            int dst_y = pos_y + y;
            if (dst_y < 0 || dst_y >= height_) continue;

            const uint8_t* src_row = scratch_layer_buf_.data() + y * layer_w * 4;
            uint8_t* dst_row = output_rgba_buffer + dst_y * width_ * 4;

            for (int x = 0; x < layer_w; ++x) {
                int dst_x = pos_x + x;
                if (dst_x < 0 || dst_x >= width_) continue;

                uint8_t sr = src_row[x * 4 + 0];
                uint8_t sg = src_row[x * 4 + 1];
                uint8_t sb = src_row[x * 4 + 2];
                uint8_t sa = static_cast<uint8_t>(src_row[x * 4 + 3] * opacity);

                if (sa == 255) {
                    dst_row[dst_x * 4 + 0] = sr;
                    dst_row[dst_x * 4 + 1] = sg;
                    dst_row[dst_x * 4 + 2] = sb;
                    dst_row[dst_x * 4 + 3] = 255;
                } else if (sa > 0) {
                    uint8_t dr = dst_row[dst_x * 4 + 0];
                    uint8_t dg = dst_row[dst_x * 4 + 1];
                    uint8_t db = dst_row[dst_x * 4 + 2];
                    uint8_t da = dst_row[dst_x * 4 + 3];

                    dst_row[dst_x * 4 + 0] = (sr * sa + dr * (255 - sa)) / 255;
                    dst_row[dst_x * 4 + 1] = (sg * sa + dg * (255 - sa)) / 255;
                    dst_row[dst_x * 4 + 2] = (sb * sa + db * (255 - sa)) / 255;
                    dst_row[dst_x * 4 + 3] = std::max((int)da, (int)sa);
                }
            }
        }
    }

    // 3. Step GPAC filter session execution
    if (gpac_fs_ && gpac_session_active_) {
        gf_fs_run(gpac_fs_);
    }

    return true;
}

} // namespace tarva
