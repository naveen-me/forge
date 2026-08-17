#include "media_sources.h"
#include "media_output.h"
#include "audio_mixer.h"
#include "logger.h"
#include <cassert>
#include <cmath>
#include <cstdlib>
#include <cstring>
#include <iostream>
#include <string>
#include <vector>

namespace {

std::string run_capture(const std::string& cmd) {
    std::string result;
    FILE* pipe = popen(cmd.c_str(), "r");
    if (!pipe) return result;
    char buf[256];
    size_t n = 0;
    while ((n = fread(buf, 1, sizeof(buf) - 1, pipe)) > 0) {
        buf[n] = '\0';
        result += buf;
    }
    pclose(pipe);
    return result;
}

void generate_test_media() {
    std::system("ffmpeg -y -f lavfi -i testsrc=size=640x360:rate=30 "
                "-f lavfi -i sine=frequency=440:sample_rate=48000 "
                "-t 2 -c:v libx264 -pix_fmt yuv420p -c:a aac "
                "/tmp/test_av.mp4 > /dev/null 2>&1");
}

void test_video_source_audio_decode() {
    tarva::VideoSource vs;
    bool ok = vs.load("/tmp/test_av.mp4", 640, 360);
    assert(ok);
    assert(vs.has_audio());
    assert(vs.audio_sample_rate() == 48000);
    assert(vs.audio_channels() == 2);

    // Read one frame's worth of audio (33.3 ms @ 48 kHz = 1600 sample frames).
    const size_t kSamples = 1600;
    std::vector<int16_t> pcm(kSamples * 2, 0);
    size_t got = 0;
    ok = vs.read_audio_s16(pcm.data(), kSamples, 0, got);
    assert(ok);
    assert(got > 0);

    // The 440 Hz sine must not be silence.
    double energy = 0.0;
    for (size_t i = 0; i < got * 2; ++i) {
        energy += std::abs(static_cast<double>(pcm[i]));
    }
    assert(energy > 0.0);
    LOG_INFO("VideoSource audio decode test passed (got " + std::to_string(got) +
             " sample frames, energy=" + std::to_string(energy) + ")");
}

void test_media_output_audio_stream() {
    const int width = 320;
    const int height = 180;
    const int fps = 30;
    const std::string output_path = "/tmp/test_audio_output.mp4";

    tarva::MediaOutput output(width, height, fps);
    bool ok = output.initialize(output_path, true, 48000, 2);
    assert(ok);
    assert(output.has_audio());

    // 30 video frames (1 second) with a matching second of 440 Hz sine audio.
    std::vector<uint8_t> frame(width * height * 4, 0);
    for (int i = 0; i < 30; ++i) {
        ok = output.send_frame_rgba(frame.data(), i);
        assert(ok);
    }

    const size_t kSamplesPerFrame = 1600; // 33.3 ms @ 48 kHz
    std::vector<int16_t> sine(kSamplesPerFrame * 2, 0);
    const double kFreq = 440.0;
    for (size_t i = 0; i < kSamplesPerFrame; ++i) {
        int16_t v = static_cast<int16_t>(12000.0 * std::sin(2.0 * M_PI * kFreq * i / 48000.0));
        sine[i * 2 + 0] = v;
        sine[i * 2 + 1] = v;
    }
    for (int i = 0; i < 30; ++i) {
        ok = output.send_audio_s16(sine.data(), kSamplesPerFrame, i * 33333333LL);
        assert(ok);
    }

    output.finalize();
    assert(output.frames_sent() == 30);
    assert(output.audio_samples_sent() >= 48000 - 1024); // ~1 second of audio

    // Verify with ffprobe that the file really carries an AAC audio stream.
    std::string probe = run_capture(
        "ffprobe -v error -select_streams a -show_entries stream=codec_name -of csv=p=0 " + output_path);
    assert(probe.find("aac") != std::string::npos);
    LOG_INFO("MediaOutput AAC stream test passed (ffprobe: " + probe + ")");
}

void test_audio_mixer_multi_source() {
    tarva::AudioMixer mixer(48000, 2);
    tarva::AudioPcmBuffer a;
    a.samples_s16 = { 1000, 1000, 2000, 2000 };
    tarva::AudioPcmBuffer b;
    b.samples_s16 = { 500, 500, 1000, 1000 };

    tarva::AudioPcmBuffer mixed;
    bool ok = mixer.mix_pcm_buffers({ a, b }, mixed, 4);
    assert(ok);
    assert(mixed.samples_s16.size() == 8);
    assert(mixed.samples_s16[0] == 1500);
    assert(mixed.samples_s16[6] == 3000);
    LOG_INFO("AudioMixer multi-source mix test passed");
}

} // namespace

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::DEBUG);
    LOG_INFO("Testing audio pipeline (decode -> mix -> AAC mux)...");

    generate_test_media();
    test_video_source_audio_decode();
    test_media_output_audio_stream();
    test_audio_mixer_multi_source();

    LOG_INFO("All audio pipeline tests passed successfully!");
    return 0;
}
