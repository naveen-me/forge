<template>
  <div class="p-6 bg-background-light dark:bg-background-dark font-sans">
    <!-- Header -->
    <div class="flex flex-col md:flex-row justify-between md:items-center mb-4 gap-3">
      <h1 class="text-2xl font-bold text-text-light dark:text-text-dark">Library</h1>
      <div class="flex flex-col md:flex-row gap-2">
        <button class="flex items-center justify-center gap-2 px-3 py-2 rounded-md border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark text-sm">
          <span class="material-icons text-base">create_new_folder</span>
          <span>New Folder</span>
        </button>
        <button class="flex items-center justify-center gap-2 px-3 py-2 rounded-md bg-primary text-white text-sm">
          <span class="material-icons text-base">add</span>
          <span>Add Files</span>
        </button>
      </div>
    </div>

    <!-- Breadcrumbs -->
    <div class="mb-4">
      <nav aria-label="Breadcrumb" class="flex">
        <ol class="inline-flex items-center space-x-1 md:space-x-2 rtl:space-x-reverse">
          <li class="inline-flex items-center">
            <a href="#" class="inline-flex items-center text-xs font-medium text-subtext-light hover:text-primary dark:text-subtext-dark dark:hover:text-white">
              <span class="material-icons text-base mr-1.5">folder</span> Media Library
            </a>
          </li>
          <li>
            <div class="flex items-center">
              <span class="material-icons text-subtext-light dark:text-subtext-dark text-base">chevron_right</span>
              <a href="#" class="ms-1 text-xs font-medium text-subtext-light hover:text-primary md:ms-2 dark:text-subtext-dark dark:hover:text-white">Client Projects</a>
            </div>
          </li>
          <li aria-current="page">
            <div class="flex items-center">
              <span class="material-icons text-subtext-light dark:text-subtext-dark text-base">chevron_right</span>
              <span class="ms-1 text-xs font-medium text-text-light md:ms-2 dark:text-text-dark">Brand Redesign</span>
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
          <input type="text" placeholder="Search" class="w-full pl-10 pr-4 py-2 rounded-md border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark focus:ring-primary focus:border-primary text-sm h-full" />
        </div>
        <div class="flex gap-2 justify-end">
          <div class="relative">
            <button @click="sortOpen = !sortOpen" class="flex items-center gap-2 px-3 py-2 rounded-md border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark text-sm h-full">
              <span class="material-icons text-base">sort</span>
              <span>Sort By</span>
              <span class="material-icons text-base">expand_more</span>
            </button>
            <div v-if="sortOpen" @click.outside="sortOpen = false" class="absolute z-10 mt-1 w-56 bg-card-light dark:bg-card-dark rounded-lg shadow-lg border border-border-light dark:border-border-dark">
              <ul class="py-1 text-sm">
                <li><a href="#" class="flex items-center justify-between px-3 py-1.5 hover:bg-gray-100 dark:hover:bg-gray-700 text-text-light dark:text-text-dark"><span class="flex items-center gap-2"><span class="material-icons text-base">arrow_upward</span> Name</span></a></li>
                <li><a href="#" class="flex items-center justify-between px-3 py-1.5 hover:bg-gray-100 dark:hover:bg-gray-700 text-text-light dark:text-text-dark"><span class="flex items-center gap-2"><span class="material-icons text-base">arrow_downward</span> Name</span></a></li>
                <li><a href="#" class="flex items-center justify-between px-3 py-1.5 hover:bg-gray-100 dark:hover:bg-gray-700 text-text-light dark:text-text-dark">Date Created</a></li>
                <li><a href="#" class="flex items-center justify-between px-3 py-1.5 hover:bg-gray-100 dark:hover:bg-gray-700 text-text-light dark:text-text-dark">File Size</a></li>
                <li class="bg-gray-200 dark:bg-gray-600"><a href="#" class="flex items-center justify-between px-3 py-1.5 font-semibold text-primary dark:text-white">Last Modified <span class="material-icons text-lg">check</span></a></li>
              </ul>
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
      <div class="w-full" v-if="selectedItems.length > 0">
        <div class="flex flex-col sm:flex-row justify-between items-center p-2 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
          <div class="flex items-center gap-2 mb-2 sm:mb-0 sm:mr-4 whitespace-nowrap">
            <p class="font-medium text-primary dark:text-blue-300 text-sm">{{ selectedItems.length }} items selected</p>
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
      <!-- Grid View -->
      <div v-if="view === 'grid'" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
        <div v-for="folder in folders" :key="folder.id" @click="toggleSelection(folder.id)" :class="{'bg-blue-50 dark:bg-blue-900/20': isSelected(folder.id)}" class="relative group bg-card-light dark:bg-card-dark rounded-lg shadow-around border-2 border-transparent p-2.5 flex items-center gap-3 cursor-pointer">
          <input type="checkbox" :checked="isSelected(folder.id)" class="form-checkbox h-4 w-4 rounded-full text-primary bg-white/50 border-gray-400/70 focus:ring-0 focus:ring-offset-0" />
          <span class="material-icons text-subtext-light dark:text-subtext-dark text-2xl">folder</span>
          <div class="flex-grow">
            <p class="font-semibold text-text-light dark:text-text-dark truncate text-sm">{{ folder.name }}</p>
            <p class="text-xs text-subtext-light dark:text-subtext-dark">{{ folder.itemCount }} videos</p>
          </div>
        </div>
      </div>
      <!-- List View -->
      <div v-if="view === 'list'" class="bg-card-light dark:bg-card-dark rounded-lg shadow-sm text-sm">
        <div class="space-y-px">
          <div v-for="folder in folders" :key="folder.id" @click="toggleSelection(folder.id)" :class="{'bg-blue-50 dark:bg-blue-900/20': isSelected(folder.id)}" class="flex items-center p-2.5 hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg cursor-pointer">
            <input type="checkbox" :checked="isSelected(folder.id)" class="form-checkbox h-4 w-4 rounded-full text-primary bg-gray-100 border-gray-300 focus:ring-0 focus:ring-offset-0 dark:bg-gray-600 dark:border-gray-500" />
            <span class="material-icons text-subtext-light dark:text-subtext-dark text-xl mx-3">folder</span>
            <span class="font-semibold text-text-light dark:text-text-dark flex-grow">{{ folder.name }}</span>
            <span class="text-xs text-subtext-light dark:text-subtext-dark">{{ folder.itemCount }} videos</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Videos Section -->
    <div>
      <h2 class="text-lg font-semibold text-text-light dark:text-text-dark mb-3">Videos</h2>
      <!-- Grid View -->
      <div v-if="view === 'grid'" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
        <div v-for="video in videos" :key="video.id" :class="{'bg-blue-50 dark:bg-blue-900/20': isSelected(video.id), 'opacity-60': video.status === 'missing'}" class="relative group bg-card-light dark:bg-card-dark rounded-lg shadow-around overflow-hidden border-2 border-transparent">
          <input type="checkbox" :checked="isSelected(video.id)" @click.stop="toggleSelection(video.id)" class="form-checkbox h-4 w-4 rounded-full text-primary bg-white/50 border-gray-400/70 focus:ring-0 focus:ring-offset-0 absolute top-2.5 left-2.5 z-10" />
          <div class="aspect-video bg-gray-200 dark:bg-gray-700 flex items-center justify-center relative">
            <span v-if="video.status !== 'missing'" class="material-icons text-blue-500 dark:text-blue-400 text-4xl opacity-50 group-hover:opacity-20 transition-opacity">movie</span>
            <div v-if="video.status !== 'missing'" @click="playVideo(video)" class="absolute inset-0 bg-black/20 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer">
              <button class="bg-white/30 backdrop-blur-sm rounded-full p-2 text-white w-12 h-12 flex items-center justify-center">
                <span class="material-icons text-3xl">play_arrow</span>
              </button>
            </div>
            <div v-if="video.status === 'missing'" class="absolute inset-0 bg-black/50 flex items-center justify-center text-white">
              <span class="material-icons text-lg">error_outline</span>
            </div>
            <span class="absolute bottom-1.5 right-1.5 bg-black/50 text-white text-xs px-1.5 py-0.5 rounded">{{ video.dimensions }}</span>
          </div>
          <div class="p-2.5">
            <p class="font-semibold text-text-light dark:text-text-dark truncate text-sm">{{ video.name }}</p>
            <p class="text-xs text-subtext-light dark:text-subtext-dark mt-1">{{ video.format }} · {{ video.size }}</p>
            <span v-if="video.status === 'missing'" class="text-red-500 dark:text-red-400 text-xs font-semibold mt-1 inline-block">Source Missing</span>
          </div>
        </div>
      </div>
      <!-- List View -->
      <div v-if="view === 'list'" class="bg-card-light dark:bg-card-dark rounded-lg shadow-sm overflow-hidden text-sm">
        <table class="w-full text-left text-subtext-light dark:text-subtext-dark">
          <thead class="text-xs text-subtext-light dark:text-subtext-dark uppercase bg-gray-50 dark:bg-gray-700">
            <tr>
              <th scope="col" class="p-3"><input type="checkbox" @change="toggleSelectAll" :checked="allSelected" class="form-checkbox h-4 w-4 rounded-full text-primary bg-gray-100 border-gray-300 focus:ring-0 focus:ring-offset-0 dark:bg-gray-600 dark:border-gray-500" /></th>
              <th scope="col" class="p-3">File Name</th>
              <th scope="col" class="p-3">Format</th>
              <th scope="col" class="p-3">Dimensions</th>
              <th scope="col" class="p-3">File Size</th>
              <th scope="col" class="p-3"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="video in videos" :key="video.id" :class="{'bg-blue-50 dark:bg-blue-900/20': isSelected(video.id), 'opacity-60': video.status === 'missing'}" class="border-t dark:border-border-dark hover:bg-gray-50 dark:hover:bg-gray-800">
              <td class="p-3"><input type="checkbox" :checked="isSelected(video.id)" @change.stop="toggleSelection(video.id)" class="form-checkbox h-4 w-4 rounded-full text-primary bg-gray-100 border-gray-300 focus:ring-0 focus:ring-offset-0 dark:bg-gray-600 dark:border-gray-500" /></td>
              <td class="p-3 font-medium text-text-light dark:text-text-dark">
                <div class="flex items-center gap-3">
                  <div class="relative group w-16 h-10 rounded-md overflow-hidden bg-gray-200 dark:bg-gray-700 flex-shrink-0">
                    <span v-if="video.status !== 'missing'" class="material-icons text-blue-500 dark:text-blue-400 text-2xl absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 opacity-50 group-hover:opacity-20 transition-opacity">movie</span>
                    <div v-if="video.status !== 'missing'" @click="playVideo(video)" class="absolute inset-0 bg-black/20 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer">
                      <button class="bg-white/30 backdrop-blur-sm rounded-full w-8 h-8 flex items-center justify-center text-white">
                        <span class="material-icons text-xl">play_arrow</span>
                      </button>
                    </div>
                     <div v-if="video.status === 'missing'" class="absolute inset-0 bg-black/50 flex items-center justify-center text-white">
                        <span class="material-icons text-lg">error_outline</span>
                    </div>
                  </div>
                  <span>{{ video.name }}</span>
                </div>
              </td>
              <td class="p-3">{{ video.format }}</td>
              <td class="p-3">{{ video.dimensions }}</td>
              <td class="p-3">{{ video.size }}</td>
              <td class="p-3 text-right">
                <span v-if="video.status === 'missing'" class="text-red-500 dark:text-red-400 text-xs font-semibold bg-red-100 dark:bg-red-900/30 px-2 py-1 rounded-full">Source Missing</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modals -->
    <DeleteModal :open="deleteModalOpen" @close="deleteModalOpen = false" @delete="handleDelete" :item-count="selectedItems.length" />
    <MoveCopyModal :open="moveModalOpen || copyModalOpen" :mode="moveModalOpen ? 'move' : 'copy'" @close="closeMoveCopyModal" @confirm="handleMoveCopy" :item-count="selectedItems.length" />

    <!-- Toasts -->
    <SuccessToast :show="showSuccessToast" :message="successMessage" />
    <ErrorToast :show="showErrorToast" :message="errorMessage" />
    
    <!-- Video Player Popup -->
    <div v-if="playingVideo" @click="closeVideoPlayer" class="fixed inset-0 bg-black/80 flex items-center justify-center z-40">
        <div @click.stop class="bg-card-dark rounded-lg shadow-2xl w-full max-w-4xl relative">
            <div class="aspect-video">
                <video class="w-full h-full rounded-t-lg" controls autoplay>
                    <source :src="playingVideo.url" type="video/mp4" />
                    Your browser does not support the video tag.
                </video>
            </div>
            <div class="p-6">
                <h3 class="text-2xl font-bold text-text-dark">{{ playingVideo.name }}</h3>
                <div class="flex items-center gap-4 text-lg text-subtext-dark mt-2">
                    <span>{{ playingVideo.format }}</span>
                    <span>·</span>
                    <span>{{ playingVideo.dimensions }}</span>
                    <span>·</span>
                    <span>{{ playingVideo.size }}</span>
                </div>
            </div>
        </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

