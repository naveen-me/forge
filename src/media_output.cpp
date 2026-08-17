#include "media_output.h"
#include "logger.h"
#include <iostream>
#include <cstring>

namespace tarva {

MediaOutput::MediaOutput(int width, int height, int fps)
    : width_(width), height_(height), fps_(fps) {
    rgba_frame_ = av_frame_alloc();
    yuv_frame_ = av_frame_alloc();
    pkt_ = av_packet_alloc();
}

MediaOutput::~MediaOutput() {
    finalize();
    if (rgba_frame_) av_frame_free(&rgba_frame_);
    if (yuv_frame_) av_frame_free(&yuv_frame_);
    if (pkt_) av_packet_free(&pkt_);
    if (audio_out_frame_) av_frame_free(&audio_out_frame_);
    if (audio_pkt_) av_packet_free(&audio_pkt_);
    if (audio_codec_ctx_) avcodec_free_context(&audio_codec_ctx_);
    if (audio_swr_ctx_) swr_free(&audio_swr_ctx_);
}

bool MediaOutput::initialize(const std::string& destination_url, bool with_audio,
                             int audio_sample_rate, int audio_channels) {
    std::lock_guard<std::mutex> lock(output_mutex_);
    destination_url_ = destination_url;

    bool is_rtmp = (destination_url.rfind("rtmp://", 0) == 0 || destination_url.rfind("rtsp://", 0) == 0);
    const char* format_name = is_rtmp ? "flv" : nullptr;

    if (avformat_alloc_output_context2(&fmt_ctx_, nullptr, format_name, destination_url.c_str()) < 0 || !fmt_ctx_) {
        LOG_ERROR("MediaOutput: Failed to allocate output context for " + destination_url);
        return false;
    }

    const AVCodec* codec = avcodec_find_encoder(AV_CODEC_ID_H264);
    if (!codec) {
        LOG_ERROR("MediaOutput: H.264 encoder not found");
        return false;
    }

    stream_ = avformat_new_stream(fmt_ctx_, codec);
    if (!stream_) {
        LOG_ERROR("MediaOutput: Failed to create output stream");
        return false;
    }

    codec_ctx_ = avcodec_alloc_context3(codec);
    codec_ctx_->width = width_;
    codec_ctx_->height = height_;
    codec_ctx_->time_base = {1, fps_};
    codec_ctx_->framerate = {fps_, 1};
    codec_ctx_->pix_fmt = AV_PIX_FMT_YUV420P;
    codec_ctx_->gop_size = fps_ * 2; // Keyframe every 2 seconds
    codec_ctx_->max_b_frames = 0;
    codec_ctx_->thread_count = 2;

    // Fast CPU encoding preset
    av_opt_set(codec_ctx_->priv_data, "preset", "ultrafast", 0);
    av_opt_set(codec_ctx_->priv_data, "tune", "zerolatency", 0);

    if (fmt_ctx_->oformat->flags & AVFMT_GLOBALHEADER) {
        codec_ctx_->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
    }

    if (avcodec_open2(codec_ctx_, codec, nullptr) < 0) {
        LOG_ERROR("MediaOutput: Failed to open H.264 codec");
        return false;
    }

    if (avcodec_parameters_from_context(stream_->codecpar, codec_ctx_) < 0) {
        LOG_ERROR("MediaOutput: Failed to copy codec params to stream");
        return false;
    }

    stream_->time_base = codec_ctx_->time_base;

    // Optional AAC audio stream (must be added before the header is written).
    if (with_audio) {
        setup_audio_stream(audio_sample_rate, audio_channels);
    }

    if (!(fmt_ctx_->oformat->flags & AVFMT_NOFILE)) {
        if (avio_open(&fmt_ctx_->pb, destination_url.c_str(), AVIO_FLAG_WRITE) < 0) {
            LOG_ERROR("MediaOutput: Failed to open output destination file/URL: " + destination_url);
            return false;
        }
    }

    if (avformat_write_header(fmt_ctx_, nullptr) < 0) {
        LOG_ERROR("MediaOutput: Failed to write output header");
        return false;
    }

    // Allocate YUV420P frame buffer for encoder input
    yuv_frame_->format = AV_PIX_FMT_YUV420P;
    yuv_frame_->width = width_;
    yuv_frame_->height = height_;
    av_frame_get_buffer(yuv_frame_, 0);

    // Setup SwsContext for RGBA -> YUV420P conversion
    sws_ctx_ = sws_getContext(
        width_, height_, AV_PIX_FMT_RGBA,
        width_, height_, AV_PIX_FMT_YUV420P,
        SWS_BILINEAR, nullptr, nullptr, nullptr
    );

    initialized_ = true;
    LOG_INFO("MediaOutput initialized successfully for: " + destination_url +
             " (" + std::to_string(width_) + "x" + std::to_string(height_) + " @" + std::to_string(fps_) + "fps)");
    return true;
}

bool MediaOutput::setup_audio_stream(int sample_rate, int channels) {
    audio_sample_rate_ = sample_rate > 0 ? sample_rate : 48000;
    audio_channels_ = channels > 0 ? channels : 2;

    const AVCodec* acodec = avcodec_find_encoder(AV_CODEC_ID_AAC);
    if (!acodec) {
        LOG_ERROR("MediaOutput: AAC encoder not found; audio stream disabled");
        return false;
    }

    audio_stream_ = avformat_new_stream(fmt_ctx_, acodec);
    if (!audio_stream_) {
        LOG_ERROR("MediaOutput: Failed to create audio output stream");
        return false;
    }

    audio_codec_ctx_ = avcodec_alloc_context3(acodec);
    audio_codec_ctx_->sample_rate = audio_sample_rate_;
    audio_codec_ctx_->bit_rate = 128000;
    audio_codec_ctx_->sample_fmt = AV_SAMPLE_FMT_FLTP;
    av_channel_layout_default(&audio_codec_ctx_->ch_layout, audio_channels_);
    audio_codec_ctx_->time_base = {1, audio_sample_rate_};

    if (fmt_ctx_->oformat->flags & AVFMT_GLOBALHEADER) {
        audio_codec_ctx_->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
    }

    if (avcodec_open2(audio_codec_ctx_, acodec, nullptr) < 0) {
        LOG_ERROR("MediaOutput: Failed to open AAC codec");
        return false;
    }

    if (avcodec_parameters_from_context(audio_stream_->codecpar, audio_codec_ctx_) < 0) {
        LOG_ERROR("MediaOutput: Failed to copy audio codec params to stream");
        return false;
    }
    audio_stream_->time_base = audio_codec_ctx_->time_base;

    audio_frame_size_ = audio_codec_ctx_->frame_size > 0 ? audio_codec_ctx_->frame_size : 1024;

    // Resampler: packed S16 interleaved -> FLTP planar (AAC input format).
    AVChannelLayout in_layout;
    av_channel_layout_default(&in_layout, audio_channels_);
    int sret = swr_alloc_set_opts2(
        &audio_swr_ctx_, &audio_codec_ctx_->ch_layout, AV_SAMPLE_FMT_FLTP, audio_sample_rate_,
        &in_layout, AV_SAMPLE_FMT_S16, audio_sample_rate_, 0, nullptr);
    av_channel_layout_uninit(&in_layout);
    if (sret < 0 || swr_init(audio_swr_ctx_) < 0) {
        LOG_ERROR("MediaOutput: Failed to init audio resampler");
        return false;
    }

    audio_out_frame_ = av_frame_alloc();
    audio_out_frame_->format = AV_SAMPLE_FMT_FLTP;
    audio_out_frame_->sample_rate = audio_sample_rate_;
    av_channel_layout_copy(&audio_out_frame_->ch_layout, &audio_codec_ctx_->ch_layout);
    audio_out_frame_->nb_samples = audio_frame_size_;
    if (av_frame_get_buffer(audio_out_frame_, 0) < 0) {
        LOG_ERROR("MediaOutput: Failed to allocate audio frame buffer");
        return false;
    }

    audio_pkt_ = av_packet_alloc();
    audio_initialized_ = true;
    LOG_INFO("MediaOutput audio stream ready (AAC " + std::to_string(audio_sample_rate_) +
             " Hz, " + std::to_string(audio_channels_) + " ch, frame size " +
             std::to_string(audio_frame_size_) + ")");
    return true;
}

bool MediaOutput::send_frame_rgba(const uint8_t* rgba_buffer, int64_t frame_index) {
    if (!initialized_ || !rgba_buffer) return false;

    std::lock_guard<std::mutex> lock(output_mutex_);

    av_frame_make_writable(yuv_frame_);

    const uint8_t* src_slices[1] = { rgba_buffer };
    int src_stride[1] = { width_ * 4 };

    sws_scale(sws_ctx_, src_slices, src_stride, 0, height_,
              yuv_frame_->data, yuv_frame_->linesize);

    yuv_frame_->pts = frame_index;

    if (avcodec_send_frame(codec_ctx_, yuv_frame_) < 0) {
        LOG_ERROR("MediaOutput: Error sending frame to H.264 encoder");
        return false;
    }

    while (avcodec_receive_packet(codec_ctx_, pkt_) == 0) {
        av_packet_rescale_ts(pkt_, codec_ctx_->time_base, stream_->time_base);
        pkt_->stream_index = stream_->index;

        if (av_interleaved_write_frame(fmt_ctx_, pkt_) < 0) {
            LOG_ERROR("MediaOutput: Error writing encoded frame to output stream");
            av_packet_unref(pkt_);
            return false;
        }
        av_packet_unref(pkt_);
    }

    frames_sent_++;
    return true;
}

bool MediaOutput::send_audio_s16(const int16_t* samples, size_t sample_frames, int64_t pts_ns) {
    if (!audio_initialized_ || !samples || sample_frames == 0) return false;

    std::lock_guard<std::mutex> lock(output_mutex_);
    if (!audio_initialized_) return false;

    const size_t channels = static_cast<size_t>(audio_channels_);

    // Queue the input. The fifo is fully drained below, so it is bounded by
    // one encoder frame_size chunk plus this call's samples.
    audio_fifo_.insert(audio_fifo_.end(), samples, samples + sample_frames * channels);

    while (audio_fifo_.size() - audio_fifo_pos_ >= static_cast<size_t>(audio_frame_size_) * channels) {
        av_frame_make_writable(audio_out_frame_);
        audio_out_frame_->nb_samples = audio_frame_size_;
        audio_out_frame_->pts = audio_samples_sent_;

        const uint8_t* in_data[1] = {
            reinterpret_cast<const uint8_t*>(audio_fifo_.data() + audio_fifo_pos_)
        };
        int converted = swr_convert(audio_swr_ctx_, audio_out_frame_->data, audio_frame_size_,
                                    in_data, audio_frame_size_);
        if (converted <= 0) {
            LOG_ERROR("MediaOutput: Audio resample failed");
            return false;
        }

        if (avcodec_send_frame(audio_codec_ctx_, audio_out_frame_) < 0) {
            LOG_ERROR("MediaOutput: Error sending audio frame to AAC encoder");
            return false;
        }

        while (avcodec_receive_packet(audio_codec_ctx_, audio_pkt_) == 0) {
            av_packet_rescale_ts(audio_pkt_, audio_codec_ctx_->time_base, audio_stream_->time_base);
            audio_pkt_->stream_index = audio_stream_->index;
            if (av_interleaved_write_frame(fmt_ctx_, audio_pkt_) < 0) {
                LOG_ERROR("MediaOutput: Error writing audio packet");
                av_packet_unref(audio_pkt_);
                return false;
            }
            av_packet_unref(audio_pkt_);
        }

        audio_samples_sent_ += audio_frame_size_;
        audio_fifo_pos_ += static_cast<size_t>(audio_frame_size_) * channels;
    }

    // Compact the consumed portion of the fifo.
    if (audio_fifo_pos_ > 0) {
        audio_fifo_.erase(audio_fifo_.begin(), audio_fifo_.begin() + static_cast<ptrdiff_t>(audio_fifo_pos_));
        audio_fifo_pos_ = 0;
    }

    (void)pts_ns; // audio timing is sample-accurate via audio_samples_sent_
    return true;
}

void MediaOutput::flush_audio_encoder() {
    if (!audio_initialized_ || !audio_codec_ctx_) return;

    if (avcodec_send_frame(audio_codec_ctx_, nullptr) >= 0) {
        while (avcodec_receive_packet(audio_codec_ctx_, audio_pkt_) == 0) {
            av_packet_rescale_ts(audio_pkt_, audio_codec_ctx_->time_base, audio_stream_->time_base);
            audio_pkt_->stream_index = audio_stream_->index;
            av_interleaved_write_frame(fmt_ctx_, audio_pkt_);
            av_packet_unref(audio_pkt_);
        }
    }
}

void MediaOutput::finalize() {
    std::lock_guard<std::mutex> lock(output_mutex_);
    if (!initialized_) return;

    // Flush encoders
    if (codec_ctx_) {
        avcodec_send_frame(codec_ctx_, nullptr);
        while (avcodec_receive_packet(codec_ctx_, pkt_) == 0) {
            av_packet_rescale_ts(pkt_, codec_ctx_->time_base, stream_->time_base);
            pkt_->stream_index = stream_->index;
            av_interleaved_write_frame(fmt_ctx_, pkt_);
            av_packet_unref(pkt_);
        }
    }
    flush_audio_encoder();

    if (fmt_ctx_) {
        av_write_trailer(fmt_ctx_);
        if (!(fmt_ctx_->oformat->flags & AVFMT_NOFILE) && fmt_ctx_->pb) {
            avio_closep(&fmt_ctx_->pb);
        }
        avformat_free_context(fmt_ctx_);
        fmt_ctx_ = nullptr;
    }

    if (codec_ctx_) {
        avcodec_free_context(&codec_ctx_);
        codec_ctx_ = nullptr;
    }

    if (sws_ctx_) {
        sws_freeContext(sws_ctx_);
        sws_ctx_ = nullptr;
    }

    if (audio_codec_ctx_) {
        avcodec_free_context(&audio_codec_ctx_);
        audio_codec_ctx_ = nullptr;
    }
    if (audio_swr_ctx_) {
        swr_free(&audio_swr_ctx_);
        audio_swr_ctx_ = nullptr;
    }
    if (audio_out_frame_) {
        av_frame_free(&audio_out_frame_);
        audio_out_frame_ = nullptr;
    }
    if (audio_pkt_) {
        av_packet_free(&audio_pkt_);
        audio_pkt_ = nullptr;
    }
    audio_initialized_ = false;

    initialized_ = false;
    LOG_INFO("MediaOutput finalized. Total frames sent: " + std::to_string(frames_sent_) +
             ", audio samples sent: " + std::to_string(audio_samples_sent_));
}

} // namespace tarva
