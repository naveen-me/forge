#ifndef TARVA_TIMELINE_ENGINE_H
#define TARVA_TIMELINE_ENGINE_H

#include <vector>
#include <memory>
#include <mutex>
#include <cstdint>
#include "scene_schema.h"

namespace tarva {

class TimelineEngine {
public:
    TimelineEngine();

    void set_scene(const Scene& scene);

    // Resolves active layers at global playout time pts_ns following same-layer replacement semantics
    std::vector<Layer> resolve_active_layers(int64_t current_pts_ns) const;

    int64_t current_time_ns() const { return current_pts_ns_; }
    void set_current_time_ns(int64_t pts_ns) { current_pts_ns_ = pts_ns; }
    void advance_time_ns(int64_t delta_ns) { current_pts_ns_ += delta_ns; }

    const Scene& scene() const { return scene_; }

private:
    Scene scene_;
    int64_t current_pts_ns_ = 0;
    mutable std::mutex timeline_mutex_;
};

} // namespace tarva

#endif // TARVA_TIMELINE_ENGINE_H
