#ifndef TARVA_MEDIA_SOURCES_H
#define TARVA_MEDIA_SOURCES_H

#include <string>
#include <vector>
#include <deque>
#include <memory>
#include <mutex>
#include <cstdint>

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
#include <libswresample/swresample.h>
#include <libavutil/imgutils.h>
#include <libavutil/channel_layout.h>
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

    // --- audio (present only when the source file carries an audio stream) ---
    bool has_audio() const { return audio_stream_idx_ >= 0; }

    // Reads up to max_samples interleaved S16 stereo samples whose playout
    // position corresponds to pts_ns. samples_out receives the number of
    // sample frames produced (may be < max_samples at end-of-stream).
    bool read_audio_s16(int16_t* dest, size_t max_samples, int64_t pts_ns, size_t& samples_out);

    int audio_sample_rate() const { return audio_output_sample_rate_; }
    int audio_channels() const { return audio_output_channels_; }

private:
    // Decodes until the next video frame is available (looping at EOF) and
    // returns its pts in ns, or AV_NOPTS_VALUE for untimestamped sources.
    int64_t decode_next_video_frame();

    // Scales src into an RGBA destination buffer (w*h*4).
    void scale_into(AVFrame* src, uint8_t* dest, int w, int h);

    // Decodes audio packets until audio_fifo_ holds at least max_samples,
    // skipping chunks entirely before pts_ns. Returns false on hard error.
    bool fill_audio_fifo(size_t max_samples, int64_t pts_ns);

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

    // --- pts-aligned video presentation state ---
    // Bounded presentation-order reorder buffer (see read_frame_rgba). Each
    // entry holds a refcounted AVFrame (av_frame_ref) so holding decoded
    // frames costs no pixel copies; the frame is scaled into the caller's
    // buffer only at presentation time.
    struct ReorderFrame {
        int64_t pts_ns = 0;
        AVFrame* frame = nullptr;
        ReorderFrame() = default;
        ReorderFrame(int64_t pts, AVFrame* f) : pts_ns(pts), frame(f) {}
        ~ReorderFrame() { if (frame) av_frame_free(&frame); }
        ReorderFrame(const ReorderFrame&) = delete;
        ReorderFrame& operator=(const ReorderFrame&) = delete;
        ReorderFrame(ReorderFrame&& o) noexcept : pts_ns(o.pts_ns), frame(o.frame) { o.frame = nullptr; }
        ReorderFrame& operator=(ReorderFrame&& o) noexcept {
            if (frame) av_frame_free(&frame);
            pts_ns = o.pts_ns;
            frame = o.frame;
            o.frame = nullptr;
            return *this;
        }
    };
    int64_t last_presented_pts_ns_ = -1;
    std::deque<ReorderFrame> reorder_buf_;
    size_t reorder_capacity_ = 2;
    bool decoder_flushed_ = false; // set after EOF flush; reset on seek

    // --- audio state (independent demux context; the video demuxer drops
    //     audio packets so audio needs its own reader to stay isolated) ---
    AVFormatContext* audio_fmt_ctx_ = nullptr;
    AVCodecContext* audio_codec_ctx_ = nullptr;
    SwrContext* swr_ctx_ = nullptr;
    AVPacket* audio_pkt_ = nullptr;
    AVFrame* audio_frame_ = nullptr;
    int audio_stream_idx_ = -1;
    int audio_output_sample_rate_ = 48000;
    int audio_output_channels_ = 2;

    std::vector<int16_t> audio_fifo_; // interleaved S16 stereo
    size_t audio_fifo_pos_ = 0;       // read cursor into audio_fifo_
    int64_t last_audio_pts_ns_ = -1;
    int64_t audio_duration_ns_ = 0;
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
