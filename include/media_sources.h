#ifndef TARVA_MEDIA_SOURCES_H
#define TARVA_MEDIA_SOURCES_H

#include <string>
#include <vector>
#include <memory>
#include <mutex>
#include <cstdint>

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
}

#include <cairo.h>
#include "wpe_html_renderer.h"

namespace tarva {

class MediaSource {
public:
    virtual ~MediaSource() = default;
    virtual bool load(const std::string& source, int target_w, int target_h) = 0;
    virtual bool read_frame_rgba(uint8_t* dest_buffer, int target_w, int target_h, int64_t pts_ns) = 0;
    virtual int width() const = 0;
    virtual int height() const = 0;
    virtual bool is_eof() const { return eof_; }
    virtual void seek(int64_t pts_ns) {}

protected:
    bool eof_ = false;
};

// --- VideoSource using FFmpeg ---
class VideoSource : public MediaSource {
public:
    VideoSource();
    ~VideoSource() override;

    bool load(const std::string& filepath, int target_w, int target_h) override;
    bool read_frame_rgba(uint8_t* dest_buffer, int target_w, int target_h, int64_t pts_ns) override;
    int width() const override { return src_width_; }
    int height() const override { return src_height_; }
    void seek(int64_t pts_ns) override;

private:
    AVFormatContext* fmt_ctx_ = nullptr;
    AVCodecContext* codec_ctx_ = nullptr;
    SwsContext* sws_ctx_ = nullptr;
    int video_stream_idx_ = -1;

    AVFrame* av_frame_ = nullptr;
    AVFrame* rgba_frame_ = nullptr;
    AVPacket* av_pkt_ = nullptr;

    int src_width_ = 0;
    int src_height_ = 0;
    int target_w_ = 0;
    int target_h_ = 0;
    int64_t duration_ns_ = 0;
    bool loop_ = false;
};

// --- ImageSource using Cairo ---
class ImageSource : public MediaSource {
public:
    ImageSource();
    ~ImageSource() override;

    bool load(const std::string& filepath, int target_w, int target_h) override;
    bool read_frame_rgba(uint8_t* dest_buffer, int target_w, int target_h, int64_t pts_ns) override;
    int width() const override { return width_; }
    int height() const override { return height_; }

private:
    int width_ = 0;
    int height_ = 0;
    cairo_surface_t* surface_ = nullptr;
};

// --- TextSource using Cairo ---
class TextSource : public MediaSource {
public:
    TextSource(const std::string& text, int font_size = 32, const std::string& color = "#FFFFFF", const std::string& bg_color = "");
    ~TextSource() override = default;

    bool load(const std::string& dummy_source, int target_w, int target_h) override;
    bool read_frame_rgba(uint8_t* dest_buffer, int target_w, int target_h, int64_t pts_ns) override;
    int width() const override { return width_; }
    int height() const override { return height_; }

    void set_text(const std::string& text) { text_ = text; }

private:
    std::string text_;
    int font_size_ = 32;
    std::string color_ = "#FFFFFF";
    std::string bg_color_;
    int width_ = 400;
    int height_ = 100;
};

// --- HtmlSource wrapping WpeHtmlRenderer ---
class HtmlSource : public MediaSource {
public:
    HtmlSource();
    ~HtmlSource() override = default;

    bool load(const std::string& url_or_html, int target_w, int target_h) override;
    bool read_frame_rgba(uint8_t* dest_buffer, int target_w, int target_h, int64_t pts_ns) override;
    int width() const override { return width_; }
    int height() const override { return height_; }

private:
    int width_ = 1920;
    int height_ = 1080;
    std::unique_ptr<WpeHtmlRenderer> renderer_;
    std::vector<uint8_t> cached_rgba_buffer_;
    bool has_cached_frame_ = false;
};

} // namespace tarva

#endif // TARVA_MEDIA_SOURCES_H
