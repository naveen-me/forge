#include "source_manager.h"
#include "logger.h"
#include <cassert>
#include <iostream>
#include <cstdlib>

void generate_test_media() {
    std::system("ffmpeg -y -f lavfi -i color=c=red:s=320x240:d=1 -vframes 1 /tmp/sm_test.png > /dev/null 2>&1");
}

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::DEBUG);
    LOG_INFO("Testing SourceManager...");

    generate_test_media();

    tarva::SourceManager manager;

    // 1. Protocol capabilities check
    assert(manager.supports_protocol("/media/file.mp4"));
    assert(manager.supports_protocol("http://example.com/video.mp4"));
    assert(manager.supports_protocol("https://example.com/stream.m3u8"));
    assert(manager.supports_protocol("srt://127.0.0.1:9000"));
    assert(!manager.supports_protocol("ftp://invalid.example.com/file"));

    // 2. Prepare valid image source
    tarva::Layer valid_layer;
    valid_layer.id = "img1";
    valid_layer.type = "image";
    valid_layer.source = "/tmp/sm_test.png";
    valid_layer.width = 320;
    valid_layer.height = 240;

    auto handle1 = manager.prepare_source(valid_layer, 1920, 1080);
    assert(handle1 != nullptr);
    assert(handle1->state == tarva::SourceState::READY);
    assert(handle1->media_source != nullptr);

    // 3. Prepare invalid/broken source - must report ERROR state without crashing
    tarva::Layer broken_layer;
    broken_layer.id = "broken1";
    broken_layer.type = "video";
    broken_layer.source = "/non_existent_file.mp4";

    auto handle2 = manager.prepare_source(broken_layer, 1920, 1080);
    assert(handle2 != nullptr);
    assert(handle2->state == tarva::SourceState::ERROR);
    assert(!handle2->error_message.empty());

    // 4. List sources
    auto all_sources = manager.list_sources();
    assert(all_sources.size() == 2);

    LOG_INFO("All SourceManager tests passed successfully!");
    return 0;
}