// Dummy Data
const folders = ref([
  { id: 'f1', name: 'Client Projects', itemCount: 12 },
  { id: 'f2', name: 'Marketing Materials', itemCount: 8 },
  { id: 'f3', name: 'Tutorials', itemCount: 25 },
]);

const videos = ref([
  { id: 'v1', name: 'new_audio_channel', format: 'MP4', size: '2.5 GB', dimensions: '1920x1080', status: 'ok', url: 'https://www.w3schools.com/html/mov_bbb.mp4' },
  { id: 'v2', name: 'water', format: 'MOV', size: '1.2 GB', dimensions: '1280x720', status: 'ok', url: 'https://www.w3schools.com/html/mov_bbb.mp4' },
  { id: 'v3', name: 'JodhrajChannel', format: 'MP4', size: '3.1 GB', dimensions: '3840x2160', status: 'missing', url: '' },
  { id: 'v4', name: 'Audio check', format: 'MP4', size: '850 MB', dimensions: '1920x1080', status: 'ok', url: 'https://www.w3schools.com/html/mov_bbb.mp4' },
]);

// Component State
const view = ref('grid');
const sortOpen = ref(false);
const deleteModalOpen = ref(false);
const moveModalOpen = ref(false);
const copyModalOpen = ref(false);
const showSuccessToast = ref(false);
const successMessage = ref('');
const showErrorToast = ref(false);
const errorMessage = ref('');
const selectedItems = ref([]);
const playingVideo = ref(null);

