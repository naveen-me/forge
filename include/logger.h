#ifndef TARVA_LOGGER_H
#define TARVA_LOGGER_H

#include <string>
#include <iostream>
#include <mutex>
#include <sstream>

namespace tarva {

enum class LogLevel {
    DEBUG = 0,
    INFO,
    WARN,
    ERR
};

class Logger {
public:
    static Logger& instance();
    void set_level(LogLevel level);
    LogLevel level() const;
    void log(LogLevel level, const std::string& msg);

private:
    Logger() = default;
    LogLevel level_ = LogLevel::INFO;
    std::mutex mutex_;
};

#define LOG_DEBUG(msg) ::tarva::Logger::instance().log(::tarva::LogLevel::DEBUG, msg)
#define LOG_INFO(msg)  ::tarva::Logger::instance().log(::tarva::LogLevel::INFO, msg)
#define LOG_WARN(msg)  ::tarva::Logger::instance().log(::tarva::LogLevel::WARN, msg)
#define LOG_ERROR(msg) ::tarva::Logger::instance().log(::tarva::LogLevel::ERR, msg)

} // namespace tarva

#endif // TARVA_LOGGER_H
