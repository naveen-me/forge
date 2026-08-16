#include "audio_mixer.h"
#include "logger.h"
#include <cmath>
#include <algorithm>

namespace tarva {

AudioMixer::AudioMixer(int sample_rate, int channels)
    : sample_rate_(sample_rate), channels_(channels) {}

AudioMixer::~AudioMixer() {}

bool AudioMixer::mix_pcm_buffers(const std::vector<AudioPcmBuffer>& input_buffers,
                                AudioPcmBuffer& output_buffer,
                                size_t required_samples_per_channel) {
    std::lock_guard<std::mutex> lock(mixer_mutex_);

    size_t total_samples = required_samples_per_channel * channels_;
    output_buffer.sample_rate = sample_rate_;
    output_buffer.channels = channels_;
    output_buffer.samples_s16.assign(total_samples, 0);

    if (input_buffers.empty()) {
        // Output silence
        return true;
    }

    std::vector<int32_t> mix_accumulator(total_samples, 0);

    for (const auto& buf : input_buffers) {
        size_t count = std::min(total_samples, buf.samples_s16.size());
        for (size_t i = 0; i < count; ++i) {
            mix_accumulator[i] += buf.samples_s16[i];
        }
    }

    // Clamp and copy to output S16 buffer
    for (size_t i = 0; i < total_samples; ++i) {
        int32_t sample = mix_accumulator[i];
        if (sample > 32767) sample = 32767;
        if (sample < -32768) sample = -32768;
        output_buffer.samples_s16[i] = static_cast<int16_t>(sample);
    }

    return true;
}

} // namespace tarva
