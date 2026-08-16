#include "logger.h"
#include <chrono>
#include <iomanip>

namespace tarva {

Logger& Logger::instance() {
    static Logger inst;
    return inst;
}

void Logger::set_level(LogLevel level) {
    std::lock_guard<std::mutex> lock(mutex_);
    level_ = level;
}

LogLevel Logger::level() const {
    return level_;
}

void Logger::log(LogLevel level, const std::string& msg) {
    std::lock_guard<std::mutex> lock(mutex_);
    if (level < level_) return;

    auto now = std::chrono::system_clock::now();
    auto time_t_now = std::chrono::system_clock::to_time_t(now);
    auto ms = std::chrono::duration_cast<std::chrono::milliseconds>(
                  now.time_since_epoch()) % 1000;

    const char* lvl_str = "INFO";
    switch (level) {
        case LogLevel::DEBUG: lvl_str = "DEBUG"; break;
        case LogLevel::INFO:  lvl_str = "INFO "; break;
        case LogLevel::WARN:  lvl_str = "WARN "; break;
        case LogLevel::ERR:   lvl_str = "ERROR"; break;
    }

    std::cout << "[" << std::put_time(std::localtime(&time_t_now), "%Y-%m-%d %H:%M:%S")
              << "." << std::setfill('0') << std::setw(3) << ms.count() << "] "
              << "[" << lvl_str << "] " << msg << std::endl << std::flush;
}

} // namespace tarva
