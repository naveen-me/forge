<template>
  <div class="p-6 bg-background-light dark:bg-background-dark font-sans">
    <!-- Header -->
    <div class="flex flex-col md:flex-row justify-between md:items-center mb-4 gap-3">
      <h1 class="text-2xl font-bold text-text-light dark:text-text-dark">Ads</h1>
      <div class="flex flex-col md:flex-row gap-2">
        <button v-if="!store.currentGroupId" @click="newGroupModalOpen = true" class="flex items-center justify-center gap-2 px-3 py-2 rounded-md border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark text-sm">
          <span class="material-symbols-outlined text-base">create_new_folder</span>
          <span>New Group</span>
        </button>
        <button @click="handleAddFiles" :disabled="isAddingFiles" class="flex items-center justify-center gap-2 px-3 py-2 rounded-md bg-primary text-white text-sm" :class="{'opacity-50 cursor-not-allowed': isAddingFiles}">
          <span class="material-symbols-outlined text-base">add</span>
          <span>{{ isAddingFiles ? 'Adding...' : 'Add Files' }}</span>
        </button>
      </div>
    </div>

    <!-- Breadcrumbs -->
    <div class="mb-4">
      <nav aria-label="Breadcrumb" class="flex">
        <ol class="inline-flex items-center space-x-1 md:space-x-2 rtl:space-x-reverse p-0 pr-0">
          <li class="inline-flex items-center">
            <a @click="navigateToGroup(null)" href="#" class="inline-flex items-center text-xs font-medium text-subtext-light hover:text-primary dark:text-subtext-dark dark:hover:text-white">
              <span class="material-symbols-outlined text-base mr-1.5">folder</span> Ads
            </a>
          </li>
          <li v-for="group in store.currentPath" :key="group.id">
            <div class="flex items-center">
              <span class="material-symbols-outlined text-subtext-light dark:text-subtext-dark text-base">chevron_right</span>
              <a @click="navigateToGroup(group.id)" href="#" class="ms-1 text-xs font-medium text-subtext-light hover:text-primary md:ms-2 dark:text-subtext-dark dark:hover:text-white">{{ group.name }}</a>
            </div>
          </li>
        </ol>
      </nav>
    </div>

    <!-- Search, Sort, and View Controls -->
    <div class="flex flex-col md:flex-row mb-4">
      <div class="w-full md:w-1/2 flex flex-col sm:flex-row gap-2">
        <div class="relative flex-grow">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-subtext-light dark:text-subtext-dark text-lg">search</span>
          <input v-model="searchQuery" @input="handleSearch" type="text" placeholder="Search" class="w-full pl-10 pr-4 py-2 rounded-md border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark focus:ring-primary focus:border-primary text-sm h-full" />
        </div>
        <div class="flex gap-2 justify-end">
          <div class="relative" ref="sortDropdownRef">
            <button @click.stop="sortOpen = !sortOpen" class="flex items-center gap-2 px-3 py-2 rounded-md border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark text-sm h-full">
              <span class="material-symbols-outlined text-base">sort</span>
              <span>Sort By</span>
              <span class="material-symbols-outlined text-base">expand_more</span>
            </button>
            <div v-show="sortOpen" class="absolute z-10 mt-1 w-56 bg-card-light dark:bg-card-dark rounded-lg shadow-lg border border-border-light dark:border-border-dark">
              <ul class="py-1 text-sm">
                <li>
                  <a href="#" @click.prevent="setSort('name', 'asc'); sortOpen = false" class="flex items-center justify-between px-3 py-1.5 hover:bg-gray-100 dark:hover:bg-gray-700 text-text-light dark:text-text-dark">
                    <span class="flex items-center gap-2"><span class="material-symbols-outlined text-base">arrow_upward</span> Name</span>
                    <span v-if="currentSort.field === 'name' && currentSort.direction === 'asc'" class="material-symbols-outlined text-lg text-primary">check</span>
                  </a>
                </li>
                <li>
                  <a href="#" @click.prevent="setSort('name', 'desc'); sortOpen = false" class="flex items-center justify-between px-3 py-1.5 hover:bg-gray-100 dark:hover:bg-gray-700 text-text-light dark:text-text-dark">
                    <span class="flex items-center gap-2"><span class="material-symbols-outlined text-base">arrow_downward</span> Name</span>
                    <span v-if="currentSort.field === 'name' && currentSort.direction === 'desc'" class="material-symbols-outlined text-lg text-primary">check</span>
                  </a>
                </li>
                <li>
                  <a href="#" @click.prevent="setSort('dateCreated', 'desc'); sortOpen = false" class="flex items-center justify-between px-3 py-1.5 hover:bg-gray-100 dark:hover:bg-gray-700 text-text-light dark:text-text-dark">
                    Date Created
                    <span v-if="currentSort.field === 'dateCreated' && currentSort.direction === 'desc'" class="material-symbols-outlined text-lg text-primary">check</span>
                  </a>
                </li>
                <li>
                  <a href="#" @click.prevent="setSort('size', 'desc'); sortOpen = false" class="flex items-center justify-between px-3 py-1.5 hover:bg-gray-100 dark:hover:bg-gray-700 text-text-light dark:text-text-dark">
                    File Size
                    <span v-if="currentSort.field === 'size' && currentSort.direction === 'desc'" class="material-symbols-outlined text-lg text-primary">check</span>
                  </a>
                </li>
                <li class="border-t border-border-light dark:border-border-dark">
                  <a href="#" @click.prevent="setSort('modified', 'desc'); sortOpen = false" class="flex items-center justify-between px-3 py-1.5 hover:bg-gray-100 dark:hover:bg-gray-700 text-text-light dark:text-text-dark font-semibold text-primary dark:text-white">
                    Last Modified
                    <span v-if="currentSort.field === 'modified' && currentSort.direction === 'desc'" class="material-symbols-outlined text-lg">check</span>
                  </a>
                </li>
              </ul>
            </div>
          </div>
          <div class="flex items-center border border-border-light dark:border-border-dark rounded-md bg-card-light dark:bg-card-dark h-full">
            <button @click="view = 'grid'" :class="{ 'bg-gray-200 dark:bg-gray-600 rounded-l-md': view === 'grid' }" class="p-2 text-text-light dark:text-text-dark h-full">
              <span class="material-symbols-outlined text-xl">grid_view</span>
            </button>
            <button @click="view = 'list'" :class="{ 'bg-gray-200 dark:bg-gray-600 rounded-r-md': view === 'list' }" class="p-2 text-text-light dark:text-text-dark h-full">
              <span class="material-symbols-outlined text-xl">list</span>
            </button>
          </div>
        </div>
      </div>
      <div class="w-full md:w-1/2" :class="{'md:ml-4': store.selectedItems.length > 0}" v-if="store.selectedItems.length > 0">
        <div class="flex flex-col sm:flex-row justify-between items-center bg-blue-100 dark:bg-blue-900/30 rounded-lg">
          <div class="flex items-center gap-2 sm:mb-0 sm:mr-4 h-full whitespace-nowrap">
            <p class="font-medium text-primary dark:text-blue-300 text-sm m-0 px-3">{{ store.selectedItems.length }} items selected</p>
          </div>
          <div class="flex gap-1.5 flex-wrap justify-center p-2">
            <button @click="moveModalOpen = true" class="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-white dark:bg-card-dark text-text-light dark:text-text-dark border border-border-light dark:border-border-dark shadow-sm text-xs">
              <span class="material-symbols-outlined text-base">drive_file_move</span>
              <span>Move</span>
            </button>
            <button @click="deleteModalOpen = true" class="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-red-600 text-white text-xs">
              <span class="material-symbols-outlined text-base">delete</span>
              <span>Delete</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Groups Section -->
    <div class="mb-6">
      <h2 class="text-lg font-semibold text-text-light dark:text-text-dark mb-3">Groups</h2>
      <div v-if="view === 'grid'" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
        <div v-for="group in store.groups" :key="group.id" @dblclick="navigateToGroup(group.id)" :class="{'bg-blue-50 dark:bg-blue-900/20': isSelected(group.id)}" class="relative group bg-card-light dark:bg-card-dark rounded-lg shadow-around border-2 border-transparent p-2.5 flex items-center gap-3 cursor-pointer">
          <input type="checkbox" :checked="isSelected(group.id)" @click.stop="toggleSelection(group.id)" class="form-checkbox h-4 w-4 rounded-full text-primary bg-white/50 border-gray-400/70 focus:ring-0 focus:ring-offset-0 absolute top-2.5 left-2.5 z-10" />
          <span class="material-symbols-outlined text-subtext-light dark:text-subtext-dark text-2xl">folder</span>
          <div class="flex-grow">
            <input v-if="renamingItemId === group.id" v-model="renamingText" @blur="finishRenaming" @keyup.enter="finishRenaming" @keyup.esc="cancelRenaming" type="text" class="font-semibold text-text-light dark:text-text-dark bg-gray-100 dark:bg-gray-800 rounded-md px-2 py-0.5 border border-primary focus:outline-none focus:ring-1 focus:ring-primary w-full text-sm" />
            <p v-else @click="startRenaming(group)" class="font-semibold text-text-light dark:text-text-dark truncate text-sm cursor-pointer">{{ group.name }}</p>
          </div>
        </div>
      </div>
      <div v-if="view === 'list'" class="bg-card-light dark:bg-card-dark rounded-lg shadow-sm text-sm">
        <div v-for="group in store.groups" :key="group.id" @dblclick="navigateToGroup(group.id)" :class="{'bg-blue-50 dark:bg-blue-900/20': isSelected(group.id)}" class="flex items-center p-2.5 hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg cursor-pointer">
          <input type="checkbox" :checked="isSelected(group.id)" @click.stop="toggleSelection(group.id)" class="form-checkbox h-4 w-4 rounded-full text-primary bg-gray-100 border-gray-300 focus:ring-0 focus:ring-offset-0 dark:bg-gray-600 dark:border-gray-500" />
          <span class="material-symbols-outlined text-subtext-light dark:text-subtext-dark text-xl mx-3">folder</span>
          <input v-if="renamingItemId === group.id" v-model="renamingText" @blur="finishRenaming" @keyup.enter="finishRenaming" @keyup.esc="cancelRenaming" type="text" class="font-semibold text-text-light dark:text-text-dark bg-gray-100 dark:bg-gray-800 rounded-md px-2 py-0.5 border border-primary focus:outline-none focus:ring-1 focus:ring-primary w-full flex-grow text-sm" />
          <span v-else @click="startRenaming(group)" class="font-semibold text-text-light dark:text-text-dark flex-grow cursor-pointer">{{ group.name }}</span>
        </div>
      </div>
    </div>

    <!-- Ads Section -->
    <div>
      <h2 class="text-lg font-semibold text-text-light dark:text-text-dark mb-3">Ads</h2>
      <div v-if="view === 'grid'" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
        <div v-for="ad in store.ads" :key="ad.id" :class="{'bg-blue-50 dark:bg-blue-900/20': isSelected(ad.id), 'opacity-60': ad.isMissing}" class="relative group bg-card-light dark:bg-card-dark rounded-lg shadow-around overflow-hidden border-2 border-transparent">
          <input type="checkbox" :checked="isSelected(ad.id)" @click.stop="toggleSelection(ad.id)" class="form-checkbox h-4 w-4 rounded-full text-primary bg-white/50 border-gray-400/70 focus:ring-0 focus:ring-offset-0 absolute top-2.5 left-2.5 z-10" />
          <div class="aspect-video bg-gray-200 dark:bg-gray-700 flex items-center justify-center relative">
            <img v-if="ad.thumbnailPath" :src="`http://localhost:3001${ad.thumbnailPath}`" class="w-full h-full object-cover" />
            <span v-else class="material-symbols-outlined text-gray-400 dark:text-gray-500 text-4xl opacity-50 group-hover:opacity-20 transition-opacity">movie</span>
            <div v-if="ad.isMissing" class="absolute inset-0 bg-black/50 flex items-center justify-center text-white">
              <span class="material-symbols-outlined text-lg">error_outline</span>
            </div>
            <span v-if="ad.dimensions" class="absolute bottom-1.5 right-1.5 bg-black/50 text-white text-xs px-1.5 py-0.5 rounded">{{ ad.dimensions }}</span>
          </div>
          <div class="p-2.5">
            <input v-if="renamingItemId === ad.id" v-model="renamingText" @blur="finishRenaming" @keyup.enter="finishRenaming" @keyup.esc="cancelRenaming" type="text" class="font-semibold text-text-light dark:text-text-dark bg-gray-100 dark:bg-gray-800 rounded-md px-2 py-0.5 border border-primary focus:outline-none focus:ring-1 focus:ring-primary w-full text-sm" />
            <p v-else @click="startRenaming(ad)" class="font-semibold text-text-light dark:text-text-dark truncate text-sm cursor-pointer">{{ ad.name }}</p>
            <p class="text-xs text-subtext-light dark:text-subtext-dark mt-1">{{ ad.extension ? ad.extension.toUpperCase() : (ad.mimeType && ad.mimeType.includes('/') ? ad.mimeType.split('/')[1].toUpperCase() : (ad.mimeType ? ad.mimeType.split(',')[0].toUpperCase() : '')) }} · {{ formatFileSize(ad.size) }}</p>
            <span v-if="ad.isMissing" class="text-red-500 dark:text-red-400 text-xs font-semibold mt-1 inline-block">Source Missing</span>
          </div>
        </div>
      </div>
      <div v-if="view === 'list'" class="bg-card-light dark:bg-card-dark rounded-lg shadow-sm overflow-hidden text-sm">
        <table class="w-full text-left text-subtext-light dark:text-subtext-dark">
          <thead class="text-xs text-subtext-light dark:text-subtext-dark uppercase bg-gray-50 dark:bg-gray-700">
            <tr>
              <th class="p-3" scope="col"><input class="form-checkbox h-4 w-4 rounded-full text-primary bg-gray-100 border-gray-300 focus:ring-0 focus:ring-offset-0 dark:bg-gray-600 dark:border-gray-500" type="checkbox"/></th>
              <th class="p-3" scope="col">File Name</th>
              <th class="p-3" scope="col">Format</th>
              <th class="p-3" scope="col">Dimensions</th>
              <th class="p-3" scope="col">File Size</th>
              <th class="p-3" scope="col"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="ad in store.ads" :key="ad.id" :class="{'bg-blue-50 dark:bg-blue-900/20': isSelected(ad.id), 'opacity-60': ad.isMissing}" class="border-t dark:border-border-dark hover:bg-gray-50 dark:hover:bg-gray-800 group">
              <td class="p-3"><input type="checkbox" :checked="isSelected(ad.id)" @change.stop="toggleSelection(ad.id)" class="form-checkbox h-4 w-4 rounded-full text-primary bg-gray-100 border-gray-300 focus:ring-0 focus:ring-offset-0 dark:bg-gray-600 dark:border-gray-500" /></td>
              <td class="p-3 font-medium text-text-light dark:text-text-dark">
                <div class="flex items-center gap-3">
                  <div class="relative group w-16 h-10 rounded-md overflow-hidden bg-gray-200 dark:bg-gray-700 flex-shrink-0">
                     <img v-if="ad.thumbnailPath" :src="`http://localhost:3001${ad.thumbnailPath}`" class="w-full h-full object-cover" />
                     <span v-else class="material-symbols-outlined text-gray-400 dark:text-gray-500 text-2xl absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 opacity-50 group-hover:opacity-20 transition-opacity">movie</span>
                     <div v-if="ad.isMissing" class="absolute inset-0 bg-black/50 flex items-center justify-center text-white">
                        <span class="material-symbols-outlined text-lg">error_outline</span>
                    </div>
                  </div>
                  <input v-if="renamingItemId === ad.id" v-model="renamingText" @blur="finishRenaming" @keyup.enter="finishRenaming" @keyup.esc="cancelRenaming" type="text" class="font-semibold text-text-light dark:text-text-dark bg-gray-100 dark:bg-gray-800 rounded-md px-2 py-0.5 border border-primary focus:outline-none focus:ring-1 focus:ring-primary w-full text-sm" />
                  <span v-else @click="startRenaming(ad)" class="cursor-pointer">{{ ad.name }}</span>
                </div>
              </td>
              <td class="p-3">{{ ad.extension ? ad.extension.toUpperCase() : (ad.mimeType && ad.mimeType.includes('/') ? ad.mimeType.split('/')[1].toUpperCase() : (ad.mimeType ? ad.mimeType.split(',')[0].toUpperCase() : '')) }}</td>
              <td class="p-3">{{ ad.dimensions || '-' }}</td>
              <td class="p-3">{{ formatFileSize(ad.size) }}</td>
              <td class="p-3 text-right">
                <span v-if="ad.isMissing" class="text-red-500 dark:text-red-400 text-xs font-semibold bg-red-100 dark:bg-red-900/30 px-2 py-1 rounded-full">Source Missing</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modals -->
    <DeleteModal :open="deleteModalOpen" @close="deleteModalOpen = false" @delete="handleDelete" :item-count="store.selectedItems.length" />
    <MoveCopyAdModal :open="moveModalOpen" @close="closeMoveCopyModal" @confirm="handleMoveCopy" :item-count="store.selectedItems.length" />
    <NewFolderModal :open="newGroupModalOpen" @close="newGroupModalOpen = false" @create="submitNewGroup" />
    
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useAdStore } from '../stores/ads';
import adService from '../services/adService';

