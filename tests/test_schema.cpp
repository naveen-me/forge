#include "scene_schema.h"
#include "logger.h"
#include <cassert>
#include <iostream>
#include <fstream>

void test_time_parsing() {
    int64_t ns1 = tarva::parse_time_str("00:00:00");
    assert(ns1 == 0);

    int64_t ns2 = tarva::parse_time_str("00:01:30.500");
    int64_t expected = (1 * 60 + 30) * 1000000000LL + 500000000LL;
    assert(ns2 == expected);

    std::string formatted = tarva::format_time_ns(ns2);
    assert(formatted == "00:01:30.500");

    LOG_INFO("Time parsing test passed.");
}

void test_scene_json_parsing() {
    std::string json_str = R"({
        "canvas": {
            "width": 1920,
            "height": 1080,
            "fps": 30
        },
        "output": {
            "url": "rtmp://example.com/live/test"
        },
        "layers": [
            {
                "id": "base-video",
                "type": "video",
                "layer": 0,
                "source": "/media/program.mp4",
                "start": "00:00:00",
                "end": "00:30:00",
                "loop": true
            },
            {
                "id": "ticker",
                "type": "html",
                "layer": 20,
                "source": "https://graphics.example.com/ticker",
                "start": "00:02:00",
                "end": "00:08:00",
                "x": 100,
                "y": 900,
                "width": 1720,
                "height": 100,
                "effect": {
                    "type": "scroll",
                    "direction": "left",
                    "speed": 120
                }
            }
        ]
    })";

    nlohmann::json j = nlohmann::json::parse(json_str);
    tarva::Scene scene = j.get<tarva::Scene>();

    assert(scene.canvas.width == 1920);
    assert(scene.canvas.height == 1080);
    assert(scene.canvas.fps == 30);
    assert(scene.output.url == "rtmp://example.com/live/test");
    assert(scene.layers.size() == 2);

    assert(scene.layers[0].id == "base-video");
    assert(scene.layers[0].type == "video");
    assert(scene.layers[0].layer == 0);
    assert(scene.layers[0].loop == true);

    assert(scene.layers[1].id == "ticker");
    assert(scene.layers[1].type == "html");
    assert(scene.layers[1].layer == 20);
    assert(scene.layers[1].start_ns == 120LL * 1000000000LL);
    assert(scene.layers[1].end_ns == 480LL * 1000000000LL);
    assert(scene.layers[1].effect.has_value());
    assert(scene.layers[1].effect->type == "scroll");
    assert(scene.layers[1].effect->speed == 120.0);

    LOG_INFO("Scene JSON parsing test passed.");
}

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::DEBUG);
    test_time_parsing();
    test_scene_json_parsing();
    std::cout << "All schema tests passed successfully!\n";
    return 0;
}
