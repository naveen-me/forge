#include "audio_mixer.h"
#include "gpac_compositor.h"
#include "media_sources.h"
#include "logger.h"
#include <cassert>
#include <iostream>
#include <vector>

void test_audio_mixer() {
    tarva::AudioMixer mixer(48000, 2);

    tarva::AudioPcmBuffer buf1;
    buf1.sample_rate = 48000;
    buf1.channels = 2;
    buf1.samples_s16 = { 1000, 1000, 2000, 2000 };

    tarva::AudioPcmBuffer buf2;
    buf2.sample_rate = 48000;
    buf2.channels = 2;
    buf2.samples_s16 = { 500, 500, 1000, 1000 };

    tarva::AudioPcmBuffer mixed_output;
    bool ok = mixer.mix_pcm_buffers({ buf1, buf2 }, mixed_output, 2);
    assert(ok);
    assert(mixed_output.samples_s16.size() == 4);
    assert(mixed_output.samples_s16[0] == 1500);
    assert(mixed_output.samples_s16[1] == 1500);
    assert(mixed_output.samples_s16[2] == 3000);
    assert(mixed_output.samples_s16[3] == 3000);

    LOG_INFO("AudioMixer test passed successfully!");
}

void test_layer_effects() {
    int canvas_w = 1920;
    int canvas_h = 1080;
    tarva::GpacCompositor compositor(canvas_w, canvas_h, 30);

    // Layer with fade effect (2 seconds = 2e9 ns)
    tarva::RenderableLayer text_layer;
    text_layer.layer_config.id = "fade_txt";
    text_layer.layer_config.layer = 10;
    text_layer.layer_config.x = 100;
    text_layer.layer_config.y = 100;
    text_layer.layer_config.width = 400;
    text_layer.layer_config.height = 100;
    text_layer.layer_config.start_ns = 0;
    text_layer.layer_config.opacity = 1.0;

    tarva::Effect fade_eff;
    fade_eff.type = "fade";
    fade_eff.duration_ns = 2LL * 1000000000LL;
    text_layer.layer_config.effect = fade_eff;

    text_layer.source = std::make_shared<tarva::TextSource>("Fading Text", 32);
    text_layer.source->load("", 400, 100);

    std::vector<uint8_t> frame_buf(canvas_w * canvas_h * 4, 0);

    // Render at t = 0s (fade opacity should be 0)
    bool ok = compositor.render_frame({ text_layer }, 0, frame_buf.data());
    assert(ok);

    // Render at t = 1s (1e9 ns) (fade opacity should be 0.5)
    ok = compositor.render_frame({ text_layer }, 1LL * 1000000000LL, frame_buf.data());
    assert(ok);

    // Render at t = 2s (2e9 ns) (fade opacity should be 1.0)
    ok = compositor.render_frame({ text_layer }, 2LL * 1000000000LL, frame_buf.data());
    assert(ok);

    LOG_INFO("Layer effects test passed successfully!");
}

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::DEBUG);
    LOG_INFO("Testing AudioMixer and Layer Effects...");

    test_audio_mixer();
    test_layer_effects();

    LOG_INFO("All AudioMixer and Layer Effects tests passed successfully!");
    return 0;
}
