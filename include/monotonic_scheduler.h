#ifndef TARVA_MONOTONIC_SCHEDULER_H
#define TARVA_MONOTONIC_SCHEDULER_H

#include <chrono>
#include <thread>
#include <cstdint>

namespace tarva {

class MonotonicScheduler {
public:
    MonotonicScheduler(int fps = 30);

    void start();

    // Calculates target deadline for specified frame_idx and sleeps until deadline to prevent timing drift
    void wait_for_frame_deadline(int64_t frame_idx);

    int fps() const { return fps_; }
    int64_t frame_duration_ns() const { return frame_duration_ns_; }

    double total_elapsed_sec() const;

private:
    int fps_ = 30;
    int64_t frame_duration_ns_ = 33333333LL; // Default 33.33ms for 30fps
    std::chrono::steady_clock::time_point start_time_;
    bool started_ = false;
};

} // namespace tarva

#endif // TARVA_MONOTONIC_SCHEDULER_H
