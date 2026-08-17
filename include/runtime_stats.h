#ifndef TARVA_RUNTIME_STATS_H
#define TARVA_RUNTIME_STATS_H

#include <atomic>
#include <chrono>
#include <cstdint>
#include <string>

namespace tarva {

// Output/engine states reported by /status.
enum class OutputState {
    IDLE,
    INITIALIZING,
    RUNNING,
    ERROR,
    FINALIZED
};

// Thread-safe snapshot of engine runtime metrics, updated by the playout loop
// and read by the API server. All counters are atomics so the render loop and
// the HTTP handler never share a lock.
class RuntimeStats {
public:
    RuntimeStats();

    // --- written by the playout loop ---
    void set_playout_time_ns(int64_t pts_ns);
    void note_frame_rendered();
    void note_frame_dropped();
    void note_output_frame();
    void set_output_state(OutputState state);

    // --- read by /status ---
    int64_t playout_time_ns() const;
    int64_t rendered_frames() const;
    int64_t dropped_frames() const;
    int64_t output_frames() const;
    OutputState output_state() const;

    // Average rendered FPS since start (frames / wall time).
    double rendered_fps() const;

    // On-demand resource measurements (called from the HTTP handler).
    double cpu_usage_percent();
    size_t ram_rss_mb();

private:
    std::atomic<int64_t> playout_time_ns_{0};
    std::atomic<int64_t> rendered_frames_{0};
    std::atomic<int64_t> dropped_frames_{0};
    std::atomic<int64_t> output_frames_{0};
    std::atomic<int> output_state_{static_cast<int>(OutputState::IDLE)};

    std::chrono::steady_clock::time_point start_time_;
    std::atomic<int64_t> last_cpu_user_us_{0};
    std::atomic<int64_t> last_cpu_sys_us_{0};
};

} // namespace tarva

#endif // TARVA_RUNTIME_STATS_H
