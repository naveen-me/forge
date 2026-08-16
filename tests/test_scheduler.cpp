#include "monotonic_scheduler.h"
#include "logger.h"
#include <cassert>
#include <iostream>
#include <cmath>

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::DEBUG);
    LOG_INFO("Testing MonotonicScheduler deadline pacing...");

    int fps = 30;
    tarva::MonotonicScheduler scheduler(fps);
    scheduler.start();

    // Simulate pacing for 30 frames (1 second total target duration)
    for (int i = 1; i <= 30; ++i) {
        scheduler.wait_for_frame_deadline(i);
    }

    double elapsed = scheduler.total_elapsed_sec();
    LOG_INFO("30 frames scheduled at 30fps total elapsed: " + std::to_string(elapsed) + " seconds");

    // Total elapsed for 30 frames at 30fps should be ~1.0 second (tolerance +/- 0.05s)
    assert(std::abs(elapsed - 1.0) < 0.08);

    LOG_INFO("MonotonicScheduler test passed successfully!");
    return 0;
}
