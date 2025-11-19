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
          <label for="duration" class="block text-sm font-medium text-gray-700">Duration (seconds)</label>
          <input type="number" id="duration" v-model="editableItem.duration" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm" />
        </div>
        <div class="flex justify-end gap-2">
          <button type="button" @click="$emit('close')" class="px-4 py-2 rounded-md text-gray-600 bg-gray-100 hover:bg-gray-200">Cancel</button>
          <button type="submit" class="px-4 py-2 rounded-md bg-primary text-white">Save</button>
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
        duration: 0,
      },
    };
  },
  watch: {
    item: {
      immediate: true,
      handler(newItem) {
        if (newItem) {
          this.editableItem.start_time = this.formatDateForInput(newItem.start_time);
          this.editableItem.duration = this.getDurationInSeconds(newItem);
        }
      },
    },
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
    save() {
      const end_time = new Date(new Date(this.editableItem.start_time).getTime() + this.editableItem.duration * 1000);
      this.$emit('save', {
        ...this.item,
        start_time: new Date(this.editableItem.start_time).toISOString(),
        end_time: end_time.toISOString(),
      });
    },
  },
};
</script>
