<template>
  <teleport to="body">
    <div v-if="open" @keydown.escape.window="$emit('close')" class="fixed inset-0 z-50 overflow-y-auto" role="dialog">
      <div class="flex items-end justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
        <div @click="$emit('close')" class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" aria-hidden="true"></div>
        <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
        <div class="inline-block align-bottom bg-card-light dark:bg-card-dark rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div class="bg-card-light dark:bg-card-dark px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <h3 class="text-lg leading-6 font-medium text-text-light dark:text-text-dark">Create New Folder</h3>
            <div class="mt-4">
              <input v-model="newFolderName" @keyup.enter="createFolder" type="text" placeholder="Folder name" class="w-full p-2 border rounded bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark border-border-light dark:border-border-dark focus:ring-primary focus:border-primary">
            </div>
          </div>
          <div class="bg-background-light dark:bg-background-dark px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button @click="createFolder" type="button" class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-primary text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary sm:ml-3 sm:w-auto sm:text-sm">Create</button>
            <button @click="$emit('close')" type="button" class="mt-3 w-full inline-flex justify-center rounded-md border border-border-light dark:border-border-dark shadow-sm px-4 py-2 bg-card-light dark:bg-card-dark text-base font-medium text-text-light dark:text-text-dark hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary sm:mt-0 sm:w-auto sm:text-sm">Cancel</button>
          </div>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref } from 'vue';

defineProps({
  open: {
    type: Boolean,
    required: true,
  },
});

const emit = defineEmits(['close', 'create']);

const newFolderName = ref('');

const createFolder = () => {
  if (newFolderName.value.trim()) {
    emit('create', newFolderName.value.trim());
    newFolderName.value = '';
  }
};
</script>
