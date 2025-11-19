<template>
  <div class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-xl font-bold">Edit Schedule Item</h3>
        <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
      <form @submit.prevent="save" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700">Item</label>
          <p class="mt-1 text-lg">{{ item.item?.name || item.item?.title || item.item_type }}</p>
        </div>
        <div>
          <label for="start_time" class="block text-sm font-medium text-gray-700">Start Time</label>
          <input type="datetime-local" id="start_time" v-model="editableItem.start_time" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Duration</label>
          <div class="flex gap-2 mb-4">
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

        <div class="p-3 bg-gray-50 rounded-md text-sm mb-4">
          <div class="text-gray-700">Total duration: <span class="font-medium text-primary">{{ formatDuration(totalSeconds) }}</span> ({{ totalSeconds }} seconds)</div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Video Start Offset</label>
          <div class="flex gap-2">
            <div class="flex-1">
              <input
                type="number"
                v-model.number="offset_hours"
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
                v-model.number="offset_minutes"
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
                v-model.number="offset_seconds"
                min="0"
                max="59"
                placeholder="SS"
                class="w-full px-3 py-2 text-center rounded-md border border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm"
              />
              <p class="text-xs text-gray-500 text-center mt-1">Seconds</p>
            </div>
          </div>
          <div class="mt-2 text-xs text-gray-500">The time in the video where playback should start (e.g., skip intro)</div>
        </div>

        <div class="p-3 bg-gray-50 rounded-md text-sm">
          <div class="text-gray-700">Video start offset: <span class="font-medium text-primary">{{ formatDuration(totalOffsetSeconds) }}</span> ({{ totalOffsetSeconds }} seconds)</div>
        </div>

        <div class="flex justify-end gap-2">
          <button type="button" @click="$emit('close')" class="px-4 py-2 rounded-md text-gray-600 bg-gray-100 hover:bg-gray-200">Cancel</button>
          <button type="submit" class="px-4 py-2 rounded-md bg-primary text-white" :disabled="!isValidDuration">Save</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ScheduleItemModal',
  props: {
    item: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      editableItem: {
        start_time: '',
      },
      hours: 0,
      minutes: 0,
      seconds: 0,
      offset_hours: 0,
      offset_minutes: 0,
      offset_seconds: 0,
    };
  },
  computed: {
    totalSeconds() {
      return (this.hours * 3600) + (this.minutes * 60) + this.seconds;
    },
    totalOffsetSeconds() {
      return (this.offset_hours * 3600) + (this.offset_minutes * 60) + this.offset_seconds;
    },
    isValidDuration() {
      return this.totalSeconds > 0;
    }
  },
  watch: {
    item: {
      immediate: true,
      handler(newItem) {
        if (newItem && this.editableItem) {
          this.editableItem.start_time = this.formatDateForInput(newItem.start_time);
          const duration = this.getDurationInSeconds(newItem);
          this.updateDurationFields(duration);
          this.updateOffsetFields(newItem.offset_time || 0);
        }
      },
    },
    // Watch for changes to duration fields and update internal state
    hours() { this.updateDurationField(); },
    minutes() { this.updateDurationField(); },
    seconds() { this.updateDurationField(); },
    // Watch for changes to offset fields and update internal state
    offset_hours() { this.updateOffsetField(); },
    offset_minutes() { this.updateOffsetField(); },
    offset_seconds() { this.updateOffsetField(); },
  },
  methods: {
    formatDateForInput(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      return `${year}-${month}-${day}T${hours}:${minutes}`;
    },
    getDurationInSeconds(item) {
      if (!item.start_time || !item.end_time) return 0;
      const start = new Date(item.start_time);
      const end = new Date(item.end_time);
      const diff = end.getTime() - start.getTime();
      return Math.round(diff / 1000);
    },
    updateDurationFields(seconds) {
      this.hours = Math.floor(seconds / 3600);
      this.minutes = Math.floor((seconds % 3600) / 60);
      this.seconds = seconds % 60;
    },
    updateOffsetFields(seconds) {
      this.offset_hours = Math.floor(seconds / 3600);
      this.offset_minutes = Math.floor((seconds % 3600) / 60);
      this.offset_seconds = seconds % 60;
    },
    updateDurationField() {
      // This is just to trigger the computed property when components change
    },
    updateOffsetField() {
      // This is just to trigger the computed property when components change
    },
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
        this.$emit('save', {
          ...this.item,
          start_time: new Date(this.editableItem.start_time).toISOString(),
          duration: this.totalSeconds, // Send duration for API compatibility
          offset_time: this.totalOffsetSeconds, // Send offset time
        });
      }
    },
  },
};
</script>
