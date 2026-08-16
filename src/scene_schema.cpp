#include "scene_schema.h"
#include "logger.h"
#include <sstream>
#include <iomanip>
#include <cmath>
#include <algorithm>

namespace tarva {

int64_t parse_time_str(const std::string& time_str) {
    if (time_str.empty()) return 0;

    int hours = 0, minutes = 0;
    double seconds = 0.0;

    std::stringstream ss(time_str);
    char c1, c2;

    if (ss >> hours >> c1 >> minutes >> c2 >> seconds) {
        double total_sec = hours * 3600.0 + minutes * 60.0 + seconds;
        return static_cast<int64_t>(std::round(total_sec * 1e9));
    } else {
        // Fallback: try parsing as raw seconds
        try {
            double total_sec = std::stod(time_str);
            return static_cast<int64_t>(std::round(total_sec * 1e9));
        } catch (...) {
            LOG_ERROR("Failed to parse time string: " + time_str);
            return 0;
        }
    }
}

std::string format_time_ns(int64_t ns) {
    if (ns == INT64_MAX) return "infinity";
    if (ns < 0) ns = 0;

    int64_t total_ms = ns / 1000000;
    int64_t ms = total_ms % 1000;
    int64_t total_sec = total_ms / 1000;
    int64_t sec = total_sec % 60;
    int64_t total_min = total_sec / 60;
    int64_t min = total_min % 60;
    int64_t hours = total_min / 60;

    std::stringstream ss;
    ss << std::setfill('0') << std::setw(2) << hours << ":"
       << std::setfill('0') << std::setw(2) << min << ":"
       << std::setfill('0') << std::setw(2) << sec << "."
       << std::setfill('0') << std::setw(3) << ms;
    return ss.str();
}

void from_json(const nlohmann::json& j, Canvas& c) {
    if (j.contains("width")) j.at("width").get_to(c.width);
    if (j.contains("height")) j.at("height").get_to(c.height);
    if (j.contains("fps")) j.at("fps").get_to(c.fps);
}

void to_json(nlohmann::json& j, const Canvas& c) {
    j = nlohmann::json{
        {"width", c.width},
        {"height", c.height},
        {"fps", c.fps}
    };
}

void from_json(const nlohmann::json& j, Output& o) {
    if (j.contains("url")) j.at("url").get_to(o.url);
}

void to_json(nlohmann::json& j, const Output& o) {
    j = nlohmann::json{
        {"url", o.url}
    };
}

void from_json(const nlohmann::json& j, Effect& e) {
    if (j.contains("type")) j.at("type").get_to(e.type);
    if (j.contains("direction")) j.at("direction").get_to(e.direction);
    if (j.contains("speed")) j.at("speed").get_to(e.speed);
    if (j.contains("duration")) {
        if (j["duration"].is_string()) {
            e.duration_ns = parse_time_str(j["duration"].get<std::string>());
        } else {
            e.duration_ns = static_cast<int64_t>(j["duration"].get<double>() * 1e9);
        }
    }
}

void to_json(nlohmann::json& j, const Effect& e) {
    j = nlohmann::json{
        {"type", e.type},
        {"direction", e.direction},
        {"speed", e.speed},
        {"duration", format_time_ns(e.duration_ns)}
    };
}

void from_json(const nlohmann::json& j, Layer& l) {
    if (j.contains("id")) j.at("id").get_to(l.id);
    if (j.contains("type")) j.at("type").get_to(l.type);
    if (j.contains("layer")) j.at("layer").get_to(l.layer);
    if (j.contains("source")) j.at("source").get_to(l.source);

    if (j.contains("start")) {
        if (j["start"].is_string()) {
            l.start_str = j["start"].get<std::string>();
            l.start_ns = parse_time_str(l.start_str);
        } else if (j["start"].is_number()) {
            double s = j["start"].get<double>();
            l.start_ns = static_cast<int64_t>(s * 1e9);
            l.start_str = format_time_ns(l.start_ns);
        }
    }

    if (j.contains("end")) {
        if (j["end"].is_string()) {
            l.end_str = j["end"].get<std::string>();
            l.end_ns = parse_time_str(l.end_str);
        } else if (j["end"].is_number()) {
            double e = j["end"].get<double>();
            l.end_ns = static_cast<int64_t>(e * 1e9);
            l.end_str = format_time_ns(l.end_ns);
        }
    } else {
        l.end_ns = INT64_MAX;
    }

    if (j.contains("x")) j.at("x").get_to(l.x);
    if (j.contains("y")) j.at("y").get_to(l.y);
    if (j.contains("width")) j.at("width").get_to(l.width);
    if (j.contains("height")) j.at("height").get_to(l.height);
    if (j.contains("opacity")) j.at("opacity").get_to(l.opacity);
    if (j.contains("rotation")) j.at("rotation").get_to(l.rotation);
    if (j.contains("loop")) j.at("loop").get_to(l.loop);
    if (j.contains("hidden")) j.at("hidden").get_to(l.hidden);

    if (j.contains("text")) j.at("text").get_to(l.text);
    if (j.contains("fontSize")) j.at("fontSize").get_to(l.font_size);
    if (j.contains("fontColor")) j.at("fontColor").get_to(l.font_color);
    if (j.contains("backgroundColor")) j.at("backgroundColor").get_to(l.background_color);

    if (j.contains("effect")) {
        Effect eff;
        j.at("effect").get_to(eff);
        l.effect = eff;
    }
}

void to_json(nlohmann::json& j, const Layer& l) {
    j = nlohmann::json{
        {"id", l.id},
        {"type", l.type},
        {"layer", l.layer},
        {"source", l.source},
        {"start", l.start_str},
        {"x", l.x},
        {"y", l.y},
        {"width", l.width},
        {"height", l.height},
        {"opacity", l.opacity},
        {"rotation", l.rotation},
        {"loop", l.loop},
        {"hidden", l.hidden}
    };
    if (l.end_ns != INT64_MAX) {
        j["end"] = l.end_str;
    }
    if (!l.text.empty()) {
        j["text"] = l.text;
        j["fontSize"] = l.font_size;
        j["fontColor"] = l.font_color;
        j["backgroundColor"] = l.background_color;
    }
    if (l.effect.has_value()) {
        j["effect"] = l.effect.value();
    }
}

void from_json(const nlohmann::json& j, Scene& s) {
    if (j.contains("canvas")) j.at("canvas").get_to(s.canvas);
    if (j.contains("output")) j.at("output").get_to(s.output);
    if (j.contains("layers")) j.at("layers").get_to(s.layers);
    if (j.contains("revision")) j.at("revision").get_to(s.revision);
}

void to_json(nlohmann::json& j, const Scene& s) {
    j = nlohmann::json{
        {"canvas", s.canvas},
        {"output", s.output},
        {"layers", s.layers},
        {"revision", s.revision}
    };
}

} // namespace tarva