// Import modal components
import NewFolderModal from './modals/NewFolderModal.vue';
import DeleteModal from './modals/DeleteModal.vue';
import MoveCopyAdModal from './modals/MoveCopyAdModal.vue';

const store = useAdStore();

const view = ref('grid');
const sortOpen = ref(false);
const deleteModalOpen = ref(false);
const moveModalOpen = ref(false);
const newGroupModalOpen = ref(false);
const searchQuery = ref('');
const isAddingFiles = ref(false);
const renamingItemId = ref(null);
const renamingText = ref('');
const currentSort = ref({ field: 'modified', direction: 'desc' });
const sortDropdownRef = ref(null);

onMounted(() => {
  store.init();
  store.fetchGroupContents();
  store.fetchPath();
});

const isSelected = (itemId) => store.selectedItems.includes(itemId);
const toggleSelection = (itemId) => store.toggleItemSelected(itemId);

const navigateToGroup = (groupId) => {
  store.setCurrentGroupId(groupId);
};

const startRenaming = (item) => {
  renamingItemId.value = item.id;
  renamingText.value = item.name;
};

const finishRenaming = async () => {
  if (renamingItemId.value && renamingText.value) {
    await store.renameItem(renamingItemId.value, renamingText.value);
  }
  renamingItemId.value = null;
  renamingText.value = '';
};

