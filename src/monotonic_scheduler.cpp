#include "monotonic_scheduler.h"
#include "logger.h"

namespace tarva {

MonotonicScheduler::MonotonicScheduler(int fps)
    : fps_((fps > 0) ? fps : 30) {
    frame_duration_ns_ = 1000000000LL / fps_;
}

void MonotonicScheduler::start() {
    start_time_ = std::chrono::steady_clock::now();
    started_ = true;
}

void MonotonicScheduler::wait_for_frame_deadline(int64_t frame_idx) {
    if (!started_) start();

    auto target_deadline = start_time_ + std::chrono::nanoseconds(frame_idx * frame_duration_ns_);
    auto now = std::chrono::steady_clock::now();

    if (now < target_deadline) {
        std::this_thread::sleep_until(target_deadline);
    }
}

double MonotonicScheduler::total_elapsed_sec() const {
    if (!started_) return 0.0;
    auto now = std::chrono::steady_clock::now();
    return std::chrono::duration_cast<std::chrono::microseconds>(now - start_time_).count() / 1e6;
}

} // namespace tarva
