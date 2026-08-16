#include "scene_controller.h"
#include "logger.h"
#include <algorithm>

namespace tarva {

SceneController::SceneController(std::shared_ptr<TimelineEngine> timeline,
                                 std::shared_ptr<SourceManager> source_manager)
    : timeline_(timeline), source_manager_(source_manager) {}

Scene SceneController::current_scene() const {
    std::lock_guard<std::recursive_mutex> lock(controller_mutex_);
    return timeline_->scene();
}

bool SceneController::update_full_scene(const Scene& new_scene) {
    std::lock_guard<std::recursive_mutex> lock(controller_mutex_);

    // Preload sources for all layers in new scene
    for (const auto& layer : new_scene.layers) {
        source_manager_->prepare_source(layer, new_scene.canvas.width, new_scene.canvas.height);
    }

    Scene scene_copy = new_scene;
    scene_copy.revision = timeline_->scene().revision + 1;
    timeline_->set_scene(scene_copy);

    LOG_INFO("SceneController: Updated full scene to revision " + std::to_string(scene_copy.revision));
    return true;
}

bool SceneController::add_layer(const Layer& layer) {
    std::lock_guard<std::recursive_mutex> lock(controller_mutex_);

    Scene scene = timeline_->scene();

    // Remove existing layer if same id
    scene.layers.erase(
        std::remove_if(scene.layers.begin(), scene.layers.end(),
            [&](const Layer& l) { return l.id == layer.id; }),
        scene.layers.end()
    );

    scene.layers.push_back(layer);
    scene.revision++;

    source_manager_->prepare_source(layer, scene.canvas.width, scene.canvas.height);
    timeline_->set_scene(scene);

    LOG_INFO("SceneController: Added layer '" + layer.id + "' (revision " + std::to_string(scene.revision) + ")");
    return true;
}

bool SceneController::patch_layer(const std::string& layer_id, const nlohmann::json& patch) {
    std::lock_guard<std::recursive_mutex> lock(controller_mutex_);

    Scene scene = timeline_->scene();
    auto it = std::find_if(scene.layers.begin(), scene.layers.end(),
        [&](const Layer& l) { return l.id == layer_id; });

    if (it == scene.layers.end()) {
        LOG_WARN("SceneController: Patch failed, layer not found: " + layer_id);
        return false;
    }

    // Convert layer to JSON, apply patch, and deserialize
    nlohmann::json layer_j = *it;
    layer_j.update(patch);

    Layer patched_layer = layer_j.get<Layer>();

    // If start time string was patched, update start_ns
    if (patch.contains("start") && patch["start"].is_string()) {
        patched_layer.start_ns = parse_time_str(patch["start"].get<std::string>());
    }
    if (patch.contains("end") && patch["end"].is_string()) {
        patched_layer.end_ns = parse_time_str(patch["end"].get<std::string>());
    }

    *it = patched_layer;
    scene.revision++;

    source_manager_->prepare_source(patched_layer, scene.canvas.width, scene.canvas.height);
    timeline_->set_scene(scene);

    LOG_INFO("SceneController: Patched layer '" + layer_id + "' (revision " + std::to_string(scene.revision) + ")");
    return true;
}

bool SceneController::delete_layer(const std::string& layer_id) {
    std::lock_guard<std::recursive_mutex> lock(controller_mutex_);

    Scene scene = timeline_->scene();
    auto initial_size = scene.layers.size();

    scene.layers.erase(
        std::remove_if(scene.layers.begin(), scene.layers.end(),
            [&](const Layer& l) { return l.id == layer_id; }),
        scene.layers.end()
    );

    if (scene.layers.size() == initial_size) {
        LOG_WARN("SceneController: Delete failed, layer not found: " + layer_id);
        return false;
    }

    scene.revision++;
    source_manager_->remove_source(layer_id);
    timeline_->set_scene(scene);

    LOG_INFO("SceneController: Deleted layer '" + layer_id + "' (revision " + std::to_string(scene.revision) + ")");
    return true;
}

bool SceneController::set_layer_hidden(const std::string& layer_id, bool hidden) {
    nlohmann::json patch = { {"hidden", hidden} };
    return patch_layer(layer_id, patch);
}

void SceneController::schedule_operation(int64_t execute_at_ns, const std::string& op_type,
                                         const std::string& layer_id, const nlohmann::json& payload) {
    std::lock_guard<std::recursive_mutex> lock(controller_mutex_);
    ScheduledOperation op{ execute_at_ns, op_type, layer_id, payload };
    scheduled_ops_.push_back(op);
    LOG_INFO("SceneController: Scheduled operation '" + op_type + "' for layer '" + layer_id + "' at " + format_time_ns(execute_at_ns));
}

void SceneController::process_scheduled_operations(int64_t current_pts_ns) {
    std::lock_guard<std::recursive_mutex> lock(controller_mutex_);

    auto it = scheduled_ops_.begin();
    while (it != scheduled_ops_.end()) {
        if (current_pts_ns >= it->execute_at_ns) {
            LOG_INFO("SceneController: Executing scheduled operation '" + it->op_type + "' on layer '" + it->layer_id + "'");

            if (it->op_type == "patch_layer") {
                patch_layer(it->layer_id, it->payload);
            } else if (it->op_type == "add_layer") {
                Layer l = it->payload.get<Layer>();
                add_layer(l);
            } else if (it->op_type == "delete_layer") {
                delete_layer(it->layer_id);
            }

            it = scheduled_ops_.erase(it);
        } else {
            ++it;
        }
    }
}

} // namespace tarva
