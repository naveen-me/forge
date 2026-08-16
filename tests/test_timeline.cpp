#include "timeline_engine.h"
#include "logger.h"
#include <cassert>
#include <iostream>

void test_timeline_same_layer_replacement() {
    tarva::TimelineEngine engine;

    tarva::Scene scene;
    scene.canvas.width = 1920;
    scene.canvas.height = 1080;

    // Item A: layer 10, start 00:00:00 (0 ns), end 00:00:10 (10s = 10e9 ns)
    tarva::Layer itemA;
    itemA.id = "logo-A";
    itemA.layer = 10;
    itemA.start_ns = 0;
    itemA.end_ns = 10LL * 1000000000LL;

    // Item B: layer 10, start 00:00:05 (5s = 5e9 ns), end 00:00:15 (15s = 15e9 ns)
    tarva::Layer itemB;
    itemB.id = "logo-B";
    itemB.layer = 10;
    itemB.start_ns = 5LL * 1000000000LL;
    itemB.end_ns = 15LL * 1000000000LL;

    scene.layers = { itemA, itemB };
    engine.set_scene(scene);

    // At t = 2s (2e9 ns): Only A is active
    auto active_2s = engine.resolve_active_layers(2LL * 1000000000LL);
    assert(active_2s.size() == 1);
    assert(active_2s[0].id == "logo-A");

    // At t = 6s (6e9 ns): Both A and B time ranges overlap on layer 10. B starts later (5s vs 0s) -> B replaces A!
    auto active_6s = engine.resolve_active_layers(6LL * 1000000000LL);
    assert(active_6s.size() == 1);
    assert(active_6s[0].id == "logo-B");

    // At t = 12s (12e9 ns): A has ended, B is active -> B active
    auto active_12s = engine.resolve_active_layers(12LL * 1000000000LL);
    assert(active_12s.size() == 1);
    assert(active_12s[0].id == "logo-B");

    // At t = 16s (16e9 ns): Gap (no active layers)
    auto active_16s = engine.resolve_active_layers(16LL * 1000000000LL);
    assert(active_16s.empty());

    LOG_INFO("Same-layer replacement test passed successfully!");
}

void test_timeline_different_layers_coexist() {
    tarva::TimelineEngine engine;

    tarva::Scene scene;
    scene.canvas.width = 1920;
    scene.canvas.height = 1080;

    tarva::Layer video;
    video.id = "base-video";
    video.layer = 0;
    video.start_ns = 0;
    video.end_ns = 30LL * 1000000000LL;

    tarva::Layer ticker;
    ticker.id = "ticker-html";
    ticker.layer = 20;
    ticker.start_ns = 2LL * 1000000000LL;
    ticker.end_ns = 10LL * 1000000000LL;

    scene.layers = { video, ticker };
    engine.set_scene(scene);

    // At t = 5s: Both video (layer 0) and ticker (layer 20) coexist
    auto active = engine.resolve_active_layers(5LL * 1000000000LL);
    assert(active.size() == 2);
    assert(active[0].id == "base-video");
    assert(active[1].id == "ticker-html");

    LOG_INFO("Different layers coexist test passed successfully!");
}

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::DEBUG);
    LOG_INFO("Testing TimelineEngine...");

    test_timeline_same_layer_replacement();
    test_timeline_different_layers_coexist();

    LOG_INFO("All TimelineEngine tests passed successfully!");
    return 0;
}
