#include "runtime_stats.h"

#include <sys/resource.h>
#include <unistd.h>
#include <fstream>

namespace tarva {

RuntimeStats::RuntimeStats() {
    start_time_ = std::chrono::steady_clock::now();

    struct rusage usage;
    if (getrusage(RUSAGE_SELF, &usage) == 0) {
        last_cpu_user_us_ = usage.ru_utime.tv_sec * 1000000LL + usage.ru_utime.tv_usec;
        last_cpu_sys_us_ = usage.ru_stime.tv_sec * 1000000LL + usage.ru_stime.tv_usec;
    }
}

void RuntimeStats::set_playout_time_ns(int64_t pts_ns) {
    playout_time_ns_.store(pts_ns, std::memory_order_relaxed);
}

void RuntimeStats::note_frame_rendered() {
    rendered_frames_.fetch_add(1, std::memory_order_relaxed);
}

void RuntimeStats::note_frame_dropped() {
    dropped_frames_.fetch_add(1, std::memory_order_relaxed);
}

void RuntimeStats::note_output_frame() {
    output_frames_.fetch_add(1, std::memory_order_relaxed);
}

void RuntimeStats::set_output_state(OutputState state) {
    output_state_.store(static_cast<int>(state), std::memory_order_relaxed);
}

int64_t RuntimeStats::playout_time_ns() const {
    return playout_time_ns_.load(std::memory_order_relaxed);
}

int64_t RuntimeStats::rendered_frames() const {
    return rendered_frames_.load(std::memory_order_relaxed);
}

int64_t RuntimeStats::dropped_frames() const {
    return dropped_frames_.load(std::memory_order_relaxed);
}

int64_t RuntimeStats::output_frames() const {
    return output_frames_.load(std::memory_order_relaxed);
}

OutputState RuntimeStats::output_state() const {
    return static_cast<OutputState>(output_state_.load(std::memory_order_relaxed));
}

double RuntimeStats::rendered_fps() const {
    auto now = std::chrono::steady_clock::now();
    double elapsed_sec = std::chrono::duration_cast<std::chrono::microseconds>(now - start_time_).count() / 1e6;
    int64_t frames = rendered_frames_.load(std::memory_order_relaxed);
    if (elapsed_sec <= 0.0 || frames == 0) return 0.0;
    return static_cast<double>(frames) / elapsed_sec;
}

double RuntimeStats::cpu_usage_percent() {
    struct rusage usage;
    if (getrusage(RUSAGE_SELF, &usage) != 0) return 0.0;

    int64_t user_us = usage.ru_utime.tv_sec * 1000000LL + usage.ru_utime.tv_usec;
    int64_t sys_us = usage.ru_stime.tv_sec * 1000000LL + usage.ru_stime.tv_usec;

    int64_t prev_user = last_cpu_user_us_.exchange(user_us);
    int64_t prev_sys = last_cpu_sys_us_.exchange(sys_us);
    int64_t delta_cpu = (user_us - prev_user) + (sys_us - prev_sys);

    auto now = std::chrono::steady_clock::now();
    double delta_wall = std::chrono::duration_cast<std::chrono::microseconds>(now - start_time_).count() / 1e6;
    if (delta_wall <= 0.0 || delta_cpu <= 0) return 0.0;

    // Since the start of the process, fraction of wall time spent in user+sys.
    int64_t total_cpu = user_us + sys_us;
    return (static_cast<double>(total_cpu) / (delta_wall * 1e6)) * 100.0;
}

size_t RuntimeStats::ram_rss_mb() {
    std::ifstream statm("/proc/self/statm");
    if (statm.is_open()) {
        long pages = 0, rss = 0;
        statm >> pages >> rss;
        long page_size_bytes = sysconf(_SC_PAGESIZE);
        return static_cast<size_t>((rss * page_size_bytes) / (1024 * 1024));
    }
    return 0;
}

} // namespace tarva
