#ifndef TARVA_SCENE_CONTROLLER_H
#define TARVA_SCENE_CONTROLLER_H

#include <vector>
#include <mutex>
#include <memory>
#include <cstdint>
#include <nlohmann/json.hpp>
#include "scene_schema.h"
#include "timeline_engine.h"
#include "source_manager.h"

namespace tarva {

struct ScheduledOperation {
    int64_t execute_at_ns = 0;
    std::string op_type; // "patch_layer", "add_layer", "delete_layer"
    std::string layer_id;
    nlohmann::json payload;
};

class SceneController {
public:
    SceneController(std::shared_ptr<TimelineEngine> timeline,
                    std::shared_ptr<SourceManager> source_manager);

    // Atomic scene operations
    bool update_full_scene(const Scene& new_scene);
    bool add_layer(const Layer& layer);
    bool patch_layer(const std::string& layer_id, const nlohmann::json& patch);
    bool delete_layer(const std::string& layer_id);
    bool set_layer_hidden(const std::string& layer_id, bool hidden);

    // Scheduled operations
    void schedule_operation(int64_t execute_at_ns, const std::string& op_type,
                            const std::string& layer_id, const nlohmann::json& payload);
    void process_scheduled_operations(int64_t current_pts_ns);

    Scene current_scene() const;

    // Active layers at a given playout time (for /status)
    std::vector<Layer> active_layers_at(int64_t pts_ns) const;

    // Snapshot of all managed source states (for /status)
    std::vector<SourceHandle> source_states() const;

private:
    std::shared_ptr<TimelineEngine> timeline_;
    std::shared_ptr<SourceManager> source_manager_;

    std::vector<ScheduledOperation> scheduled_ops_;
    mutable std::recursive_mutex controller_mutex_;
};

} // namespace tarva

#endif // TARVA_SCENE_CONTROLLER_H
