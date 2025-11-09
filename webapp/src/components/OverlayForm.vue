<template>
  <div v-if="selectedOverlay" class="flex-1 flex flex-col">
    <div class="flex items-center justify-between mb-4">
      <div class="group relative flex-1">
        <h2 class="text-2xl font-bold text-gray-900 cursor-pointer" @click="editingName = true">{{ formData.name }}</h2>
        <input v-if="editingName" v-model="formData.name" @blur="editingName = false; $emit('update', formData)" @keyup.enter="editingName = false; $emit('update', formData)" class="text-2xl font-bold text-gray-900 bg-transparent border-b-2 border-primary outline-none w-full" type="text"/>
      </div>
    </div>
    <div ref="preview" class="flex-1 relative w-full aspect-16/9 bg-gray-200 rounded-xl overflow-hidden mb-6">
      <div ref="interactive" class="absolute bg-cover bg-center" :style="interactiveStyle"></div>
    </div>
    <div class="flex-1 flex flex-col">
      <form class="space-y-4 flex-1 overflow-y-auto pr-2">
        <div v-if="formData.type === 'image' || formData.type === 'video'">
          <label class="block text-sm font-medium text-gray-700">Source</label>
          <div class="mt-1 flex items-center">
            <input v-model="formData.source" class="block w-full rounded-lg bg-gray-100 border-gray-200 shadow-sm focus:border-primary focus:ring-primary sm:text-sm text-gray-900" type="text"/>
            <button @click="showMediaLibrary = true" class="ml-2 flex h-10 items-center justify-center rounded-lg bg-gray-200 px-4 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-300">Browse</button>
          </div>
        </div>
        <div v-if="formData.type === 'text'">
          <label class="block text-sm font-medium text-gray-700">Text Content</label>
          <div class="mt-1">
            <ckeditor :editor="editor" v-model="formData.source" :config="editorConfig"></ckeditor>
          </div>
        </div>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700" for="x">X</label>
            <input class="mt-1 block w-full rounded-lg bg-gray-100 border-gray-200 shadow-sm focus:border-primary focus:ring-primary sm:text-sm text-gray-900" id="x" type="number" v-model="formData.x" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700" for="y">Y</label>
            <input class="mt-1 block w-full rounded-lg bg-gray-100 border-gray-200 shadow-sm focus:border-primary focus:ring-primary sm:text-sm text-gray-900" id="y" type="number" v-model="formData.y" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700" for="width">Width</label>
            <input class="mt-1 block w-full rounded-lg bg-gray-100 border-gray-200 shadow-sm focus:border-primary focus:ring-primary sm:text-sm text-gray-900" id="width" type="number" v-model="formData.width" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700" for="height">Height</label>
            <input class="mt-1 block w-full rounded-lg bg-gray-100 border-gray-200 shadow-sm focus:border-primary focus:ring-primary sm:text-sm text-gray-900" id="height" type="number" v-model="formData.height" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700" for="opacity">Opacity</label>
            <input class="mt-1 block w-full rounded-lg bg-gray-100 border-gray-200 shadow-sm focus:border-primary focus:ring-primary sm:text-sm text-gray-900" id="opacity" max="1" min="0" step="0.1" type="number" v-model="formData.opacity" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700" for="fit">Fit</label>
            <select v-model="formData.fit" class="mt-1 block w-full rounded-lg bg-gray-100 border-gray-200 shadow-sm focus:border-primary focus:ring-primary sm:text-sm text-gray-900">
              <option>fit</option>
              <option>cover</option>
              <option>fill</option>
            </select>
          </div>
        </div>
        <div>
          <h3 class="text-lg font-medium text-gray-900">OBS Filters</h3>
          <div class="space-y-2 mt-2">
            <div class="flex items-center gap-2">
              <input type="checkbox" v-model="scrollFilterEnabled" class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary" />
              <label class="text-sm font-medium text-gray-700">Scroll</label>
            </div>
            <div v-if="scrollFilterEnabled" class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700">Speed X</label>
                <input v-model="formData.filters.scroll.speed_x" class="mt-1 block w-full rounded-lg bg-gray-100 border-gray-200 shadow-sm focus:border-primary focus:ring-primary sm:text-sm text-gray-900" type="number" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700">Speed Y</label>
                <input v-model="formData.filters.scroll.speed_y" class="mt-1 block w-full rounded-lg bg-gray-100 border-gray-200 shadow-sm focus:border-primary focus:ring-primary sm:text-sm text-gray-900" type="number" />
              </div>
            </div>
          </div>
        </div>
      </form>
      <div class="flex justify-between items-center pt-6">
        <button @click="$emit('update', formData)" class="flex h-10 items-center justify-center rounded-lg bg-primary px-6 text-sm font-medium text-white transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2">Save</button>
        <button @click="$emit('delete', selectedOverlay.id)" class="h-10 w-10 flex items-center justify-center rounded-lg text-red-500 hover:bg-red-100 hover:text-red-700 transition-colors">
          <span class="material-symbols-outlined text-2xl">delete</span>
        </button>
      </div>
    </div>
  </div>
  <div v-else class="flex items-center justify-center h-full">
    <p class="text-gray-500">Select an overlay to edit its properties.</p>
  </div>
  <div v-if="showMediaLibrary" class="fixed inset-0 bg-gray-900 bg-opacity-50 flex items-center justify-center">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-4xl h-full max-h-[80vh] p-6">
      <MediaLibrary @file-selected="handleFileSelect" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted, onUnmounted } from 'vue';
