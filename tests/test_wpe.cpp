#include "wpe_html_renderer.h"
#include "logger.h"
#include <cassert>
#include <iostream>
#include <vector>

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::DEBUG);
    LOG_INFO("Testing WpeHtmlRenderer offscreen buffer capture...");

    int width = 800;
    int height = 600;

    tarva::WpeHtmlRenderer renderer(width, height);
    bool ok = renderer.initialize();
    assert(ok);

    std::string test_html = R"(
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { background-color: rgb(255, 0, 0); margin: 0; padding: 0; }
                h1 { color: white; font-family: sans-serif; font-size: 40px; margin: 20px; }
            </style>
        </head>
        <body>
            <h1>TARVA Playout HTML Layer Test</h1>
        </body>
        </html>
    )";

    ok = renderer.load_html(test_html);
    assert(ok);

    std::vector<uint8_t> frame_buffer(width * height * 4, 0);
    ok = renderer.capture_frame_rgba(frame_buffer.data(), width, height);
    assert(ok);

    // Verify background pixel (0,0) is Red (r=255, g=0, b=0)
    uint8_t r = frame_buffer[0];
    uint8_t g = frame_buffer[1];
    uint8_t b = frame_buffer[2];
    uint8_t a = frame_buffer[3];

    LOG_INFO("Captured pixel at (0,0): R=" + std::to_string((int)r) +
             " G=" + std::to_string((int)g) +
             " B=" + std::to_string((int)b) +
             " A=" + std::to_string((int)a));

    assert(r == 255);
    assert(g == 0);
    assert(b == 0);

    LOG_INFO("WpeHtmlRenderer offscreen buffer test passed successfully!");
    return 0;
}
