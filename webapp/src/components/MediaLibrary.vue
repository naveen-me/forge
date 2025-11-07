<template>
  <div class="p-6 bg-background-light dark:bg-background-dark font-sans">
    <!-- Header -->
    <div class="flex flex-col md:flex-row justify-between md:items-center mb-4 gap-3">
      <h1 class="text-2xl font-bold text-text-light dark:text-text-dark">Library</h1>
      <div class="flex flex-col md:flex-row gap-2">
        <button @click="newFolderModalOpen = true" class="flex items-center justify-center gap-2 px-3 py-2 rounded-md border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark text-sm">
          <span class="material-icons text-base">create_new_folder</span>
          <span>New Folder</span>
        </button>
        <button @click="handleAddFiles" :disabled="isAddingFiles" class="flex items-center justify-center gap-2 px-3 py-2 rounded-md bg-primary text-white text-sm" :class="{'opacity-50 cursor-not-allowed': isAddingFiles}">
          <span class="material-icons text-base">add</span>
          <span>{{ isAddingFiles ? 'Adding...' : 'Add Files' }}</span>
        </button>
      </div>
    </div>

    <!-- Breadcrumbs -->
    <div class="mb-4">
      <nav aria-label="Breadcrumb" class="flex">
        <ol class="inline-flex items-center space-x-1 md:space-x-2 rtl:space-x-reverse">
          <li class="inline-flex items-center">
            <a @click="navigateToFolder(null)" href="#" class="inline-flex items-center text-xs font-medium text-subtext-light hover:text-primary dark:text-subtext-dark dark:hover:text-white">
              <span class="material-icons text-base mr-1.5">folder</span> Media Library
            </a>
          </li>
          <li v-for="folder in store.currentPath" :key="folder.id">
            <div class="flex items-center">
              <span class="material-icons text-subtext-light dark:text-subtext-dark text-base">chevron_right</span>
              <a @click="navigateToFolder(folder.id)" href="#" class="ms-1 text-xs font-medium text-subtext-light hover:text-primary md:ms-2 dark:text-subtext-dark dark:hover:text-white">{{ folder.name }}</a>
            </div>
          </li>
        </ol>
      </nav>
    </div>

    <!-- Search, Sort, and View Controls -->
    <div class="flex flex-col md:flex-row gap-4 mb-4">
      <div class="w-full flex-grow flex flex-col sm:flex-row gap-2">
        <div class="relative flex-grow">
          <span class="material-icons absolute left-3 top-1/2 -translate-y-1/2 text-subtext-light dark:text-subtext-dark text-lg">search</span>
          <input v-model="searchQuery" @input="handleSearch" type="text" placeholder="Search" class="w-full pl-10 pr-4 py-2 rounded-md border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark focus:ring-primary focus:border-primary text-sm h-full" />
        </div>
        <div class="flex gap-2 justify-end">
          <div class="relative">
            <button @click="sortOpen = !sortOpen" class="flex items-center gap-2 px-3 py-2 rounded-md border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark text-sm h-full">
              <span class="material-icons text-base">sort</span>
              <span>Sort By</span>
              <span class="material-icons text-base">expand_more</span>
            </button>
            <div v-if="sortOpen" @click.outside="sortOpen = false" class="absolute z-10 mt-1 w-56 bg-card-light dark:bg-card-dark rounded-lg shadow-lg border border-border-light dark:border-border-dark">
              <!-- Sorting options can be implemented here -->
            </div>
          </div>
          <div class="flex items-center border border-border-light dark:border-border-dark rounded-md bg-card-light dark:bg-card-dark h-full">
            <button @click="view = 'grid'" :class="{ 'bg-gray-200 dark:bg-gray-600 rounded-l-md': view === 'grid' }" class="p-2 text-text-light dark:text-text-dark h-full">
              <span class="material-icons text-xl">grid_view</span>
            </button>
            <button @click="view = 'list'" :class="{ 'bg-gray-200 dark:bg-gray-600 rounded-r-md': view === 'list' }" class="p-2 text-text-light dark:text-text-dark h-full">
              <span class="material-icons text-xl">list</span>
            </button>
          </div>
        </div>
      </div>
      <div class="w-full" v-if="store.selectedItems.length > 0">
        <div class="flex flex-col sm:flex-row justify-between items-center p-2 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
          <div class="flex items-center gap-2 mb-2 sm:mb-0 sm:mr-4 whitespace-nowrap">
            <p class="font-medium text-primary dark:text-blue-300 text-sm">{{ store.selectedItems.length }} items selected</p>
          </div>
          <div class="flex gap-1.5 flex-wrap justify-center">
            <button @click="copyModalOpen = true" class="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-white dark:bg-card-dark text-text-light dark:text-text-dark border border-border-light dark:border-border-dark shadow-sm text-xs">
              <span class="material-icons text-base">content_copy</span>
              <span>Copy</span>
            </button>
            <button @click="moveModalOpen = true" class="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-white dark:bg-card-dark text-text-light dark:text-text-dark border border-border-light dark:border-border-dark shadow-sm text-xs">
              <span class="material-icons text-base">drive_file_move</span>
              <span>Move</span>
            </button>
            <button @click="deleteModalOpen = true" class="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-red-600 text-white text-xs">
              <span class="material-icons text-base">delete</span>
              <span>Delete</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Folders Section -->
    <div class="mb-6">
      <h2 class="text-lg font-semibold text-text-light dark:text-text-dark mb-3">Folders</h2>
      <div v-if="view === 'grid'" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
        <div v-for="folder in store.folders" :key="folder.id" @click="toggleSelection(folder.id)" @dblclick="navigateToFolder(folder.id)" :class="{'bg-blue-50 dark:bg-blue-900/20': isSelected(folder.id)}" class="relative group bg-card-light dark:bg-card-dark rounded-lg shadow-around border-2 border-transparent p-2.5 flex items-center gap-3 cursor-pointer">
          <input type="checkbox" :checked="isSelected(folder.id)" class="form-checkbox h-4 w-4 rounded-full text-primary bg-white/50 border-gray-400/70 focus:ring-0 focus:ring-offset-0" />
          <span class="material-icons text-subtext-light dark:text-subtext-dark text-2xl">folder</span>
          <div class="flex-grow">
            <p class="font-semibold text-text-light dark:text-text-dark truncate text-sm">{{ folder.name }}</p>
          </div>
        </div>
      </div>
      <div v-if="view === 'list'" class="bg-card-light dark:bg-card-dark rounded-lg shadow-sm text-sm">
        <div v-for="folder in store.folders" :key="folder.id" @click="toggleSelection(folder.id)" @dblclick="navigateToFolder(folder.id)" :class="{'bg-blue-50 dark:bg-blue-900/20': isSelected(folder.id)}" class="flex items-center p-2.5 hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg cursor-pointer">
          <input type="checkbox" :checked="isSelected(folder.id)" class="form-checkbox h-4 w-4 rounded-full text-primary bg-gray-100 border-gray-300 focus:ring-0 focus:ring-offset-0 dark:bg-gray-600 dark:border-gray-500" />
          <span class="material-icons text-subtext-light dark:text-subtext-dark text-xl mx-3">folder</span>
          <span class="font-semibold text-text-light dark:text-text-dark flex-grow">{{ folder.name }}</span>
        </div>
      </div>
    </div>

    <!-- Videos Section -->
    <div>
      <h2 class="text-lg font-semibold text-text-light dark:text-text-dark mb-3">Videos</h2>
      <div v-if="view === 'grid'" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
        <div v-for="video in store.videos" :key="video.id" :class="{'bg-blue-50 dark:bg-blue-900/20': isSelected(video.id), 'opacity-60': video.isMissing}" class="relative group bg-card-light dark:bg-card-dark rounded-lg shadow-around overflow-hidden border-2 border-transparent">
          <input type="checkbox" :checked="isSelected(video.id)" @click.stop="toggleSelection(video.id)" class="form-checkbox h-4 w-4 rounded-full text-primary bg-white/50 border-gray-400/70 focus:ring-0 focus:ring-offset-0 absolute top-2.5 left-2.5 z-10" />
          <div class="aspect-video bg-gray-200 dark:bg-gray-700 flex items-center justify-center relative">
            <div v-if="!video.isMissing" @click="playVideo(video)" class="absolute inset-0 bg-black/20 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer">
              <button class="bg-white/30 backdrop-blur-sm rounded-full p-2 text-white w-12 h-12 flex items-center justify-center">
                <span class="material-icons text-3xl">play_arrow</span>
              </button>
            </div>
            <div v-if="video.isMissing" class="absolute inset-0 bg-black/50 flex items-center justify-center text-white">
              <span class="material-icons text-lg">error_outline</span>
            </div>
          </div>
          <div class="p-2.5">
            <p class="font-semibold text-text-light dark:text-text-dark truncate text-sm">{{ video.name }}</p>
            <p class="text-xs text-subtext-light dark:text-subtext-dark mt-1">{{ video.mimeType }} · {{ formatFileSize(video.size) }}</p>
            <span v-if="video.isMissing" class="text-red-500 dark:text-red-400 text-xs font-semibold mt-1 inline-block">Source Missing</span>
          </div>
        </div>
      </div>
      <div v-if="view === 'list'" class="bg-card-light dark:bg-card-dark rounded-lg shadow-sm overflow-hidden text-sm">
        <table class="w-full text-left text-subtext-light dark:text-subtext-dark">
          <!-- Table Head -->
          <tbody>
            <tr v-for="video in store.videos" :key="video.id" :class="{'bg-blue-50 dark:bg-blue-900/20': isSelected(video.id), 'opacity-60': video.isMissing}" class="border-t dark:border-border-dark hover:bg-gray-50 dark:hover:bg-gray-800">
              <td class="p-3"><input type="checkbox" :checked="isSelected(video.id)" @change.stop="toggleSelection(video.id)" class="form-checkbox h-4 w-4 rounded-full text-primary bg-gray-100 border-gray-300 focus:ring-0 focus:ring-offset-0 dark:bg-gray-600 dark:border-gray-500" /></td>
              <td class="p-3 font-medium text-text-light dark:text-text-dark">
                <div class="flex items-center gap-3">
                  <div class="relative group w-16 h-10 rounded-md overflow-hidden bg-gray-200 dark:bg-gray-700 flex-shrink-0">
                     <div v-if="!video.isMissing" @click="playVideo(video)" class="absolute inset-0 bg-black/20 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer">
                      <button class="bg-white/30 backdrop-blur-sm rounded-full w-8 h-8 flex items-center justify-center text-white">
                        <span class="material-icons text-xl">play_arrow</span>
                      </button>
                    </div>
                     <div v-if="video.isMissing" class="absolute inset-0 bg-black/50 flex items-center justify-center text-white">
                        <span class="material-icons text-lg">error_outline</span>
                    </div>
                  </div>
                  <span>{{ video.name }}</span>
                </div>
              </td>
              <td class="p-3">{{ video.mimeType }}</td>
              <td class="p-3">{{ video.dimensions }}</td>
              <td class="p-3">{{ formatFileSize(video.size) }}</td>
              <td class="p-3 text-right">
                <span v-if="video.isMissing" class="text-red-500 dark:text-red-400 text-xs font-semibold bg-red-100 dark:bg-red-900/30 px-2 py-1 rounded-full">Source Missing</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modals -->
    <DeleteModal :open="deleteModalOpen" @close="deleteModalOpen = false" @delete="handleDelete" :item-count="store.selectedItems.length" />
    <MoveCopyModal :open="moveModalOpen || copyModalOpen" :mode="moveModalOpen ? 'move' : 'copy'" @close="closeMoveCopyModal" @confirm="handleMoveCopy" :item-count="store.selectedItems.length" />
    <NewFolderModal :open="newFolderModalOpen" @close="newFolderModalOpen = false" @create="submitNewFolder" />
    
    <!-- Video Player Popup -->
    <div v-if="playingVideo" @click="closeVideoPlayer" class="fixed inset-0 bg-black/80 flex items-center justify-center z-50">
      <div @click.stop class="bg-card-dark rounded-lg shadow-2xl w-full max-w-4xl relative">
        <div class="aspect-video">
          <video class="w-full h-full rounded-t-lg" controls autoplay>
            <source :src="`http://localhost:3001/api/stream/video/${playingVideo.id}`" :type="playingVideo.mimeType" />
            Your browser does not support the video tag.
          </video>
        </div>
        <div class="p-6">
          <h3 class="text-2xl font-bold text-text-dark">{{ playingVideo.name }}</h3>
          <div class="flex items-center gap-4 text-lg text-subtext-dark mt-2">
            <span>{{ playingVideo.mimeType }}</span>
            <span>·</span>
            <span>{{ playingVideo.dimensions }}</span>
            <span>·</span>
            <span>{{ formatFileSize(playingVideo.size) }}</span>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useMediaStore } from '../stores/media';
