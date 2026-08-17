#include "api_server.h"
#include "scene_controller.h"
#include "timeline_engine.h"
#include "source_manager.h"
#include "runtime_stats.h"
#include "logger.h"
#include <cassert>
#include <cstdlib>
#include <iostream>
#include <nlohmann/json.hpp>

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::DEBUG);
    LOG_INFO("Testing /status endpoint runtime metrics...");

    auto timeline = std::make_shared<tarva::TimelineEngine>();
    auto source_mgr = std::make_shared<tarva::SourceManager>();
    auto controller = std::make_shared<tarva::SceneController>(timeline, source_mgr);
    auto stats = std::make_shared<tarva::RuntimeStats>();

    tarva::Scene scene;
    scene.canvas.width = 1920;
    scene.canvas.height = 1080;
    scene.canvas.fps = 30;

    tarva::Layer layer;
    layer.id = "lower-third";
    layer.type = "text";
    layer.layer = 10;
    layer.text = "LIVE";
    layer.start_ns = 0;
    scene.layers = { layer };
    controller->update_full_scene(scene);

    // Simulate an engine that has rendered some frames.
    stats->set_playout_time_ns(5LL * 1000000000LL); // t = 5s
    for (int i = 0; i < 150; ++i) stats->note_frame_rendered();
    stats->note_frame_dropped();
    for (int i = 0; i < 149; ++i) stats->note_output_frame();
    stats->set_output_state(tarva::OutputState::RUNNING);

    int port = 18080;
    if (const char* env_p = std::getenv("TEST_STATUS_PORT")) {
        port = std::atoi(env_p);
    }

    tarva::ApiServer server(port, controller, stats);
    bool started = server.start();
    if (!started) {
        LOG_ERROR("ApiServer failed to start on port " + std::to_string(port));
        return 1;
    }

    httplib::Client cli("http://127.0.0.1:" + std::to_string(port));

    // /health
    auto health = cli.Get("/health");
    if (!health || health->status != 200) {
        LOG_ERROR("/health request failed");
        server.stop();
        return 1;
    }
    auto health_j = nlohmann::json::parse(health->body);
    if (health_j["status"] != "ok") {
        LOG_ERROR("/health returned unexpected body");
        server.stop();
        return 1;
    }

    // /status
    auto res = cli.Get("/status");
    if (!res || res->status != 200) {
        LOG_ERROR("/status request failed");
        server.stop();
        return 1;
    }
    nlohmann::json j = nlohmann::json::parse(res->body);

    auto check = [&](const std::string& what, bool cond) -> bool {
        if (!cond) LOG_ERROR("/status check failed: " + what);
        return cond;
    };
    bool ok = true;
    ok &= check("status", j["status"] == "running");
    ok &= check("canvas.width", j["canvas"]["width"] == 1920);
    ok &= check("canvas.height", j["canvas"]["height"] == 1080);
    ok &= check("revision", j["revision"] >= 1);
    ok &= check("layerCount", j["layerCount"] == 1);
    ok &= check("playoutTime.ns", j["playoutTime"]["ns"] == 5LL * 1000000000LL);
    ok &= check("playoutTime.formatted", j["playoutTime"]["formatted"] == "00:00:05.000");
    ok &= check("fps.target", j["fps"]["target"] == 30);
    ok &= check("fps.rendered", j["fps"]["rendered"] > 0.0);
    ok &= check("frames.rendered", j["frames"]["rendered"] == 150);
    ok &= check("frames.dropped", j["frames"]["dropped"] == 1);
    ok &= check("frames.output", j["frames"]["output"] == 149);
    ok &= check("output.state", j["output"]["state"] == "running");
    ok &= check("output.framesSent", j["output"]["framesSent"] == 149);

    // The lower-third layer is active at t=5s and must be listed.
    ok &= check("activeLayers.array", j["activeLayers"].is_array());
    ok &= check("activeLayers.size", j["activeLayers"].size() == 1);
    ok &= check("activeLayers.id", j["activeLayers"][0]["id"] == "lower-third");

    // The source manager must expose the prepared text source as ready.
    ok &= check("sources.array", j["sources"].is_array());
    ok &= check("sources.size", j["sources"].size() >= 1);
    ok &= check("sources.state", j["sources"][0]["state"] == "ready");

    ok &= check("resources.ramRssMb", j["resources"]["ramRssMb"] > 0);

    if (!ok) {
        LOG_ERROR("One or more /status checks failed: " + j.dump());
        server.stop();
        return 1;
    }

    LOG_INFO("Status response: " + j.dump());

    server.stop();
    LOG_INFO("All /status endpoint tests passed successfully!");
    return 0;
}
