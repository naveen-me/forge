#include "benchmark_harness.h"
#include "logger.h"
#include <fstream>
#include <iostream>
#include <sstream>
#include <unistd.h>
#include <sys/resource.h>

namespace tarva {

BenchmarkHarness::BenchmarkHarness(int width, int height, int target_fps)
    : width_(width), height_(height), target_fps_(target_fps) {}

void BenchmarkHarness::start() {
    start_time_ = std::chrono::steady_clock::now();
    frame_count_ = 0;
    dropped_count_ = 0;
    total_frame_time_ms_ = 0.0;
    LOG_INFO("BenchmarkHarness started (" + std::to_string(width_) + "x" + std::to_string(height_) + " @" + std::to_string(target_fps_) + "fps)");
}

void BenchmarkHarness::record_frame_time(double frame_time_ms) {
    frame_count_++;
    total_frame_time_ms_ += frame_time_ms;
}

void BenchmarkHarness::record_dropped_frame() {
    dropped_count_++;
}

void BenchmarkHarness::stop() {
    stop_time_ = std::chrono::steady_clock::now();
}

BenchmarkMetrics BenchmarkHarness::get_metrics() const {
    auto end_time = (stop_time_ > start_time_) ? stop_time_ : std::chrono::steady_clock::now();
    double elapsed_sec = std::chrono::duration_cast<std::chrono::microseconds>(end_time - start_time_).count() / 1e6;
    if (elapsed_sec <= 0.0) elapsed_sec = 0.001;

    BenchmarkMetrics m;
    m.canvas_width = width_;
    m.canvas_height = height_;
    m.target_fps = target_fps_;
    m.total_frames = frame_count_;
    m.elapsed_sec = elapsed_sec;
    m.rendered_fps = frame_count_ / elapsed_sec;
    m.output_fps = frame_count_ / elapsed_sec;
    m.dropped_frames = dropped_count_;
    m.avg_frame_time_ms = (frame_count_ > 0) ? (total_frame_time_ms_ / frame_count_) : 0.0;

    const_cast<BenchmarkHarness*>(this)->measure_cpu_percent(); // populate
    m.cpu_usage_pct = const_cast<BenchmarkHarness*>(this)->measure_cpu_percent();
    m.ram_rss_mb = const_cast<BenchmarkHarness*>(this)->measure_ram_rss_mb();

    return m;
}

double BenchmarkHarness::measure_cpu_percent() {
    struct rusage usage;
    if (getrusage(RUSAGE_SELF, &usage) == 0) {
        double user_sec = usage.ru_utime.tv_sec + usage.ru_utime.tv_usec / 1e6;
        double sys_sec = usage.ru_stime.tv_sec + usage.ru_stime.tv_usec / 1e6;
        auto end_time = (stop_time_ > start_time_) ? stop_time_ : std::chrono::steady_clock::now();
        double elapsed = std::chrono::duration_cast<std::chrono::microseconds>(end_time - start_time_).count() / 1e6;
        if (elapsed > 0) {
            return ((user_sec + sys_sec) / elapsed) * 100.0;
        }
    }
    return 0.0;
}

size_t BenchmarkHarness::measure_ram_rss_mb() {
    std::ifstream statm("/proc/self/statm");
    if (statm.is_open()) {
        long pages = 0, rss = 0;
        statm >> pages >> rss;
        long page_size_bytes = sysconf(_SC_PAGESIZE);
        return static_cast<size_t>((rss * page_size_bytes) / (1024 * 1024));
    }
    return 0;
}

void BenchmarkHarness::save_report_json(const std::string& filepath) const {
    BenchmarkMetrics m = get_metrics();

    nlohmann::json report = {
        {"canvas", {
            {"width", m.canvas_width},
            {"height", m.canvas_height},
            {"targetFps", m.target_fps}
        }},
        {"performance", {
            {"totalFrames", m.total_frames},
            {"elapsedSeconds", m.elapsed_sec},
            {"renderedFps", m.rendered_fps},
            {"outputFps", m.output_fps},
            {"droppedFrames", m.dropped_frames},
            {"avgFrameTimeMs", m.avg_frame_time_ms},
            {"cpuUsagePercent", m.cpu_usage_pct},
            {"ramRssMb", m.ram_rss_mb}
        }}
    };

    std::ofstream ofs(filepath);
    if (ofs.is_open()) {
        ofs << report.dump(2) << std::endl;
        LOG_INFO("Benchmark report saved to: " + filepath);
    } else {
        LOG_ERROR("Failed to write benchmark report to: " + filepath);
    }
}

} // namespace tarva
