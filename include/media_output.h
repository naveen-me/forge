#ifndef TARVA_MEDIA_OUTPUT_H
#define TARVA_MEDIA_OUTPUT_H

#include <string>
#include <mutex>
#include <vector>
#include <cstdint>

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
#include <libswresample/swresample.h>
#include <libavutil/imgutils.h>
#include <libavutil/opt.h>
#include <libavutil/channel_layout.h>
}

namespace tarva {

class MediaOutput {
public:
    MediaOutput(int width = 1920, int height = 1080, int fps = 30);
    ~MediaOutput();

    // with_audio adds an AAC audio stream (S16 interleaved input). The audio
    // stream must be requested before any frames are sent (the container needs
    // all streams at header time).
    bool initialize(const std::string& destination_url,
                    bool with_audio = false,
                    int audio_sample_rate = 48000,
                    int audio_channels = 2);
    bool send_frame_rgba(const uint8_t* rgba_buffer, int64_t frame_index);

    // Queues interleaved S16 PCM; sample_frames is the number of frames per
    // channel. Encoded in bounded chunks (encoder frame_size) with monotonic
    // sample-accurate timestamps.
    bool send_audio_s16(const int16_t* samples, size_t sample_frames, int64_t pts_ns);

    void finalize();

    bool is_initialized() const { return initialized_; }
    bool has_audio() const { return audio_initialized_; }
    int64_t frames_sent() const { return frames_sent_; }
    int64_t audio_samples_sent() const { return audio_samples_sent_; }

private:
    bool setup_audio_stream(int sample_rate, int channels);
    void flush_audio_encoder();

    int width_ = 1920;
    int height_ = 1080;
    int fps_ = 30;
    std::string destination_url_;
    bool initialized_ = false;
    int64_t frames_sent_ = 0;

    AVFormatContext* fmt_ctx_ = nullptr;
    AVCodecContext* codec_ctx_ = nullptr;
    AVStream* stream_ = nullptr;
    SwsContext* sws_ctx_ = nullptr;

    AVFrame* rgba_frame_ = nullptr;
    AVFrame* yuv_frame_ = nullptr;
    AVPacket* pkt_ = nullptr;

    // --- audio ---
    bool audio_initialized_ = false;
    int audio_sample_rate_ = 48000;
    int audio_channels_ = 2;
    int audio_frame_size_ = 0;
    int64_t audio_samples_sent_ = 0;

    AVCodecContext* audio_codec_ctx_ = nullptr;
    AVStream* audio_stream_ = nullptr;
    SwrContext* audio_swr_ctx_ = nullptr;
    AVFrame* audio_out_frame_ = nullptr;
    AVPacket* audio_pkt_ = nullptr;

    std::vector<int16_t> audio_fifo_; // interleaved S16 input queue (bounded)
    size_t audio_fifo_pos_ = 0;

    std::mutex output_mutex_;
};

} // namespace tarva

#endif // TARVA_MEDIA_OUTPUT_H
