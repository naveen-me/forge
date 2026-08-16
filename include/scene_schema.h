#ifndef TARVA_SCENE_SCHEMA_H
#define TARVA_SCENE_SCHEMA_H

#include <string>
#include <vector>
#include <optional>
#include <cstdint>
#include <nlohmann/json.hpp>

namespace tarva {

// Time conversion helper: Converts "HH:MM:SS[.mmm]" to nanoseconds
int64_t parse_time_str(const std::string& time_str);
std::string format_time_ns(int64_t ns);

struct Canvas {
    int width = 1920;
    int height = 1080;
    int fps = 30;
};

struct Output {
    std::string url;
};

struct Effect {
    std::string type; // "fade", "scroll", "slide", "scale"
    std::string direction = "left";
    double speed = 0.0;
    int64_t duration_ns = 0;
};

struct Layer {
    std::string id;
    std::string type; // "video", "image", "html", "text"
    int layer = 0;    // z-index
    std::string source;

    std::string start_str = "00:00:00";
    std::string end_str;
    int64_t start_ns = 0;
    int64_t end_ns = INT64_MAX; // Maximum lifetime if end omitted

    int x = 0;
    int y = 0;
    int width = 0;  // 0 means use canvas / natural source size
    int height = 0; // 0 means use canvas / natural source size
    double opacity = 1.0;
    double rotation = 0.0;
    bool loop = false;

    // Text specific
    std::string text;
    int font_size = 32;
    std::string font_color = "#FFFFFF";
    std::string background_color = "";

    std::optional<Effect> effect;

    // Runtime active state
    bool hidden = false;
};

struct Scene {
    Canvas canvas;
    Output output;
    std::vector<Layer> layers;
    int64_t revision = 1;
};

// JSON deserialization and serialization
void from_json(const nlohmann::json& j, Canvas& c);
void to_json(nlohmann::json& j, const Canvas& c);

void from_json(const nlohmann::json& j, Output& o);
void to_json(nlohmann::json& j, const Output& o);

void from_json(const nlohmann::json& j, Effect& e);
void to_json(nlohmann::json& j, const Effect& e);

void from_json(const nlohmann::json& j, Layer& l);
void to_json(nlohmann::json& j, const Layer& l);

void from_json(const nlohmann::json& j, Scene& s);
void to_json(nlohmann::json& j, const Scene& s);

} // namespace tarva

#endif // TARVA_SCENE_SCHEMA_H