// Selection Logic
const isSelected = (itemId) => selectedItems.value.includes(itemId);

const toggleSelection = (itemId) => {
  const index = selectedItems.value.indexOf(itemId);
  if (index > -1) {
    selectedItems.value.splice(index, 1);
  } else {
    selectedItems.value.push(itemId);
  }
};

const allItems = computed(() => [...folders.value, ...videos.value].map(item => item.id));
const allSelected = computed(() => allItems.value.length > 0 && selectedItems.value.length === allItems.value.length);

const toggleSelectAll = () => {
    if(allSelected.value) {
        selectedItems.value = [];
    } else {
        selectedItems.value = [...allItems.value];
    }
}


// Toast Logic
const triggerSuccess = (message) => {
  successMessage.value = message;
  showSuccessToast.value = true;
  setTimeout(() => showSuccessToast.value = false, 3000);
};

const triggerError = (message) => {
  errorMessage.value = message;
  showErrorToast.value = true;
  setTimeout(() => showErrorToast.value = false, 3000);
};

// Modal Logic
const closeMoveCopyModal = () => {
  moveModalOpen.value = false;
  copyModalOpen.value = false;
};

const handleDelete = () => {
  console.log('Deleting items:', selectedItems.value);
  deleteModalOpen.value = false;
  triggerSuccess(`${selectedItems.value.length} items deleted successfully.`);
  selectedItems.value = [];
};