const cancelRenaming = () => {
  renamingItemId.value = null;
  renamingText.value = '';
};

const submitNewGroup = async (groupName) => {
  if (groupName) {
    await store.createGroup(groupName);
    newGroupModalOpen.value = false;
  }
};

const handleAddFiles = async () => {
    isAddingFiles.value = true;
    try {
        const response = await adService.selectFiles();
        const filePaths = response.data.files;
        if (filePaths && filePaths.length > 0) {
            const filesToAdd = filePaths.map(path => ({ path }));
            await store.addFiles(filesToAdd, store.currentGroupId);
        }
    } catch (error) {
        console.error("Error selecting or adding files:", error);
    } finally {
        isAddingFiles.value = false;
    }
};

const handleDelete = async () => {
  await store.deleteSelectedItems();
  deleteModalOpen.value = false;
};

const closeMoveCopyModal = () => {
  moveModalOpen.value = false;
};

const handleMoveCopy = async (destinationGroupId) => {
    await store.moveItems(store.selectedItems, destinationGroupId);
    closeMoveCopyModal();
};

const handleSearch = () => {
    store.search(searchQuery.value);
};

const formatFileSize = (bytes) => {
  if (!bytes) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const setSort = (field, direction) => {
  currentSort.value = { field, direction };
  // Apply sorting to store data
  store.sortItems(field, direction);
};

// Add click outside handling
const handleClickOutside = (event) => {
  if (!sortDropdownRef.value?.contains(event.target)) {
    sortOpen.value = false;
  }
};

// Add event listener when mounted and remove when component is unmounted
onMounted(() => {
  store.fetchGroupContents();
  store.fetchPath();
  document.addEventListener('click', handleClickOutside);
});

// Clean up event listener when component is unmounted
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<style>
.form-checkbox:focus,
.form-checkbox:checked:focus {
    outline: none !important;
    box-shadow: none !important;
    border-color: #007AFF !important;
}

/* Shadow styles as defined in the design */
.shadow-subtle {
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.05) !important;
}

.shadow-around {
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.1) !important;
}
</style>
