#include "gpac_compositor.h"
#include "media_sources.h"
#include "logger.h"
#include <cassert>
#include <iostream>
#include <vector>
#include <cstdlib>

void generate_test_media() {
    std::system("ffmpeg -y -f lavfi -i color=c=red:s=1920x1080:d=1 -vframes 1 /tmp/bg_red.png > /dev/null 2>&1");
    std::system("ffmpeg -y -f lavfi -i color=c=blue:s=200x200:d=1 -vframes 1 /tmp/fg_blue.png > /dev/null 2>&1");
}

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::DEBUG);
    LOG_INFO("Testing GpacCompositor CPU 2D compositor...");

    generate_test_media();

    int canvas_w = 1920;
    int canvas_h = 1080;
    tarva::GpacCompositor compositor(canvas_w, canvas_h, 30);

    // Layer 0: Red background
    tarva::RenderableLayer bg_layer;
    bg_layer.layer_config.id = "bg";
    bg_layer.layer_config.layer = 0;
    bg_layer.layer_config.x = 0;
    bg_layer.layer_config.y = 0;
    bg_layer.layer_config.width = canvas_w;
    bg_layer.layer_config.height = canvas_h;
    bg_layer.source = std::make_shared<tarva::ImageSource>();
    bool ok = bg_layer.source->load("/tmp/bg_red.png", canvas_w, canvas_h);
    assert(ok);

    // Layer 10: Blue foreground rectangle at (500, 500)
    tarva::RenderableLayer fg_layer;
    fg_layer.layer_config.id = "fg";
    fg_layer.layer_config.layer = 10;
    fg_layer.layer_config.x = 500;
    fg_layer.layer_config.y = 500;
    fg_layer.layer_config.width = 200;
    fg_layer.layer_config.height = 200;
    fg_layer.source = std::make_shared<tarva::ImageSource>();
    ok = fg_layer.source->load("/tmp/fg_blue.png", 200, 200);
    assert(ok);

    // Layer 20: Text overlay
    tarva::RenderableLayer text_layer;
    text_layer.layer_config.id = "txt";
    text_layer.layer_config.layer = 20;
    text_layer.layer_config.x = 100;
    text_layer.layer_config.y = 100;
    text_layer.layer_config.width = 400;
    text_layer.layer_config.height = 100;
    text_layer.source = std::make_shared<tarva::TextSource>("CPU Compositor Test", 32);
    ok = text_layer.source->load("", 400, 100);
    assert(ok);

    std::vector<uint8_t> test_buf(canvas_w * canvas_h * 4, 0);
    bg_layer.source->read_frame_rgba(test_buf.data(), canvas_w, canvas_h, 0);
    LOG_INFO("Direct ImageSource pixel (10,10): R=" + std::to_string((int)test_buf[0]) +
             " G=" + std::to_string((int)test_buf[1]) +
             " B=" + std::to_string((int)test_buf[2]) +
             " A=" + std::to_string((int)test_buf[3]));

    std::vector<tarva::RenderableLayer> active_layers = { bg_layer, fg_layer, text_layer };

    std::vector<uint8_t> output_frame(canvas_w * canvas_h * 4, 0);
    ok = compositor.render_frame(active_layers, 0, output_frame.data());
    assert(ok);

    // Verify background pixel at (10, 10) is Red (r > 200, b < 50)
    uint8_t bg_r = output_frame[(10 * canvas_w + 10) * 4 + 0];
    uint8_t bg_g = output_frame[(10 * canvas_w + 10) * 4 + 1];
    uint8_t bg_b = output_frame[(10 * canvas_w + 10) * 4 + 2];
    uint8_t bg_a = output_frame[(10 * canvas_w + 10) * 4 + 3];
    LOG_INFO("BG pixel (10,10): R=" + std::to_string((int)bg_r) +
             " G=" + std::to_string((int)bg_g) +
             " B=" + std::to_string((int)bg_b) +
             " A=" + std::to_string((int)bg_a));
    assert(bg_r > 200 && bg_b < 50);

    // Verify foreground pixel at (550, 550) is Blue (b > 200, r < 50)
    uint8_t fg_r = output_frame[(550 * canvas_w + 550) * 4 + 0];
    uint8_t fg_g = output_frame[(550 * canvas_w + 550) * 4 + 1];
    uint8_t fg_b = output_frame[(550 * canvas_w + 550) * 4 + 2];
    assert(fg_b > 200 && fg_r < 50);

    LOG_INFO("GpacCompositor test passed successfully!");
    return 0;
}