const handleMoveCopy = (destination) => {
  const mode = moveModalOpen.value ? 'Moved' : 'Copied';
  console.log(`${mode} items:`, selectedItems.value, 'to', destination);
  closeMoveCopyModal();
  triggerSuccess(`${selectedItems.value.length} items ${mode.toLowerCase()} successfully.`);
  selectedItems.value = [];
};

// Video Player Logic
const playVideo = (video) => {
    if(video.status !== 'missing') {
        playingVideo.value = video;
    }
}

const closeVideoPlayer = () => {
    const videoElement = document.querySelector('#video-popup video');
    if (videoElement) {
        videoElement.pause();
        videoElement.currentTime = 0;
    }
    playingVideo.value = null;
}

// Placeholder for Modal Components (to be created)
const DeleteModal = {
  props: ['open', 'itemCount'],
  emits: ['close', 'delete'],
  template: `
    <div v-if="open" @keydown.escape.window="$emit('close')" class="fixed inset-0 z-50 overflow-y-auto" role="dialog">
      <div class="flex items-end justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
        <div @click="$emit('close')" class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"></div>
        <span class="hidden sm:inline-block sm:align-middle sm:h-screen">&#8203;</span>
        <div class="inline-block align-bottom bg-card-light dark:bg-card-dark rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div class="bg-card-light dark:bg-card-dark px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div class="sm:flex sm:items-start">
              <div class="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-red-100 dark:bg-red-900/30 sm:mx-0 sm:h-10 sm:w-10">
                <span class="material-icons text-red-600 dark:text-red-400">warning</span>
              </div>
              <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                <h3 class="text-lg leading-6 font-medium text-text-light dark:text-text-dark">Delete Items</h3>
                <div class="mt-2">
                  <p class="text-sm text-subtext-light dark:text-subtext-dark">
                    Are you sure you want to delete the {{ itemCount }} selected items? This action cannot be undone.
                  </p>
                </div>
              </div>
            </div>
          </div>
          <div class="bg-background-light dark:bg-background-dark px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button @click="$emit('delete')" type="button" class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-red-600 text-base font-medium text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 sm:ml-3 sm:w-auto sm:text-sm">Delete</button>
            <button @click="$emit('close')" type="button" class="mt-3 w-full inline-flex justify-center rounded-md border border-border-light dark:border-border-dark shadow-sm px-4 py-2 bg-card-light dark:bg-card-dark text-base font-medium text-text-light dark:text-text-dark hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary sm:mt-0 sm:w-auto sm:text-sm">Cancel</button>
          </div>
        </div>
      </div>
    </div>
  `
};

