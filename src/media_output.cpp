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
}

bool MediaOutput::initialize(const std::string& destination_url) {
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

void MediaOutput::finalize() {
    std::lock_guard<std::mutex> lock(output_mutex_);
    if (!initialized_) return;

    // Flush encoder
    if (codec_ctx_) {
        avcodec_send_frame(codec_ctx_, nullptr);
        while (avcodec_receive_packet(codec_ctx_, pkt_) == 0) {
            av_packet_rescale_ts(pkt_, codec_ctx_->time_base, stream_->time_base);
            pkt_->stream_index = stream_->index;
            av_interleaved_write_frame(fmt_ctx_, pkt_);
            av_packet_unref(pkt_);
        }
    }

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

    initialized_ = false;
    LOG_INFO("MediaOutput finalized. Total frames sent: " + std::to_string(frames_sent_));
}

} // namespace tarva
