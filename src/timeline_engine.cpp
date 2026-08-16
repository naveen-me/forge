#include "timeline_engine.h"
#include "logger.h"
#include <map>
#include <algorithm>

namespace tarva {

TimelineEngine::TimelineEngine() {}

void TimelineEngine::set_scene(const Scene& scene) {
    std::lock_guard<std::mutex> lock(timeline_mutex_);
    scene_ = scene;
}

std::vector<Layer> TimelineEngine::resolve_active_layers(int64_t current_pts_ns) const {
    std::lock_guard<std::mutex> lock(timeline_mutex_);

    // 1. Group active candidate layers by numeric layer z-index
    std::map<int, std::vector<Layer>> layer_groups;

    for (const auto& l : scene_.layers) {
        if (l.hidden) continue;

        // Active time check: start <= pts < end
        if (current_pts_ns >= l.start_ns && current_pts_ns < l.end_ns) {
            layer_groups[l.layer].push_back(l);
        }
    }

    std::vector<Layer> resolved_active_layers;

    // 2. Apply same-layer replacement semantics for each layer z-index group
    for (const auto& [z_index, candidates] : layer_groups) {
        if (candidates.empty()) continue;

        // Select item with greatest effective start time; tie-breaker: order in scene vector
        const Layer* winner = &candidates[0];
        for (size_t i = 1; i < candidates.size(); ++i) {
            if (candidates[i].start_ns > winner->start_ns) {
                winner = &candidates[i];
            } else if (candidates[i].start_ns == winner->start_ns) {
                // Deterministic tie-breaker: later revision or later item in list
                winner = &candidates[i];
            }
        }

        resolved_active_layers.push_back(*winner);
    }

    // 3. Sort final resolved layers by ascending z-index
    std::sort(resolved_active_layers.begin(), resolved_active_layers.end(),
        [](const Layer& a, const Layer& b) {
            return a.layer < b.layer;
        });

    return resolved_active_layers;
}

} // namespace tarva