const MoveCopyModal = {
  props: ['open', 'mode', 'itemCount'],
  emits: ['close', 'confirm'],
  template: `
    <div v-if="open" @keydown.escape.window="$emit('close')" class="fixed inset-0 z-50 overflow-y-auto" role="dialog">
      <div class="flex items-end justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
        <div @click="$emit('close')" class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"></div>
        <span class="hidden sm:inline-block sm:align-middle sm:h-screen">&#8203;</span>
        <div class="inline-block align-bottom bg-card-light dark:bg-card-dark rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div class="bg-card-light dark:bg-card-dark px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div class="sm:flex sm:items-start">
              <div class="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-blue-100 dark:bg-blue-900/30 sm:mx-0 sm:h-10 sm:w-10">
                <span class="material-icons text-primary dark:text-blue-400">{{ mode === 'move' ? 'drive_file_move' : 'content_copy' }}</span>
              </div>
              <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left w-full">
                <h3 class="text-lg leading-6 font-medium text-text-light dark:text-text-dark">{{ mode === 'move' ? 'Move Items' : 'Copy Items' }}</h3>
                <div class="mt-4">
                  <p class="text-sm text-subtext-light dark:text-subtext-dark mb-2">Select a destination folder:</p>
                  <div class="border border-border-light dark:border-border-dark rounded-lg p-3 space-y-2 h-48 overflow-y-auto">
                    <a href="#" class="flex items-center p-2 rounded-md hover:bg-gray-100 dark:hover:bg-gray-700"><span class="material-icons text-subtext-light dark:text-subtext-dark mr-2">folder</span><span class="text-text-light dark:text-text-dark">Media Library</span></a>
                    <a href="#" class="flex items-center p-2 rounded-md bg-blue-100 dark:bg-blue-900/40"><span class="material-icons text-primary dark:text-blue-400 mr-2">folder</span><span class="text-text-light dark:text-text-dark font-semibold">Client Projects</span></a>
                    <a href="#" class="flex items-center p-2 rounded-md hover:bg-gray-100 dark:hover:bg-gray-700 ml-6"><span class="material-icons text-subtext-light dark:text-subtext-dark mr-2">folder</span><span class="text-text-light dark:text-text-dark">Brand Redesign</span></a>
                    <a href="#" class="flex items-center p-2 rounded-md hover:bg-gray-100 dark:hover:bg-gray-700"><span class="material-icons text-subtext-light dark:text-subtext-dark mr-2">folder</span><span class="text-text-light dark:text-text-dark">Tutorials</span></a>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="bg-background-light dark:bg-background-dark px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button @click="$emit('confirm', 'some-destination')" type="button" class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-primary text-base font-medium text-white hover:bg-blue-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary sm:ml-3 sm:w-auto sm:text-sm">{{ mode === 'move' ? 'Move' : 'Copy' }}</button>
            <button @click="$emit('close')" type="button" class="mt-3 w-full inline-flex justify-center rounded-md border border-border-light dark:border-border-dark shadow-sm px-4 py-2 bg-card-light dark:bg-card-dark text-base font-medium text-text-light dark:text-text-dark hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary sm:mt-0 sm:w-auto sm:text-sm">Cancel</button>
          </div>
        </div>
      </div>
    </div>
  `
};

