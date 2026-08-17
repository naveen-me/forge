#include "wpe_html_renderer.h"
#include "logger.h"
#include <cassert>
#include <iostream>
#include <thread>
#include <chrono>

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::DEBUG);
    LOG_INFO("Testing WpeHtmlRenderer offscreen buffer capture...");

    int w = 800;
    int h = 600;
    tarva::WpeHtmlRenderer renderer(w, h);

    bool ok = renderer.initialize();
    assert(ok);

    std::string html = "<html><body style='margin:0; background-color: red;'><h1>TARVA</h1></body></html>";
    renderer.load_html(html, "http://localhost");

    std::vector<uint8_t> rgba_buf(w * h * 4, 0);

    for (int i = 0; i < 150; ++i) {
        g_main_context_iteration(NULL, FALSE);
        std::this_thread::sleep_for(std::chrono::milliseconds(10));
        if (renderer.capture_frame_rgba(rgba_buf.data(), w, h)) {
            uint8_t r = rgba_buf[0];
            uint8_t g = rgba_buf[1];
            if (r > 200 && g < 50) break;
        }
    }

    ok = renderer.capture_frame_rgba(rgba_buf.data(), w, h);
    assert(ok);

    uint8_t r = rgba_buf[0];
    uint8_t g = rgba_buf[1];
    uint8_t b = rgba_buf[2];
    uint8_t a = rgba_buf[3];

    LOG_INFO("Captured pixel at (0,0): R=" + std::to_string((int)r) +
             " G=" + std::to_string((int)g) +
             " B=" + std::to_string((int)b) +
             " A=" + std::to_string((int)a));

    assert(r > 200);
    assert(g < 50);

    LOG_INFO("WpeHtmlRenderer test passed successfully!");
    return 0;
}
