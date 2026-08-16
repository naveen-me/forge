#include "scene_controller.h"
#include "timeline_engine.h"
#include "source_manager.h"
#include "logger.h"
#include <cassert>
#include <iostream>

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::DEBUG);
    LOG_INFO("Testing SceneController atomic updates and scheduled operations...");

    auto timeline = std::make_shared<tarva::TimelineEngine>();
    auto source_mgr = std::make_shared<tarva::SourceManager>();
    auto controller = std::make_shared<tarva::SceneController>(timeline, source_mgr);

    // 1. Initial Scene Setup
    tarva::Scene scene;
    scene.canvas.width = 1920;
    scene.canvas.height = 1080;
    controller->update_full_scene(scene);

    assert(controller->current_scene().canvas.width == 1920);

    // 2. Add layer
    tarva::Layer layer;
    layer.id = "logo1";
    layer.type = "text";
    layer.layer = 10;
    layer.text = "API Test Text";
    layer.start_ns = 0;

    bool ok = controller->add_layer(layer);
    assert(ok);

    tarva::Scene cur = controller->current_scene();
    assert(cur.layers.size() == 1);
    assert(cur.layers[0].id == "logo1");

    // 3. Patch layer
    nlohmann::json patch = { {"id", "logo1"}, {"x", 500}, {"y", 800} };
    ok = controller->patch_layer("logo1", patch);
    assert(ok);

    cur = controller->current_scene();
    assert(cur.layers[0].x == 500);
    assert(cur.layers[0].y == 800);

    // 4. Hide / Show layer
    ok = controller->set_layer_hidden("logo1", true);
    assert(ok);
    cur = controller->current_scene();
    assert(cur.layers[0].hidden == true);

    ok = controller->set_layer_hidden("logo1", false);
    assert(ok);
    cur = controller->current_scene();
    assert(cur.layers[0].hidden == false);

    // 5. Scheduled operations
    nlohmann::json sched_patch = { {"x", 900} };
    controller->schedule_operation(10LL * 1000000000LL, "patch_layer", "logo1", sched_patch);

    // Execute at t = 5s (5e9 ns) -> Should not trigger
    controller->process_scheduled_operations(5LL * 1000000000LL);
    cur = controller->current_scene();
    assert(cur.layers[0].x == 500);

    // Execute at t = 10s (10e9 ns) -> Should trigger patch
    controller->process_scheduled_operations(10LL * 1000000000LL);
    cur = controller->current_scene();
    assert(cur.layers[0].x == 900);

    // 6. Delete layer
    ok = controller->delete_layer("logo1");
    assert(ok);

    cur = controller->current_scene();
    assert(cur.layers.empty());

    LOG_INFO("All SceneController and API tests passed successfully!");
    return 0;
}
