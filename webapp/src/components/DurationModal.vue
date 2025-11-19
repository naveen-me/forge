<template>
  <div class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-sm p-6">
      <h3 class="text-xl font-bold mb-4">Set Duration for Link</h3>
      <form @submit.prevent="save">
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Duration</label>
          <div class="flex gap-2">
            <div class="flex-1">
              <input
                type="number"
                v-model.number="hours"
                min="0"
                placeholder="HH"
                class="w-full px-3 py-2 text-center rounded-md border border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm"
              />
              <p class="text-xs text-gray-500 text-center mt-1">Hours</p>
            </div>
            <div class="flex items-center self-end pb-3">:</div>
            <div class="flex-1">
              <input
                type="number"
                v-model.number="minutes"
                min="0"
                max="59"
                placeholder="MM"
                class="w-full px-3 py-2 text-center rounded-md border border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm"
              />
              <p class="text-xs text-gray-500 text-center mt-1">Minutes</p>
            </div>
            <div class="flex items-center self-end pb-3">:</div>
            <div class="flex-1">
              <input
                type="number"
                v-model.number="seconds"
                min="0"
                max="59"
                placeholder="SS"
                class="w-full px-3 py-2 text-center rounded-md border border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm"
              />
              <p class="text-xs text-gray-500 text-center mt-1">Seconds</p>
            </div>
          </div>
        </div>

        <div class="mb-4 p-3 bg-gray-50 rounded-md text-sm">
          <div class="text-gray-700">Total duration: <span class="font-medium text-primary">{{ formatDuration(totalSeconds) }}</span> ({{ totalSeconds }} seconds)</div>
        </div>

        <div class="flex justify-end gap-4">
          <button type="button" @click="$emit('close')" class="px-4 py-2 rounded-md text-gray-600 bg-gray-100 hover:bg-gray-200">
            Cancel
          </button>
          <button type="submit" class="px-4 py-2 rounded-md bg-primary text-white" :disabled="!isValidDuration">
            Add to Schedule
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DurationModal',
  data() {
    return {
      hours: 0,
      minutes: 5, // Default to 5 minutes
      seconds: 0,
    };
  },
  computed: {
    totalSeconds() {
      return (this.hours * 3600) + (this.minutes * 60) + this.seconds;
    },
    isValidDuration() {
      return this.totalSeconds > 0;
    }
  },
  methods: {
    formatDuration(totalSeconds) {
      const hours = Math.floor(totalSeconds / 3600);
      const minutes = Math.floor((totalSeconds % 3600) / 60);
      const seconds = totalSeconds % 60;

      const hoursStr = hours.toString().padStart(2, '0');
      const minutesStr = minutes.toString().padStart(2, '0');
      const secondsStr = seconds.toString().padStart(2, '0');

      if (hours > 0) {
        return `${hoursStr}:${minutesStr}:${secondsStr}`;
      }
      return `${minutesStr}:${secondsStr}`;
    },
    save() {
      if (this.isValidDuration) {
        this.$emit('save', this.totalSeconds);
      }
    },
  },
};
</script>
