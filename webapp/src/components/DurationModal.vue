<template>
  <div class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-sm p-6">
      <h3 class="text-xl font-bold mb-4">Set Duration for Link</h3>
      <form @submit.prevent="save">
        <div class="mb-4">
          <label for="duration" class="block text-sm font-medium text-gray-700">Duration (in seconds)</label>
          <input
            type="number"
            id="duration"
            v-model.number="duration"
            min="1"
            required
            class="mt-1 block w-full px-3 py-2 rounded-md border border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm"
          />
        </div>
        <div class="flex justify-end gap-4">
          <button type="button" @click="$emit('close')" class="px-4 py-2 rounded-md text-gray-600 bg-gray-100 hover:bg-gray-200">
            Cancel
          </button>
          <button type="submit" class="px-4 py-2 rounded-md bg-primary text-white">
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
      duration: 300, // Default to 5 minutes
    };
  },
  methods: {
    save() {
      this.$emit('save', this.duration);
    },
  },
};
</script>