import interact from 'interact.js';
import CKEditor from '@ckeditor/ckeditor5-vue';
import ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import MediaLibrary from './MediaLibrary.vue';

const OBS_CANVAS_WIDTH = 1920;
const OBS_CANVAS_HEIGHT = 1080;

const props = defineProps({
  selectedOverlay: Object,
});

const emit = defineEmits(['update', 'delete']);

const formData = ref({});
const editingName = ref(false);
const preview = ref(null);
const interactive = ref(null);
const showMediaLibrary = ref(false);

const editor = ClassicEditor;
const editorConfig = {
  toolbar: [ 'bold', 'italic', 'underline', '|', 'fontColor', 'fontBackgroundColor' ]
};

const interactiveStyle = computed(() => {
  if (!formData.value || !preview.value) return {};
  const previewRect = preview.value.getBoundingClientRect();
  const scaleX = previewRect.width / OBS_CANVAS_WIDTH;
  const scaleY = previewRect.height / OBS_CANVAS_HEIGHT;

  if (formData.value.type === 'text') {
    return {
      width: `${formData.value.width * scaleX}px`,
      height: `${formData.value.height * scaleY}px`,
      transform: `translate(${formData.value.x * scaleX}px, ${formData.value.y * scaleY}px)`,
      color: 'white',
    }
  }

  return {
    width: `${formData.value.width * scaleX}px`,
    height: `${formData.value.height * scaleY}px`,
    transform: `translate(${formData.value.x * scaleX}px, ${formData.value.y * scaleY}px)`,
    backgroundImage: `url(${formData.value.source})`,
  };
});

const scrollFilterEnabled = computed({
  get: () => !!formData.value.filters?.scroll,
  set: (enabled) => {
    if (enabled) {
      if (!formData.value.filters) {
        formData.value.filters = {};
      }
      formData.value.filters.scroll = { speed_x: 0, speed_y: 0 };
    } else {
      delete formData.value.filters.scroll;
    }
  },
});

watch(() => props.selectedOverlay, (newOverlay) => {
  if (newOverlay) {
    formData.value = JSON.parse(JSON.stringify(newOverlay));
  } else {
    formData.value = {};
  }
}, { immediate: true, deep: true });

const handleFileSelect = (file) => {
  formData.value.source = file.filePath;
  showMediaLibrary.value = false;
};

onMounted(() => {
  if (interactive.value) {
    interact(interactive.value)
      .draggable({
        onmove: (event) => {
          const previewRect = preview.value.getBoundingClientRect();
          const scaleX = previewRect.width / OBS_CANVAS_WIDTH;
          const scaleY = previewRect.height / OBS_CANVAS_HEIGHT;
          formData.value.x += event.dx / scaleX;
          formData.value.y += event.dy / scaleY;
        },
      })
      .resizable({
        edges: { left: true, right: true, bottom: true, top: true },
        onmove: (event) => {
          const previewRect = preview.value.getBoundingClientRect();
          const scaleX = previewRect.width / OBS_CANVAS_WIDTH;
          const scaleY = previewRect.height / OBS_CANVAS_HEIGHT;
          formData.value.width = event.rect.width / scaleX;
          formData.value.height = event.rect.height / scaleY;
          formData.value.x += event.deltaRect.left / scaleX;
          formData.value.y += event.deltaRect.top / scaleY;
        },
      });
  }
});

onUnmounted(() => {
  if (interactive.value) {
    interact(interactive.value).unset();
  }
});
</script>
