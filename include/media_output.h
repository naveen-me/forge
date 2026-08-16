#ifndef TARVA_MEDIA_OUTPUT_H
#define TARVA_MEDIA_OUTPUT_H

#include <string>
#include <mutex>
#include <cstdint>

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
#include <libavutil/opt.h>
}

namespace tarva {

class MediaOutput {
public:
    MediaOutput(int width = 1920, int height = 1080, int fps = 30);
    ~MediaOutput();

    bool initialize(const std::string& destination_url);
    bool send_frame_rgba(const uint8_t* rgba_buffer, int64_t frame_index);
    void finalize();

    bool is_initialized() const { return initialized_; }
    int64_t frames_sent() const { return frames_sent_; }

private:
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

    std::mutex output_mutex_;
};

} // namespace tarva

#endif // TARVA_MEDIA_OUTPUT_H
