#ifndef TARVA_AUDIO_MIXER_H
#define TARVA_AUDIO_MIXER_H

#include <vector>
#include <memory>
#include <mutex>
#include <cstdint>

extern "C" {
#include <libswresample/swresample.h>
#include <libavutil/samplefmt.h>
#include <libavutil/channel_layout.h>
}

namespace tarva {

struct AudioPcmBuffer {
    std::vector<int16_t> samples_s16; // Interleaved stereo S16 PCM
    int sample_rate = 48000;
    int channels = 2;
};

class AudioMixer {
public:
    AudioMixer(int sample_rate = 48000, int channels = 2);
    ~AudioMixer();

    // Mixes multiple PCM audio buffers into a single destination buffer
    bool mix_pcm_buffers(const std::vector<AudioPcmBuffer>& input_buffers,
                         AudioPcmBuffer& output_buffer,
                         size_t required_samples_per_channel);

    int sample_rate() const { return sample_rate_; }
    int channels() const { return channels_; }

private:
    int sample_rate_ = 48000;
    int channels_ = 2;
    std::mutex mixer_mutex_;
};

} // namespace tarva

#endif // TARVA_AUDIO_MIXER_H
