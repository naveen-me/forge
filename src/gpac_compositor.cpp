#include "gpac_compositor.h"
#include "logger.h"
#include <cmath>
#include <cstring>
#include <algorithm>

namespace tarva {

GpacCompositor::GpacCompositor(int canvas_w, int canvas_h, int fps)
    : width_(canvas_w), height_(canvas_h), fps_(fps) {
    canvas_surface_ = cairo_image_surface_create(CAIRO_FORMAT_ARGB32, width_, height_);
}

GpacCompositor::~GpacCompositor() {
    std::lock_guard<std::mutex> lock(render_mutex_);
    if (canvas_surface_) {
        cairo_surface_destroy(canvas_surface_);
        canvas_surface_ = nullptr;
    }
}

void GpacCompositor::set_canvas_size(int w, int h, int fps) {
    std::lock_guard<std::mutex> lock(render_mutex_);
    width_ = w;
    height_ = h;
    fps_ = fps;
    if (canvas_surface_) {
        cairo_surface_destroy(canvas_surface_);
    }
    canvas_surface_ = cairo_image_surface_create(CAIRO_FORMAT_ARGB32, width_, height_);
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
    if (!output_rgba_buffer || !canvas_surface_) return false;

    std::lock_guard<std::mutex> lock(render_mutex_);

    cairo_t* cr = cairo_create(canvas_surface_);

    // 1. Clear canvas and fill background
    cairo_set_operator(cr, CAIRO_OPERATOR_CLEAR);
    cairo_paint(cr);

    cairo_set_operator(cr, CAIRO_OPERATOR_OVER);
    cairo_set_source_rgba(cr, bg_r_, bg_g_, bg_b_, bg_a_);
    cairo_rectangle(cr, 0, 0, width_, height_);
    cairo_fill(cr);

    // 2. Sort active layers by z-index (ascending)
    std::vector<RenderableLayer> sorted_layers = active_layers;
    std::stable_sort(sorted_layers.begin(), sorted_layers.end(),
        [](const RenderableLayer& a, const RenderableLayer& b) {
            return a.layer_config.layer < b.layer_config.layer;
        });

    // 3. Composite each active layer onto canvas
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

        // Fast path for opaque full-canvas base video layer
        if (rl.layer_config.x == 0 && rl.layer_config.y == 0 &&
            layer_w == width_ && layer_h == height_ &&
            rl.layer_config.opacity >= 1.0 && !rl.layer_config.effect.has_value() &&
            rl.layer_config.rotation == 0.0) {

            unsigned char* canvas_data = cairo_image_surface_get_data(canvas_surface_);
            int canvas_stride = cairo_image_surface_get_stride(canvas_surface_);

            for (int y = 0; y < height_; ++y) {
                uint32_t* dst_row = reinterpret_cast<uint32_t*>(canvas_data + y * canvas_stride);
                const uint8_t* src_row = scratch_layer_buf_.data() + y * width_ * 4;

                for (int x = 0; x < width_; ++x) {
                    uint8_t r = src_row[x * 4 + 0];
                    uint8_t g = src_row[x * 4 + 1];
                    uint8_t b = src_row[x * 4 + 2];
                    dst_row[x] = (0xFF000000) | (r << 16) | (g << 8) | b;
                }
            }
            cairo_surface_mark_dirty(canvas_surface_);
            continue;
        }

        // Convert RGBA layer_buf to Cairo ARGB32 image surface
        cairo_surface_t* layer_surf = cairo_image_surface_create(CAIRO_FORMAT_ARGB32, layer_w, layer_h);
        unsigned char* surf_data = cairo_image_surface_get_data(layer_surf);
        int surf_stride = cairo_image_surface_get_stride(layer_surf);

        for (int y = 0; y < layer_h; ++y) {
            uint32_t* dst_row = reinterpret_cast<uint32_t*>(surf_data + y * surf_stride);
            const uint8_t* src_row = scratch_layer_buf_.data() + y * layer_w * 4;

            for (int x = 0; x < layer_w; ++x) {
                uint8_t r = src_row[x * 4 + 0];
                uint8_t g = src_row[x * 4 + 1];
                uint8_t b = src_row[x * 4 + 2];
                uint8_t a = src_row[x * 4 + 3];

                if (a == 255) {
                    dst_row[x] = (0xFF000000) | (r << 16) | (g << 8) | b;
                } else if (a == 0) {
                    dst_row[x] = 0;
                } else {
                    uint32_t pr = (r * a) / 255;
                    uint32_t pg = (g * a) / 255;
                    uint32_t pb = (b * a) / 255;
                    dst_row[x] = (a << 24) | (pr << 16) | (pg << 8) | pb;
                }
            }
        }
        cairo_surface_mark_dirty(layer_surf);
        cairo_surface_flush(layer_surf);


        cairo_save(cr);

        // Apply layer position and effects
        double pos_x = rl.layer_config.x;
        double pos_y = rl.layer_config.y;
        double effective_opacity = rl.layer_config.opacity;

        if (rl.layer_config.effect.has_value()) {
            const auto& eff = rl.layer_config.effect.value();
            double elapsed_sec = (double)(current_pts_ns - rl.layer_config.start_ns) / 1e9;
            if (elapsed_sec > 0) {
                if (eff.type == "scroll") {
                    pos_x -= eff.speed * elapsed_sec;
                } else if (eff.type == "slide") {
                    if (eff.direction == "left") pos_x -= eff.speed * elapsed_sec;
                    else if (eff.direction == "right") pos_x += eff.speed * elapsed_sec;
                    else if (eff.direction == "up") pos_y -= eff.speed * elapsed_sec;
                    else if (eff.direction == "down") pos_y += eff.speed * elapsed_sec;
                } else if (eff.type == "fade" && eff.duration_ns > 0) {
                    double fade_dur_sec = (double)eff.duration_ns / 1e9;
                    effective_opacity = rl.layer_config.opacity * std::min(1.0, elapsed_sec / fade_dur_sec);
                }
            }
        }

        cairo_translate(cr, pos_x, pos_y);

        // Apply rotation around center if configured
        if (rl.layer_config.rotation != 0.0) {
            cairo_translate(cr, layer_w / 2.0, layer_h / 2.0);
            cairo_rotate(cr, rl.layer_config.rotation * M_PI / 180.0);
            cairo_translate(cr, -layer_w / 2.0, -layer_h / 2.0);
        }

        cairo_set_source_surface(cr, layer_surf, 0, 0);

        // Apply effective opacity
        if (effective_opacity < 1.0) {
            cairo_paint_with_alpha(cr, effective_opacity);
        } else {
            cairo_paint(cr);
        }

        cairo_restore(cr);
        cairo_surface_destroy(layer_surf);
    }

    cairo_destroy(cr);
    cairo_surface_flush(canvas_surface_);

    // 4. Convert final canvas ARGB32 bytes to RGBA output_rgba_buffer
    unsigned char* canvas_data = cairo_image_surface_get_data(canvas_surface_);
    int canvas_stride = cairo_image_surface_get_stride(canvas_surface_);

    const uint32_t* src_pixels = reinterpret_cast<const uint32_t*>(canvas_data);
    uint32_t* dst_pixels = reinterpret_cast<uint32_t*>(output_rgba_buffer);
    int total_pixels = width_ * height_;

    for (int i = 0; i < total_pixels; ++i) {
        uint32_t p = src_pixels[i];
        // p in memory (ARGB32/BGRA): 0xAARRGGBB
        // Swap R (bits 16..23) and B (bits 0..7) to produce 0xAABBGGRR (RGBA)
        dst_pixels[i] = (p & 0xFF00FF00) | ((p & 0x00FF0000) >> 16) | ((p & 0x000000FF) << 16);
    }

    return true;
}

} // namespace tarva
