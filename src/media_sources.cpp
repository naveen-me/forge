#include "media_sources.h"
#include "logger.h"
#include <iostream>
#include <cstring>
#include <algorithm>
#include <libavutil/mathematics.h>

namespace tarva {

// ============================================================================
// VideoSource Implementation
// ============================================================================

namespace {

// Internal decode-result sentinels (never valid pts values).
constexpr int64_t kDecodeErrorPts = INT64_MIN;
constexpr int64_t kEofPts = INT64_MIN + 1;

} // namespace

VideoSource::VideoSource() {
    av_frame_ = av_frame_alloc();
    rgba_frame_ = av_frame_alloc();
    av_pkt_ = av_packet_alloc();
    audio_pkt_ = av_packet_alloc();
    audio_frame_ = av_frame_alloc();
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
    if (swr_ctx_) swr_free(&swr_ctx_);
    if (audio_frame_) av_frame_free(&audio_frame_);
    if (audio_pkt_) av_packet_free(&audio_pkt_);
    if (audio_codec_ctx_) avcodec_free_context(&audio_codec_ctx_);
    if (audio_fmt_ctx_) avformat_close_input(&audio_fmt_ctx_);
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

    // Duration (used to wrap a looping source to the global clock).
    AVStream* vstream = fmt_ctx_->streams[video_stream_idx_];
    if (vstream->duration != AV_NOPTS_VALUE) {
        duration_ns_ = av_rescale_q(vstream->duration, vstream->time_base, {1, 1000000000});
    } else if (fmt_ctx_->duration != AV_NOPTS_VALUE) {
        duration_ns_ = fmt_ctx_->duration * 1000; // AV_TIME_BASE (us) -> ns
    }

    // Bounded reorder buffer depth: has_b_frames + 1 covers the maximum number
    // of frames the decoder can hold out of presentation order (B-frames).
    reorder_capacity_ = std::max<size_t>(2, static_cast<size_t>(codec_ctx_->has_b_frames) + 1);

    LOG_INFO("VideoSource loaded: " + filepath + " (" +
             std::to_string(src_width_) + "x" + std::to_string(src_height_) +
             " -> " + std::to_string(target_w_) + "x" + std::to_string(target_h_) + ")");

    // --- audio stream (optional) ---
    // A second, isolated demux context is used so audio decoding never
    // interferes with the video packet stream (the video reader drops
    // non-video packets).
    if (avformat_open_input(&audio_fmt_ctx_, filepath.c_str(), nullptr, nullptr) == 0 &&
        avformat_find_stream_info(audio_fmt_ctx_, nullptr) >= 0) {
        for (unsigned int i = 0; i < audio_fmt_ctx_->nb_streams; i++) {
            if (audio_fmt_ctx_->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
                audio_stream_idx_ = i;
                break;
            }
        }

        if (audio_stream_idx_ >= 0) {
            AVCodecParameters* acpar = audio_fmt_ctx_->streams[audio_stream_idx_]->codecpar;
            const AVCodec* acodec = avcodec_find_decoder(acpar->codec_id);
            int audio_source_rate = acpar->sample_rate > 0 ? acpar->sample_rate : audio_output_sample_rate_;
            AVChannelLayout in_layout;
            if (acpar->ch_layout.nb_channels <= 0) {
                av_channel_layout_default(&in_layout, 1);
            } else {
                av_channel_layout_copy(&in_layout, &acpar->ch_layout);
            }

            AVChannelLayout out_layout;
            av_channel_layout_default(&out_layout, audio_output_channels_);

            if (acodec) {
                audio_codec_ctx_ = avcodec_alloc_context3(acodec);
                avcodec_parameters_to_context(audio_codec_ctx_, acpar);
                audio_codec_ctx_->thread_count = 1;
                if (avcodec_open2(audio_codec_ctx_, acodec, nullptr) == 0) {
                    SwrContext* swr = nullptr;
                    int sret = swr_alloc_set_opts2(
                        &swr, &out_layout, AV_SAMPLE_FMT_S16, audio_output_sample_rate_,
                        &in_layout, static_cast<AVSampleFormat>(acpar->format),
                        audio_source_rate, 0, nullptr);
                    if (sret == 0 && swr_init(swr) < 0) {
                        swr_free(&swr);
                        swr = nullptr;
                    }
                    swr_ctx_ = swr;
                }
            }
            av_channel_layout_uninit(&in_layout);
            av_channel_layout_uninit(&out_layout);

            AVStream* astream = audio_fmt_ctx_->streams[audio_stream_idx_];
            if (astream->duration != AV_NOPTS_VALUE) {
                audio_duration_ns_ = av_rescale_q(astream->duration, astream->time_base, {1, 1000000000});
            } else if (audio_fmt_ctx_->duration != AV_NOPTS_VALUE) {
                audio_duration_ns_ = audio_fmt_ctx_->duration * 1000;
            }
        }

        if (!audio_codec_ctx_ || !swr_ctx_) {
            // Unusable audio (no decoder/resampler): disable the audio stream.
            if (audio_codec_ctx_) avcodec_free_context(&audio_codec_ctx_);
            if (swr_ctx_) swr_free(&swr_ctx_);
            audio_stream_idx_ = -1;
        } else {
            LOG_INFO("VideoSource audio stream ready -> " + std::to_string(audio_output_sample_rate_) +
                     " Hz S16 stereo");
        }
    } else {
        audio_stream_idx_ = -1;
    }

    return true;
}

int64_t VideoSource::decode_next_video_frame() {
    auto frame_pts_ns = [this]() -> int64_t {
        if (av_frame_->pts == AV_NOPTS_VALUE) return AV_NOPTS_VALUE;
        return av_rescale_q(av_frame_->pts,
                            fmt_ctx_->streams[video_stream_idx_]->time_base,
                            {1, 1000000000});
    };

    for (;;) {
        int ret = av_read_frame(fmt_ctx_, av_pkt_);
        if (ret < 0) {
            if (ret == AVERROR_EOF) {
                // Decoders hold the final B-frame group internally until an
                // explicit flush (send nullptr); receive alone won't release it.
                if (!decoder_flushed_) {
                    decoder_flushed_ = true;
                    avcodec_send_packet(codec_ctx_, nullptr);
                }
                if (avcodec_receive_frame(codec_ctx_, av_frame_) == 0) {
                    return frame_pts_ns();
                }
                return kEofPts;
            }
            return kDecodeErrorPts;
        }

        if (av_pkt_->stream_index != video_stream_idx_) {
            av_packet_unref(av_pkt_);
            continue;
        }

        // Hand the packet to the decoder. When the internal reorder buffer is
        // full, send returns EAGAIN — drain frames first, then retry the same
        // packet (never drop it).
        ret = avcodec_send_packet(codec_ctx_, av_pkt_);
        if (ret == AVERROR(EAGAIN)) {
            if (avcodec_receive_frame(codec_ctx_, av_frame_) == 0) {
                av_packet_unref(av_pkt_);
                return frame_pts_ns();
            }
            ret = avcodec_send_packet(codec_ctx_, av_pkt_);
        }
        av_packet_unref(av_pkt_);
        if (ret < 0) continue;

        // A single packet can produce a frame immediately or only after more
        // packets arrive (B-frame reordering).
        if (avcodec_receive_frame(codec_ctx_, av_frame_) == 0) {
            return frame_pts_ns();
        }
    }
}

void VideoSource::scale_into(AVFrame* src, uint8_t* dest, int w, int h) {
    uint8_t* dst_slices[1] = { dest };
    int dst_stride[1] = { w * 4 };
    sws_scale(sws_ctx_, src->data, src->linesize, 0, src_height_,
              dst_slices, dst_stride);
}

bool VideoSource::read_frame_rgba(uint8_t* dest_buffer, int target_w, int target_h, int64_t pts_ns) {
    if (!fmt_ctx_ || !codec_ctx_ || !sws_ctx_ || !dest_buffer) return false;

    const int64_t kSyncToleranceNs = 5 * 1000000LL; // 5 ms

    // For a looping source the caller's global clock maps into the source
    // timeline as (time mod duration). All comparisons below use this mapped
    // target so a looped source stays in sync with the clock.
    const int64_t content_target = (duration_ns_ > 0) ? pts_ns % duration_ns_ : pts_ns;
    bool wrapped = false;

    // Backward jump (or explicit seek): resync from the nearest keyframe.
    if (last_presented_pts_ns_ > content_target + kSyncToleranceNs) {
        seek(content_target);
        last_presented_pts_ns_ = -1;
        reorder_buf_.clear();
    }

    size_t frame_bytes = static_cast<size_t>(target_w) * target_h * 4;

    for (;;) {
        // Present or drop the front of the window before decoding anything
        // more: a presentable frame must never wait behind a decode that can
        // only hit EOF.
        if (!reorder_buf_.empty()) {
            if (reorder_buf_.front().pts_ns >= content_target) {
                scale_into(reorder_buf_.front().frame, dest_buffer, target_w, target_h);
                last_presented_pts_ns_ = reorder_buf_.front().pts_ns;
                reorder_buf_.pop_front();
                return true;
            }
            // Stale (before the requested time): drop and keep draining.
            reorder_buf_.pop_front();
            continue;
        }

        // Window empty: refill it. The oldest frame is presentation-order-
        // complete once the window holds reorder depth + 1 frames (B-frames).
        while (reorder_buf_.size() < reorder_capacity_) {
            int64_t fpts = decode_next_video_frame();
            if (fpts == kDecodeErrorPts) return false;

            if (fpts == kEofPts) {
                // End of source: wrap to the position matching the global clock.
                if (duration_ns_ <= 0 || wrapped) return false;
                wrapped = true;
                seek(content_target);
                last_presented_pts_ns_ = -1;
                reorder_buf_.clear();
                continue;
            }

            if (fpts == AV_NOPTS_VALUE) {
                // Untimestamped source: present sequentially (legacy behavior).
                scale_into(av_frame_, dest_buffer, target_w, target_h);
                last_presented_pts_ns_ = pts_ns;
                return true;
            }

            if (fpts <= last_presented_pts_ns_) continue; // duplicate/out-of-order behind the clock

            // Hold the decoded frame by refcount (no pixel copy) until it is
            // the oldest frame in the presentation window.
            AVFrame* held = av_frame_clone(av_frame_);
            if (!held) return false;

            auto it = std::lower_bound(reorder_buf_.begin(), reorder_buf_.end(), fpts,
                [](const ReorderFrame& a, int64_t pts) { return a.pts_ns < pts; });
            reorder_buf_.insert(it, ReorderFrame(fpts, held));
        }
    }
}

void VideoSource::seek(int64_t pts_ns) {
    if (!fmt_ctx_) return;
    int64_t target_ts = av_rescale_q(pts_ns / 1000, {1, 1000000}, fmt_ctx_->streams[video_stream_idx_]->time_base);
    av_seek_frame(fmt_ctx_, video_stream_idx_, target_ts, AVSEEK_FLAG_BACKWARD);
    if (codec_ctx_) {
        avcodec_flush_buffers(codec_ctx_);
        decoder_flushed_ = false;
    }
}

bool VideoSource::fill_audio_fifo(size_t max_samples, int64_t content_target) {
    const size_t channels = static_cast<size_t>(audio_output_channels_);
    const size_t need = max_samples * channels;
    const int64_t kFrameSlackNs =
        (static_cast<int64_t>(max_samples) * 1000000000LL) / audio_output_sample_rate_;
    bool wrapped = false;

    while (audio_fifo_.size() - audio_fifo_pos_ < need) {
        int ret = av_read_frame(audio_fmt_ctx_, audio_pkt_);
        if (ret < 0) {
            if (ret == AVERROR_EOF) {
                // Loop: wrap to the position matching the global clock.
                if (audio_duration_ns_ <= 0 || wrapped) return false;
                wrapped = true;
                int64_t target_ts = av_rescale_q(content_target / 1000, {1, 1000000},
                                                 audio_fmt_ctx_->streams[audio_stream_idx_]->time_base);
                av_seek_frame(audio_fmt_ctx_, audio_stream_idx_, target_ts, AVSEEK_FLAG_BACKWARD);
                avcodec_flush_buffers(audio_codec_ctx_);
                last_audio_pts_ns_ = -1;
                continue;
            }
            return false;
        }

        if (audio_pkt_->stream_index != audio_stream_idx_) {
            av_packet_unref(audio_pkt_);
            continue;
        }

        int sret = avcodec_send_packet(audio_codec_ctx_, audio_pkt_);
        if (sret == AVERROR(EAGAIN)) {
            // Decoder full: drain one frame, then retry the same packet.
            if (avcodec_receive_frame(audio_codec_ctx_, audio_frame_) == 0) {
                av_packet_unref(audio_pkt_);
                continue;
            }
            sret = avcodec_send_packet(audio_codec_ctx_, audio_pkt_);
        }
        av_packet_unref(audio_pkt_);
        if (sret < 0) return false;

        while (avcodec_receive_frame(audio_codec_ctx_, audio_frame_) == 0) {
            const int64_t in_rate = audio_frame_->sample_rate > 0
                                        ? audio_frame_->sample_rate
                                        : audio_output_sample_rate_;
            int64_t fpts_ns = audio_frame_->pts;
            if (fpts_ns == AV_NOPTS_VALUE) {
                fpts_ns = last_audio_pts_ns_ + 1;
            } else {
                fpts_ns = av_rescale_q(fpts_ns,
                                       audio_fmt_ctx_->streams[audio_stream_idx_]->time_base,
                                       {1, 1000000000});
            }

            const int64_t chunk_dur_ns =
                (static_cast<int64_t>(audio_frame_->nb_samples) * 1000000000LL) / in_rate;

            // Drop chunks entirely before the requested time (with one frame of slack).
            if (fpts_ns + chunk_dur_ns < content_target - kFrameSlackNs) {
                last_audio_pts_ns_ = fpts_ns;
                continue;
            }

            int out_nb = av_rescale_rnd(
                swr_get_delay(swr_ctx_, in_rate) + audio_frame_->nb_samples,
                audio_output_sample_rate_, in_rate, AV_ROUND_UP);
            if (out_nb <= 0) continue;

            std::vector<int16_t> tmp(static_cast<size_t>(out_nb) * channels);
            uint8_t* out_planes[1] = { reinterpret_cast<uint8_t*>(tmp.data()) };
            int converted = swr_convert(swr_ctx_, out_planes, out_nb,
                                        const_cast<const uint8_t**>(audio_frame_->extended_data),
                                        audio_frame_->nb_samples);
            if (converted > 0) {
                audio_fifo_.insert(audio_fifo_.end(), tmp.begin(),
                                   tmp.begin() + static_cast<ptrdiff_t>(converted) * channels);
            }
            last_audio_pts_ns_ = fpts_ns;
        }
    }
    return true;
}

bool VideoSource::read_audio_s16(int16_t* dest, size_t max_samples, int64_t pts_ns, size_t& samples_out) {
    samples_out = 0;
    if (!dest || max_samples == 0 || !audio_fmt_ctx_ || !audio_codec_ctx_ || !swr_ctx_) return false;

    const int64_t kSyncToleranceNs = 5 * 1000000LL; // 5 ms
    const size_t channels = static_cast<size_t>(audio_output_channels_);

    // Same loop mapping as the video path: for a looping source, compare and
    // seek against (time mod duration).
    const int64_t content_target =
        (audio_duration_ns_ > 0) ? pts_ns % audio_duration_ns_ : pts_ns;

    // Backward jump: resync audio to the requested time.
    if (last_audio_pts_ns_ > content_target + kSyncToleranceNs) {
        int64_t target_ts = av_rescale_q(content_target / 1000, {1, 1000000},
                                         audio_fmt_ctx_->streams[audio_stream_idx_]->time_base);
        av_seek_frame(audio_fmt_ctx_, audio_stream_idx_, target_ts, AVSEEK_FLAG_BACKWARD);
        avcodec_flush_buffers(audio_codec_ctx_);
        audio_fifo_.clear();
        audio_fifo_pos_ = 0;
        last_audio_pts_ns_ = content_target - kSyncToleranceNs; // allow a small pre-roll
    }

    if (!fill_audio_fifo(max_samples, content_target)) return false;

    const size_t avail = audio_fifo_.size() - audio_fifo_pos_;
    const size_t take = std::min(max_samples, avail / channels) * channels;
    if (take > 0) {
        std::memcpy(dest, audio_fifo_.data() + audio_fifo_pos_, take * sizeof(int16_t));
        audio_fifo_pos_ += take;
    }

    samples_out = take / channels;

    // Compact the fifo when fully consumed.
    if (audio_fifo_pos_ >= audio_fifo_.size()) {
        audio_fifo_.clear();
        audio_fifo_pos_ = 0;
    }

    if (take > 0) last_audio_pts_ns_ = pts_ns;
    return true;
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
