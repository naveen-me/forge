<template>
  <teleport to="body">
    <div v-if="open" @keydown.escape.window="$emit('close')" class="fixed inset-0 z-50 overflow-y-auto" role="dialog">
      <div class="flex items-end justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
        <div @click="$emit('close')" class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" aria-hidden="true"></div>
        <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
        <div class="inline-block align-bottom bg-card-light dark:bg-card-dark rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div class="bg-card-light dark:bg-card-dark px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div class="sm:flex sm:items-start">
              <div class="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-blue-100 dark:bg-blue-900/30 sm:mx-0 sm:h-10 sm:w-10">
                <span class="material-symbols-outlined text-primary dark:text-blue-400">drive_file_move</span>
              </div>
              <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left w-full">
                <h3 class="text-lg leading-6 font-medium text-text-light dark:text-text-dark">Move {{ itemCount }} items</h3>
                <div class="mt-4">
                  <p class="text-sm text-subtext-light dark:text-subtext-dark mb-2">Select a destination group:</p>
                  <select v-model="destinationGroupId" class="w-full mt-2 p-2 border rounded bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark border-border-light dark:border-border-dark focus:ring-primary focus:border-primary">
                    <option :value="null">Ads (Root)</option>
                    <option v-for="group in groups" :key="group.id" :value="group.id">{{ group.name }}</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
          <div class="bg-background-light dark:bg-background-dark px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button @click="confirm" type="button" class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-primary text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary sm:ml-3 sm:w-auto sm:text-sm">Move</button>
            <button @click="$emit('close')" type="button" class="mt-3 w-full inline-flex justify-center rounded-md border border-border-light dark:border-border-dark shadow-sm px-4 py-2 bg-card-light dark:bg-card-dark text-base font-medium text-text-light dark:text-text-dark hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary sm:mt-0 sm:w-auto text-sm">Cancel</button>
          </div>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, watch } from 'vue';
import adService from '../../services/adService';

const props = defineProps({
  open: { type: Boolean, required: true },
  itemCount: { type: Number, default: 0 },
});

const emit = defineEmits(['close', 'confirm']);

const destinationGroupId = ref(null);
const groups = ref([]);

const fetchGroups = async () => {
  try {
    const response = await adService.getAds(null); // Fetch root level ads/groups
    groups.value = response.data.filter(item => item.type === 'group');
  } catch (error) {
    console.error("Error fetching groups for move modal:", error);
  }
};

watch(() => props.open, (isOpen) => {
  if (isOpen) {
    fetchGroups();
  }
});

const confirm = () => {
  emit('confirm', destinationGroupId.value);
};
</script>