const SuccessToast = {
    props: ['show', 'message'],
    template: `
        <div v-if="show" class="fixed top-5 right-5 z-50 bg-green-500 text-white py-3 px-6 rounded-lg shadow-lg flex items-center">
            <span class="material-icons mr-2">check_circle</span>
            <span>{{ message }}</span>
        </div>
    `
};

const ErrorToast = {
    props: ['show', 'message'],
    template: `
        <div v-if="show" class="fixed top-5 right-5 z-50 bg-red-500 text-white py-3 px-6 rounded-lg shadow-lg flex items-center">
            <span class="material-icons mr-2">error</span>
            <span>{{ message }}</span>
        </div>
    `
};
</script>

<style>
/* Add this to your global stylesheet (e.g., src/style.css) if you want to hide scrollbars nicely */
::-webkit-scrollbar {
    width: 8px;
    height: 8px;
}
::-webkit-scrollbar-track {
    background: transparent;
}
::-webkit-scrollbar-thumb {
    background-color: rgba(156, 163, 175, 0.5);
    border-radius: 10px;
    border: 2px solid transparent;
    background-clip: content-box;
}
::-webkit-scrollbar-thumb:hover {
    background-color: rgba(156, 163, 175, 0.7);
}

.form-checkbox:focus,
.form-checkbox:checked:focus {
    outline: none !important;
    box-shadow: none !important;
    border-color: #007AFF !important;
}
</style>