import { mediaService as api } from '../services/api';

// Import modal components
import NewFolderModal from './modals/NewFolderModal.vue';
import DeleteModal from './modals/DeleteModal.vue';
import MoveCopyModal from './modals/MoveCopyModal.vue';

const store = useMediaStore();

const view = ref('grid');
const sortOpen = ref(false);
const deleteModalOpen = ref(false);
const moveModalOpen = ref(false);
const copyModalOpen = ref(false);
const newFolderModalOpen = ref(false);
const playingVideo = ref(null);
const searchQuery = ref('');
const isAddingFiles = ref(false);

onMounted(() => {
  store.fetchFolderContents();
  store.fetchPath();
});

const isSelected = (itemId) => store.selectedItems.includes(itemId);
const toggleSelection = (itemId) => store.toggleItemSelected(itemId);

const navigateToFolder = (folderId) => {
  store.setCurrentFolderId(folderId);
};

const submitNewFolder = async (folderName) => {
  if (folderName) {
    await store.createFolder(folderName, store.currentFolderId);
    newFolderModalOpen.value = false;
  }
};

const handleAddFiles = async () => {
    isAddingFiles.value = true;
    try {
        const response = await api.selectFiles();
        const filePaths = response.data.files;
        if (filePaths && filePaths.length > 0) {
            const filesToAdd = filePaths.map(path => ({ path }));
            await store.addFiles(filesToAdd, store.currentFolderId);
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
  copyModalOpen.value = false;
};

const handleMoveCopy = async (destinationFolderId) => {
    if (moveModalOpen.value) {
        await store.moveItems(store.selectedItems, destinationFolderId);
    } else {
        console.log("Copying not yet implemented");
    }
    closeMoveCopyModal();
};

const handleSearch = () => {
    store.search(searchQuery.value);
};

const playVideo = (video) => {
  if (!video.isMissing) {
    playingVideo.value = video;
  }
};
const closeVideoPlayer = () => { playingVideo.value = null; };

const formatFileSize = (bytes) => {
  if (!bytes) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};
</script>

<style>
.form-checkbox:focus,
.form-checkbox:checked:focus {
    outline: none !important;
    box-shadow: none !important;
    border-color: #007AFF !important;
}
</style>
