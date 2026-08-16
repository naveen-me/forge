#include "media_sources.h"
#include "logger.h"
#include <iostream>
#include <cstring>
#include <algorithm>

namespace tarva {

// ============================================================================
// VideoSource Implementation
// ============================================================================

VideoSource::VideoSource() {
    av_frame_ = av_frame_alloc();
    rgba_frame_ = av_frame_alloc();
    av_pkt_ = av_packet_alloc();
}

VideoSource::~VideoSource() {
    if (sws_ctx_) sws_freeContext(sws_ctx_);
    if (rgba_frame_) {
        if (rgba_frame_->data[0]) av_freep(&rgba_frame_->data[0]);
        av_frame_free(&rgba_frame_);
    }
    if (av_frame_) av_frame_free(&av_frame_);
    if (av_pkt_) av_packet_free(&av_pkt_);
    if (codec_ctx_) avcodec_free_context(&codec_ctx_);
    if (fmt_ctx_) avformat_close_input(&fmt_ctx_);
}

bool VideoSource::load(const std::string& filepath, int target_w, int target_h) {
    if (avformat_open_input(&fmt_ctx_, filepath.c_str(), nullptr, nullptr) < 0) {
        LOG_ERROR("VideoSource: Failed to open file: " + filepath);
        return false;
    }

    if (avformat_find_stream_info(fmt_ctx_, nullptr) < 0) {
        LOG_ERROR("VideoSource: Failed to find stream info");
        return false;
    }

    video_stream_idx_ = -1;
    for (unsigned int i = 0; i < fmt_ctx_->nb_streams; i++) {
        if (fmt_ctx_->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_stream_idx_ = i;
            break;
        }
    }

    if (video_stream_idx_ == -1) {
        LOG_ERROR("VideoSource: No video stream found in " + filepath);
        return false;
    }

    AVCodecParameters* codecpar = fmt_ctx_->streams[video_stream_idx_]->codecpar;
    const AVCodec* codec = avcodec_find_decoder(codecpar->codec_id);
    if (!codec) {
        LOG_ERROR("VideoSource: Unsupported codec");
        return false;
    }

    codec_ctx_ = avcodec_alloc_context3(codec);
    avcodec_parameters_to_context(codec_ctx_, codecpar);
    codec_ctx_->thread_count = 2;

    if (avcodec_open2(codec_ctx_, codec, nullptr) < 0) {
        LOG_ERROR("VideoSource: Failed to open codec");
        return false;
    }

    src_width_ = codec_ctx_->width;
    src_height_ = codec_ctx_->height;
    target_w_ = (target_w > 0) ? target_w : src_width_;
    target_h_ = (target_h > 0) ? target_h : src_height_;

    // Setup software scaler to scale and convert to AV_PIX_FMT_RGBA
    sws_ctx_ = sws_getContext(
        src_width_, src_height_, codec_ctx_->pix_fmt,
        target_w_, target_h_, AV_PIX_FMT_RGBA,
        SWS_BILINEAR, nullptr, nullptr, nullptr
    );

    // Allocate RGBA frame buffer
    int buffer_size = av_image_get_buffer_size(AV_PIX_FMT_RGBA, target_w_, target_h_, 1);
    uint8_t* rgba_buffer = (uint8_t*)av_malloc(buffer_size);
    av_image_fill_arrays(rgba_frame_->data, rgba_frame_->linesize, rgba_buffer,
                         AV_PIX_FMT_RGBA, target_w_, target_h_, 1);

    LOG_INFO("VideoSource loaded: " + filepath + " (" +
             std::to_string(src_width_) + "x" + std::to_string(src_height_) +
             " -> " + std::to_string(target_w_) + "x" + std::to_string(target_h_) + ")");
    return true;
}

bool VideoSource::read_frame_rgba(uint8_t* dest_buffer, int target_w, int target_h, int64_t pts_ns) {
    if (!fmt_ctx_ || !codec_ctx_ || !sws_ctx_ || !dest_buffer) return false;

    bool got_frame = false;

    while (!got_frame) {
        int ret = av_read_frame(fmt_ctx_, av_pkt_);
        if (ret < 0) {
            if (ret == AVERROR_EOF) {
                // Loop or EOF
                av_seek_frame(fmt_ctx_, video_stream_idx_, 0, AVSEEK_FLAG_BACKWARD);
                avcodec_flush_buffers(codec_ctx_);
                continue;
            }
            return false;
        }

        if (av_pkt_->stream_index == video_stream_idx_) {
            ret = avcodec_send_packet(codec_ctx_, av_pkt_);
            if (ret >= 0) {
                ret = avcodec_receive_frame(codec_ctx_, av_frame_);
                if (ret == 0) {
                    got_frame = true;
                }
            }
        }
        av_packet_unref(av_pkt_);
    }

    // Direct zero-copy scale and convert pixel format to RGBA
    uint8_t* dst_slices[1] = { dest_buffer };
    int dst_stride[1] = { target_w * 4 };
    sws_scale(sws_ctx_, av_frame_->data, av_frame_->linesize, 0, src_height_,
              dst_slices, dst_stride);

    return true;
}

void VideoSource::seek(int64_t pts_ns) {
    if (!fmt_ctx_) return;
    int64_t target_ts = av_rescale_q(pts_ns / 1000, {1, 1000000}, fmt_ctx_->streams[video_stream_idx_]->time_base);
    av_seek_frame(fmt_ctx_, video_stream_idx_, target_ts, AVSEEK_FLAG_BACKWARD);
    if (codec_ctx_) avcodec_flush_buffers(codec_ctx_);
}

// ============================================================================
// ImageSource Implementation
// ============================================================================

ImageSource::ImageSource() {}

ImageSource::~ImageSource() {
    if (surface_) {
        cairo_surface_destroy(surface_);
        surface_ = nullptr;
    }
}

bool ImageSource::load(const std::string& filepath, int target_w, int target_h) {
    surface_ = cairo_image_surface_create_from_png(filepath.c_str());
    if (cairo_surface_status(surface_) != CAIRO_STATUS_SUCCESS) {
        LOG_ERROR("ImageSource: Failed to load PNG image: " + filepath);
        return false;
    }

    width_ = cairo_image_surface_get_width(surface_);
    height_ = cairo_image_surface_get_height(surface_);
    cairo_format_t fmt = cairo_image_surface_get_format(surface_);

    LOG_INFO("ImageSource loaded: " + filepath + " (" + std::to_string(width_) + "x" + std::to_string(height_) + ") fmt=" + std::to_string((int)fmt));
    return true;
}

bool ImageSource::read_frame_rgba(uint8_t* dest_buffer, int target_w, int target_h, int64_t pts_ns) {
    if (!surface_ || !dest_buffer) return false;

    // Create target surface
    cairo_surface_t* target_surf = cairo_image_surface_create(CAIRO_FORMAT_ARGB32, target_w, target_h);
    cairo_t* cr = cairo_create(target_surf);

    cairo_set_operator(cr, CAIRO_OPERATOR_CLEAR);
    cairo_paint(cr);

    // Scale image to fit target bounds
    double sx = (double)target_w / width_;
    double sy = (double)target_h / height_;
    cairo_scale(cr, sx, sy);

    cairo_set_source_surface(cr, surface_, 0, 0);
    cairo_format_t src_fmt = cairo_image_surface_get_format(surface_);
    if (src_fmt == CAIRO_FORMAT_RGB24) {
        cairo_set_operator(cr, CAIRO_OPERATOR_SOURCE);
    } else {
        cairo_set_operator(cr, CAIRO_OPERATOR_OVER);
    }
    cairo_paint(cr);
    cairo_surface_flush(target_surf);
    cairo_destroy(cr);

    unsigned char* src_data = cairo_image_surface_get_data(target_surf);
    int src_stride = cairo_image_surface_get_stride(target_surf);

    cairo_format_t fmt = cairo_image_surface_get_format(target_surf);

    for (int y = 0; y < target_h; ++y) {
        const uint32_t* src_row = reinterpret_cast<const uint32_t*>(src_data + y * src_stride);
        uint8_t* dst_row = dest_buffer + y * target_w * 4;

        for (int x = 0; x < target_w; ++x) {
            uint32_t pixel = src_row[x];
            uint8_t b = (pixel >> 0) & 0xFF;
            uint8_t g = (pixel >> 8) & 0xFF;
            uint8_t r = (pixel >> 16) & 0xFF;
            uint8_t a = (fmt == CAIRO_FORMAT_RGB24) ? 0xFF : ((pixel >> 24) & 0xFF);

            dst_row[x * 4 + 0] = r;
            dst_row[x * 4 + 1] = g;
            dst_row[x * 4 + 2] = b;
            dst_row[x * 4 + 3] = a;
        }
    }

    cairo_surface_destroy(target_surf);
    return true;
}

// ============================================================================
// TextSource Implementation
// ============================================================================

TextSource::TextSource(const std::string& text, int font_size, const std::string& color, const std::string& bg_color)
    : text_(text), font_size_(font_size), color_(color), bg_color_(bg_color) {}

bool TextSource::load(const std::string& dummy_source, int target_w, int target_h) {
    width_ = (target_w > 0) ? target_w : 400;
    height_ = (target_h > 0) ? target_h : 100;
    return true;
}

bool TextSource::read_frame_rgba(uint8_t* dest_buffer, int target_w, int target_h, int64_t pts_ns) {
    if (!dest_buffer) return false;

    cairo_surface_t* surf = cairo_image_surface_create(CAIRO_FORMAT_ARGB32, target_w, target_h);
    cairo_t* cr = cairo_create(surf);

    cairo_set_operator(cr, CAIRO_OPERATOR_CLEAR);
    cairo_paint(cr);
    cairo_set_operator(cr, CAIRO_OPERATOR_OVER);

    // Render optional background
    if (!bg_color_.empty()) {
        cairo_set_source_rgba(cr, 0.0, 0.0, 0.0, 0.5);
        cairo_rectangle(cr, 0, 0, target_w, target_h);
        cairo_fill(cr);
    }

    // Render text using Cairo text path
    cairo_select_font_face(cr, "Sans", CAIRO_FONT_SLANT_NORMAL, CAIRO_FONT_WEIGHT_BOLD);
    cairo_set_font_size(cr, font_size_);
    cairo_set_source_rgba(cr, 1.0, 1.0, 1.0, 1.0); // White text

    cairo_text_extents_t extents;
    cairo_text_extents(cr, text_.c_str(), &extents);

    double x = 10;
    double y = (target_h / 2.0) + (extents.height / 2.0);
    cairo_move_to(cr, x, y);
    cairo_show_text(cr, text_.c_str());

    cairo_surface_flush(surf);
    cairo_destroy(cr);

    unsigned char* src_data = cairo_image_surface_get_data(surf);
    int src_stride = cairo_image_surface_get_stride(surf);

    for (int y_idx = 0; y_idx < target_h; ++y_idx) {
        const uint32_t* src_row = reinterpret_cast<const uint32_t*>(src_data + y_idx * src_stride);
        uint8_t* dst_row = dest_buffer + y_idx * target_w * 4;

        for (int x_idx = 0; x_idx < target_w; ++x_idx) {
            uint32_t pixel = src_row[x_idx];
            uint8_t b = (pixel >> 0) & 0xFF;
            uint8_t g = (pixel >> 8) & 0xFF;
            uint8_t r = (pixel >> 16) & 0xFF;
            uint8_t a = (pixel >> 24) & 0xFF;

            dst_row[x_idx * 4 + 0] = r;
            dst_row[x_idx * 4 + 1] = g;
            dst_row[x_idx * 4 + 2] = b;
            dst_row[x_idx * 4 + 3] = a;
        }
    }

    cairo_surface_destroy(surf);
    return true;
}

// ============================================================================
// HtmlSource Implementation
// ============================================================================

HtmlSource::HtmlSource() {}

bool HtmlSource::load(const std::string& url_or_html, int target_w, int target_h) {
    width_ = (target_w > 0) ? target_w : 1920;
    height_ = (target_h > 0) ? target_h : 1080;

    renderer_ = std::make_unique<WpeHtmlRenderer>(width_, height_);
    if (!renderer_->initialize()) {
        LOG_ERROR("HtmlSource: Failed to initialize WpeHtmlRenderer");
        return false;
    }

    bool ok = false;
    if (url_or_html.rfind("http://", 0) == 0 || url_or_html.rfind("https://", 0) == 0) {
        ok = renderer_->load_url(url_or_html);
    } else {
        ok = renderer_->load_html(url_or_html);
    }

    if (ok) {
        cached_rgba_buffer_.resize(width_ * height_ * 4, 0);
        has_cached_frame_ = renderer_->capture_frame_rgba(cached_rgba_buffer_.data(), width_, height_);
    }

    return ok;
}

bool HtmlSource::read_frame_rgba(uint8_t* dest_buffer, int target_w, int target_h, int64_t pts_ns) {
    if (!dest_buffer) return false;

    if (has_cached_frame_ && !cached_rgba_buffer_.empty()) {
        std::memcpy(dest_buffer, cached_rgba_buffer_.data(), target_w * target_h * 4);
        return true;
    }

    if (!renderer_) return false;
    return renderer_->capture_frame_rgba(dest_buffer, target_w, target_h);
}

} // namespace tarva
