#include "gpac_compositor.h"
#include "media_sources.h"
#include "media_output.h"
#include "benchmark_harness.h"
#include "logger.h"
#include <iostream>
#include <vector>
#include <cstdlib>
#include <thread>
#include <queue>
#include <condition_variable>

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::INFO);
    LOG_INFO("================================================================================");
    LOG_INFO("EXECUTING MANDATORY PHASE 1 POC GATE BENCHMARK (1080p30 CPU PLayout)");
    LOG_INFO("================================================================================");

    // 1. Prepare test media
    std::system("ffmpeg -y -f lavfi -i testsrc=size=1920x1080:rate=30 -t 5 /tmp/poc_bg_video.mp4 > /dev/null 2>&1");
    std::system("ffmpeg -y -f lavfi -i color=c=blue:s=300x150:d=1 -vframes 1 /tmp/poc_logo.png > /dev/null 2>&1");

    int width = 1920;
    int height = 1080;
    int fps = 30;
    int duration_sec = 5;
    int total_target_frames = duration_sec * fps;

    // 2. Instantiate components
    tarva::GpacCompositor compositor(width, height, fps);
    tarva::MediaOutput output(width, height, fps);
    tarva::BenchmarkHarness harness(width, height, fps);

    if (!output.initialize("/tmp/poc_output_1080p30.mp4")) {
        LOG_ERROR("POC Gate: Output initialization failed");
        return 1;
    }

    // Layer 0: MP4 Video background
    tarva::RenderableLayer video_layer;
    video_layer.layer_config.id = "bg-video";
    video_layer.layer_config.layer = 0;
    video_layer.layer_config.width = width;
    video_layer.layer_config.height = height;
    video_layer.source = std::make_shared<tarva::VideoSource>();
    if (!video_layer.source->load("/tmp/poc_bg_video.mp4", width, height)) {
        LOG_ERROR("POC Gate: Failed to load background video");
        return 1;
    }

    // Layer 10: PNG Image logo
    tarva::RenderableLayer logo_layer;
    logo_layer.layer_config.id = "logo-png";
    logo_layer.layer_config.layer = 10;
    logo_layer.layer_config.x = 1580;
    logo_layer.layer_config.y = 50;
    logo_layer.layer_config.width = 300;
    logo_layer.layer_config.height = 150;
    logo_layer.source = std::make_shared<tarva::ImageSource>();
    if (!logo_layer.source->load("/tmp/poc_logo.png", 300, 150)) {
        LOG_ERROR("POC Gate: Failed to load logo image");
        return 1;
    }

    // Layer 20: WPE Offscreen HTML layer
    tarva::RenderableLayer html_layer;
    html_layer.layer_config.id = "ticker-html";
    html_layer.layer_config.layer = 20;
    html_layer.layer_config.x = 100;
    html_layer.layer_config.y = 900;
    html_layer.layer_config.width = 1720;
    html_layer.layer_config.height = 120;
    html_layer.source = std::make_shared<tarva::HtmlSource>();

    std::string ticker_html = R"(
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body {
                    background: rgba(0, 0, 0, 0.7);
                    color: #00FFCC;
                    font-family: sans-serif;
                    margin: 0;
                    padding: 15px;
                    border-left: 10px solid #00FFCC;
                }
                h2 { margin: 0; font-size: 28px; }
                p { margin: 5px 0 0 0; font-size: 20px; color: #FFFFFF; }
            </style>
        </head>
        <body>
            <h2>TARVA Headless Playout Engine</h2>
            <p>Phase 1 POC Gate Validation — Direct Offscreen Raw Buffer Pipeline</p>
        </body>
        </html>
    )";

    if (!html_layer.source->load(ticker_html, 1720, 120)) {
        LOG_ERROR("POC Gate: Failed to load offscreen HTML layer");
        return 1;
    }

    // Layer 30: Text layer
    tarva::RenderableLayer text_layer;
    text_layer.layer_config.id = "overlay-text";
    text_layer.layer_config.layer = 30;
    text_layer.layer_config.x = 50;
    text_layer.layer_config.y = 50;
    text_layer.layer_config.width = 500;
    text_layer.layer_config.height = 80;
    text_layer.source = std::make_shared<tarva::TextSource>("LIVE 1080p30", 32, "#FF0000");
    text_layer.source->load("", 500, 80);

    std::vector<tarva::RenderableLayer> scene_layers = { video_layer, logo_layer, html_layer, text_layer };

    // 3. Pipelined Execution Loop (Compositor Thread + Encoder Worker Thread)
    struct QueuedFrame {
        int64_t frame_idx;
        std::vector<uint8_t> rgba_data;
    };

    std::queue<QueuedFrame> frame_queue;
    std::mutex queue_mutex;
    std::condition_variable queue_cv;
    bool producer_done = false;

    // Start encoder worker thread
    std::thread encoder_thread([&]() {
        while (true) {
            QueuedFrame qf;
            {
                std::unique_lock<std::mutex> lock(queue_mutex);
                queue_cv.wait(lock, [&]() { return !frame_queue.empty() || producer_done; });

                if (frame_queue.empty() && producer_done) break;

                qf = std::move(frame_queue.front());
                frame_queue.pop();
            }

            if (!output.send_frame_rgba(qf.rgba_data.data(), qf.frame_idx)) {
                harness.record_dropped_frame();
            }
        }
    });

    harness.start();

    for (int frame_idx = 0; frame_idx < total_target_frames; ++frame_idx) {
        auto t_start = std::chrono::steady_clock::now();

        int64_t pts_ns = (frame_idx * 1000000000LL) / fps;

        QueuedFrame qf;
        qf.frame_idx = frame_idx;
        qf.rgba_data.resize(width * height * 4);

        bool render_ok = compositor.render_frame(scene_layers, pts_ns, qf.rgba_data.data());
        if (!render_ok) {
            harness.record_dropped_frame();
            continue;
        }

        {
            std::lock_guard<std::mutex> lock(queue_mutex);
            frame_queue.push(std::move(qf));
        }
        queue_cv.notify_one();

        auto t_end = std::chrono::steady_clock::now();
        double frame_ms = std::chrono::duration_cast<std::chrono::microseconds>(t_end - t_start).count() / 1000.0;
        harness.record_frame_time(frame_ms);
    }

    {
        std::lock_guard<std::mutex> lock(queue_mutex);
        producer_done = true;
    }
    queue_cv.notify_one();

    if (encoder_thread.joinable()) {
        encoder_thread.join();
    }

    harness.stop();
    output.finalize();

    // 4. Save results report
    std::system("mkdir -p benchmarks");
    std::string report_path = "benchmarks/poc_results.json";
    harness.save_report_json(report_path);

    tarva::BenchmarkMetrics m = harness.get_metrics();
    LOG_INFO("================================================================================");
    LOG_INFO("POC GATE BENCHMARK RESULTS:");
    LOG_INFO("  Canvas Resolution: " + std::to_string(m.canvas_width) + "x" + std::to_string(m.canvas_height));
    LOG_INFO("  Total Frames:     " + std::to_string(m.total_frames));
    LOG_INFO("  Elapsed Time:     " + std::to_string(m.elapsed_sec) + " seconds");
    LOG_INFO("  Rendered FPS:     " + std::to_string(m.rendered_fps));
    LOG_INFO("  Avg Frame Time:   " + std::to_string(m.avg_frame_time_ms) + " ms");
    LOG_INFO("  Dropped Frames:   " + std::to_string(m.dropped_frames));
    LOG_INFO("  CPU Usage %:      " + std::to_string(m.cpu_usage_pct) + "%");
    LOG_INFO("  RAM RSS Usage:    " + std::to_string(m.ram_rss_mb) + " MB");
    LOG_INFO("================================================================================");

    if (m.rendered_fps >= 30.0 && m.dropped_frames == 0) {
        LOG_INFO("PHASE 1 POC GATE PASSED SUCCESSFULLY (30+ FPS ENFORCED)!");
        return 0;
    } else {
        LOG_ERROR("PHASE 1 POC GATE CRITERIA FAILED (Target 30 FPS not met)!");
        return 1;
    }
}
