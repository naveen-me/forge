#ifndef TARVA_BENCHMARK_HARNESS_H
#define TARVA_BENCHMARK_HARNESS_H

#include <string>
#include <chrono>
#include <cstdint>
#include <nlohmann/json.hpp>

namespace tarva {

struct BenchmarkMetrics {
    int canvas_width = 1920;
    int canvas_height = 1080;
    int target_fps = 30;
    int64_t total_frames = 0;
    double elapsed_sec = 0.0;
    double rendered_fps = 0.0;
    double output_fps = 0.0;
    int64_t dropped_frames = 0;
    double cpu_usage_pct = 0.0;
    size_t ram_rss_mb = 0;
    double avg_frame_time_ms = 0.0;
};

class BenchmarkHarness {
public:
    BenchmarkHarness(int width = 1920, int height = 1080, int target_fps = 30);

    void start();
    void record_frame_time(double frame_time_ms);
    void record_dropped_frame();
    void stop();

    BenchmarkMetrics get_metrics() const;
    void save_report_json(const std::string& filepath) const;

private:
    int width_;
    int height_;
    int target_fps_;

    std::chrono::steady_clock::time_point start_time_;
    std::chrono::steady_clock::time_point stop_time_;

    int64_t frame_count_ = 0;
    int64_t dropped_count_ = 0;
    double total_frame_time_ms_ = 0.0;

    double measure_cpu_percent();
    size_t measure_ram_rss_mb();
};

} // namespace tarva

#endif // TARVA_BENCHMARK_HARNESS_H
