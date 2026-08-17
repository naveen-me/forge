#include "logger.h"
#include "scene_schema.h"
#include "timeline_engine.h"
#include "source_manager.h"
#include "scene_controller.h"
#include "api_server.h"
#include "gpac_compositor.h"
#include "media_output.h"
#include "audio_mixer.h"
#include "runtime_stats.h"
#include "benchmark_harness.h"
#include "monotonic_scheduler.h"

#include <iostream>
#include <fstream>
#include <memory>
#include <chrono>
#include <thread>
#include <csignal>

static std::atomic<bool> g_running{true};
static void signal_handler(int sig) {
    g_running = false;
}

int main(int argc, char** argv) {
    std::signal(SIGINT, signal_handler);
    std::signal(SIGTERM, signal_handler);

    tarva::Logger::instance().set_level(tarva::LogLevel::INFO);
    LOG_INFO("================================================================================");
    LOG_INFO("TARVA Headless Playout Engine v0.1.0 Starting...");
    LOG_INFO("================================================================================");

    auto timeline = std::make_shared<tarva::TimelineEngine>();
    auto source_mgr = std::make_shared<tarva::SourceManager>();
    auto controller = std::make_shared<tarva::SceneController>(timeline, source_mgr);
    auto stats = std::make_shared<tarva::RuntimeStats>();

    // Initial default scene
    tarva::Scene default_scene;
    default_scene.canvas.width = 1920;
    default_scene.canvas.height = 1080;
    default_scene.canvas.fps = 30;
    default_scene.output.url = "/tmp/tarva_output.mp4";

    // Add default text layer
    tarva::Layer bg_text;
    bg_text.id = "main_title";
    bg_text.type = "text";
    bg_text.layer = 10;
    bg_text.x = 100;
    bg_text.y = 100;
    bg_text.width = 800;
    bg_text.height = 100;
    bg_text.text = "TARVA Playout Engine Online";
    bg_text.font_size = 40;
    bg_text.font_color = "#00FFCC";
    bg_text.start_ns = 0;

    default_scene.layers = { bg_text };
    controller->update_full_scene(default_scene);

    int api_port = 8080;
    if (const char* env_p = std::getenv("PORT")) {
        api_port = std::atoi(env_p);
    }

    tarva::ApiServer api_server(api_port, controller, stats);
    api_server.start();

    LOG_INFO("API Server running at http://0.0.0.0:" + std::to_string(api_port));
    LOG_INFO("Health Check: GET http://localhost:" + std::to_string(api_port) + "/health");
    LOG_INFO("Status Endpoint: GET http://localhost:" + std::to_string(api_port) + "/status");

    // Continuous playout loop
    int canvas_w = default_scene.canvas.width;
    int canvas_h = default_scene.canvas.height;
    int fps = default_scene.canvas.fps;
    int64_t frame_duration_ns = 1000000000LL / fps;

    tarva::GpacCompositor compositor(canvas_w, canvas_h, fps);
    tarva::MediaOutput output(canvas_w, canvas_h, fps);

    // Video + AAC audio output (audio stream is declared at initialize time so
    // the container can carry both streams).
    const int kAudioSampleRate = 48000;
    const int kAudioChannels = 2;
    if (!output.initialize(default_scene.output.url, true, kAudioSampleRate, kAudioChannels)) {
        LOG_ERROR("Failed to initialize output; shutting down.");
        api_server.stop();
        return 1;
    }
    stats->set_output_state(tarva::OutputState::RUNNING);

    std::vector<uint8_t> composite_frame(canvas_w * canvas_h * 4, 0);

    tarva::AudioMixer audio_mixer(kAudioSampleRate, kAudioChannels);
    const size_t kSamplesPerFrame =
        static_cast<size_t>(kAudioSampleRate) * static_cast<size_t>(frame_duration_ns) / 1000000000ULL;

    int64_t frame_idx = 0;
    tarva::MonotonicScheduler scheduler(fps);
    scheduler.start();

    while (g_running) {
        int64_t current_pts_ns = frame_idx * frame_duration_ns;
        stats->set_playout_time_ns(current_pts_ns);

        // Process scheduled runtime updates at frame boundary
        controller->process_scheduled_operations(current_pts_ns);

        // Resolve active layers for current frame
        std::vector<tarva::Layer> active_layers = timeline->resolve_active_layers(current_pts_ns);

        std::vector<tarva::RenderableLayer> renderable_layers;
        for (const auto& l : active_layers) {
            auto handle = source_mgr->get_source(l.id);
            if (!handle) {
                handle = source_mgr->prepare_source(l, canvas_w, canvas_h);
            }
            if (handle && handle->state == tarva::SourceState::READY && handle->media_source) {
                renderable_layers.push_back({ l, handle->media_source });
            }
        }

        if (compositor.render_frame(renderable_layers, current_pts_ns, composite_frame.data())) {
            stats->note_frame_rendered();
        } else {
            stats->note_frame_dropped();
        }

        if (output.send_frame_rgba(composite_frame.data(), frame_idx)) {
            stats->note_output_frame();
        } else {
            stats->set_output_state(tarva::OutputState::ERROR);
        }

        // Mix audio from active sources and feed the AAC stream. Sources without
        // audio contribute silence so the output stream stays continuous.
        std::vector<tarva::AudioPcmBuffer> audio_buffers;
        for (const auto& rl : renderable_layers) {
            auto vs = std::dynamic_pointer_cast<tarva::VideoSource>(rl.source);
            if (!vs || !vs->has_audio()) continue;

            tarva::AudioPcmBuffer buf;
            buf.sample_rate = kAudioSampleRate;
            buf.channels = kAudioChannels;
            buf.samples_s16.resize(kSamplesPerFrame * kAudioChannels, 0);
            size_t got = 0;
            if (vs->read_audio_s16(buf.samples_s16.data(), kSamplesPerFrame, current_pts_ns, got) && got > 0) {
                buf.samples_s16.resize(got * kAudioChannels);
                audio_buffers.push_back(std::move(buf));
            }
        }

        tarva::AudioPcmBuffer mixed;
        if (!audio_buffers.empty()) {
            audio_mixer.mix_pcm_buffers(audio_buffers, mixed, kSamplesPerFrame);
        } else {
            mixed.samples_s16.assign(kSamplesPerFrame * kAudioChannels, 0); // silence
        }
        output.send_audio_s16(mixed.samples_s16.data(), kSamplesPerFrame, current_pts_ns);

        frame_idx++;

        // Monotonic deadline pacing
        scheduler.wait_for_frame_deadline(frame_idx);
    }

    output.finalize();
    stats->set_output_state(tarva::OutputState::FINALIZED);
    api_server.stop();

    LOG_INFO("TARVA Playout Engine shutdown complete.");
    return 0;
}
