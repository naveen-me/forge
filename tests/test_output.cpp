#include "media_output.h"
#include "benchmark_harness.h"
#include "logger.h"
#include <cassert>
#include <iostream>
#include <vector>
#include <fstream>

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::DEBUG);
    LOG_INFO("Testing MediaOutput and BenchmarkHarness...");

    int width = 1920;
    int height = 1080;
    int fps = 30;
    std::string output_path = "/tmp/test_output.mp4";

    tarva::MediaOutput output(width, height, fps);
    bool ok = output.initialize(output_path);
    assert(ok);

    tarva::BenchmarkHarness harness(width, height, fps);
    harness.start();

    std::vector<uint8_t> frame_buf(width * height * 4, 0);

    // Generate 30 test frames (1 second of video)
    for (int i = 0; i < 30; ++i) {
        auto t0 = std::chrono::steady_clock::now();

        // Fill test frame with shifting color
        uint8_t color_val = static_cast<uint8_t>((i * 8) % 256);
        for (int p = 0; p < width * height; ++p) {
            frame_buf[p * 4 + 0] = color_val;
            frame_buf[p * 4 + 1] = 128;
            frame_buf[p * 4 + 2] = 200;
            frame_buf[p * 4 + 3] = 255;
        }

        ok = output.send_frame_rgba(frame_buf.data(), i);
        assert(ok);

        auto t1 = std::chrono::steady_clock::now();
        double frame_ms = std::chrono::duration_cast<std::chrono::microseconds>(t1 - t0).count() / 1000.0;
        harness.record_frame_time(frame_ms);
    }

    harness.stop();
    output.finalize();

    assert(output.frames_sent() == 30);

    // Verify output MP4 file exists and is non-empty
    std::ifstream ifs(output_path, std::ios::binary | std::ios::ate);
    assert(ifs.is_open());
    assert(ifs.tellg() > 1000);

    // Save benchmark report JSON
    std::string report_path = "/tmp/test_benchmark_report.json";
    harness.save_report_json(report_path);

    std::ifstream rfs(report_path);
    assert(rfs.is_open());

    LOG_INFO("MediaOutput and BenchmarkHarness test passed successfully!");
    return 0;
}
