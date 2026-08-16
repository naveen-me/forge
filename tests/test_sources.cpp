#include "media_sources.h"
#include "logger.h"
#include <cassert>
#include <iostream>
#include <vector>
#include <cstdlib>

void generate_test_media() {
    // Generate test PNG using ffmpeg
    std::system("ffmpeg -y -f lavfi -i color=c=blue:s=320x240:d=1 -vframes 1 /tmp/test_logo.png > /dev/null 2>&1");
    // Generate test MP4 video using ffmpeg
    std::system("ffmpeg -y -f lavfi -i testsrc=size=640x360:rate=30 -t 2 /tmp/test_video.mp4 > /dev/null 2>&1");
    // Generate test HLS playlist using ffmpeg
    std::system("ffmpeg -y -f lavfi -i testsrc=size=640x360:rate=30 -t 3 -hls_time 1 -hls_list_size 5 /tmp/test_hls.m3u8 > /dev/null 2>&1");
}

void test_image_source() {
    tarva::ImageSource img_source;
    bool ok = img_source.load("/tmp/test_logo.png", 320, 240);
    assert(ok);
    assert(img_source.width() == 320);
    assert(img_source.height() == 240);

    std::vector<uint8_t> frame(320 * 240 * 4, 0);
    ok = img_source.read_frame_rgba(frame.data(), 320, 240, 0);
    assert(ok);

    // Verify pixel is blue (b=255, r=0, g=0)
    uint8_t r = frame[0];
    uint8_t g = frame[1];
    uint8_t b = frame[2];
    assert(b > 200 && r < 50);

    LOG_INFO("ImageSource test passed.");
}

void test_text_source() {
    tarva::TextSource text_source("Hello Playout!", 36, "#FFFFFF");
    bool ok = text_source.load("", 400, 100);
    assert(ok);

    std::vector<uint8_t> frame(400 * 100 * 4, 0);
    ok = text_source.read_frame_rgba(frame.data(), 400, 100, 0);
    assert(ok);

    LOG_INFO("TextSource test passed.");
}

void test_video_source() {
    tarva::VideoSource video_source;
    bool ok = video_source.load("/tmp/test_video.mp4", 640, 360);
    assert(ok);
    assert(video_source.width() == 640);
    assert(video_source.height() == 360);

    std::vector<uint8_t> frame(640 * 360 * 4, 0);
    ok = video_source.read_frame_rgba(frame.data(), 640, 360, 0);
    assert(ok);

    LOG_INFO("VideoSource test passed.");
}

void test_hls_source() {
    tarva::VideoSource hls_source;
    bool ok = hls_source.load("/tmp/test_hls.m3u8", 640, 360);
    assert(ok);

    std::vector<uint8_t> frame(640 * 360 * 4, 0);
    ok = hls_source.read_frame_rgba(frame.data(), 640, 360, 0);
    assert(ok);

    LOG_INFO("HLS .m3u8 VideoSource integration test passed successfully!");
}

void test_html_source() {
    tarva::HtmlSource html_source;
    std::string html = "<html style='background: green;'><body><h1>TARVA</h1></body></html>";
    bool ok = html_source.load(html, 640, 360);
    assert(ok);

    std::vector<uint8_t> frame(640 * 360 * 4, 0);
    ok = html_source.read_frame_rgba(frame.data(), 640, 360, 0);
    assert(ok);

    LOG_INFO("HtmlSource test passed.");
}

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::DEBUG);
    LOG_INFO("Testing MediaSource decoders...");

    generate_test_media();
    test_image_source();
    test_text_source();
    test_video_source();
    test_hls_source();
    test_html_source();

    LOG_INFO("All MediaSource tests passed successfully!");
    return 0;
}
