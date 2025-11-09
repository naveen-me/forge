<template>
  <div v-if="selectedOverlay" class="flex-1 flex flex-col">
    <div class="flex items-center justify-between mb-4">
      <div class="group relative flex-1">
        <div class="flex items-center gap-2">
          <div class="flex-1">
            <input 
              v-if="editingName"
              v-model="formData.name" 
              @blur="handleNameBlur" 
              @keyup.enter="handleNameBlur"
              ref="nameInput"
              class="text-2xl font-bold text-gray-900 bg-transparent border-b-2 border-primary pb-1 w-full outline-none" 
              type="text"
              autofocus />
            <div 
              v-else
              @click="enableNameEditing"
              class="text-2xl font-bold text-gray-900 border-b border-gray-300 pb-1 cursor-pointer w-full">
              {{ formData.name }}
            </div>
          </div>
          <button 
            @click="enableNameEditing" 
            class="opacity-0 group-hover:opacity-100 transition-opacity text-gray-400 hover:text-gray-600 focus:outline-none"
            title="Edit name">
            <span class="material-symbols-outlined text-base">edit</span>
          </button>
        </div>
      </div>
    </div>
    <div ref="preview" class="flex-1 relative w-full aspect-16/9 bg-gray-200 rounded-xl overflow-hidden mb-6">
      <div v-for="overlay in overlaysInPreview" :key="overlay.id" class="absolute bg-cover bg-center" :style="getInteractiveStyle(overlay)" v-html="overlay.type === 'text' ? overlay.source : ''"></div>
    </div>
    <div class="flex-1 flex flex-col">
      <form class="space-y-4 flex-1 overflow-y-auto pr-2">
        <div v-if="formData.type === 'image' || formData.type === 'video'">
          <label class="block text-sm font-medium text-gray-700">Source</label>
          <div class="mt-1 flex items-center">
            <input v-model="formData.source" class="block w-full rounded-lg bg-gray-100 border-gray-200 shadow-sm focus:border-primary focus:ring-primary sm:text-sm text-gray-900" type="text"/>
            <button @click.prevent="openFileBrowser" :disabled="isBrowsingFiles" class="ml-2 flex h-10 items-center justify-center rounded-lg bg-primary px-4 text-sm font-medium text-white transition-colors hover:bg-primary/90" :class="{'opacity-50 cursor-not-allowed': isBrowsingFiles}" title="Browse files directly">
              {{ isBrowsingFiles ? 'Browsing...' : 'Browse' }}
            </button>
          </div>
        </div>
        <div v-if="formData.type === 'text'">
          <label class="block text-sm font-medium text-gray-700">Text Content</label>
          <div class="mt-1">
            <div v-if="isTextEditorFocused" class="border rounded p-2 bg-white">
              <div ref="textEditor" contenteditable="true" 
                   class="min-h-[120px] p-3 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                   @input="updateTextContent"
                   @focus="isTextEditorFocused = true"
                   @blur="isTextEditorFocused = false"
                   :class="{'prose max-w-none': true}"
                   v-html="editorContent">
              </div>
              <div class="flex flex-wrap items-center gap-1 mt-2 p-2 border rounded bg-gray-50">
                <button type="button" @click="applyFormatting('bold')" class="px-2 py-1 text-sm border rounded hover:bg-gray-100" title="Bold">
                  <span class="font-bold">B</span>
                </button>
                <button type="button" @click="applyFormatting('italic')" class="px-2 py-1 text-sm border rounded hover:bg-gray-100" title="Italic">
                  <span class="italic">I</span>
                </button>
                <button type="button" @click="applyFormatting('underline')" class="px-2 py-1 text-sm border rounded hover:bg-gray-100" title="Underline">
                  <span class="underline">U</span>
                </button>
                <div class="flex items-center border-r pr-2 border-gray-300">
                  <label class="text-xs mr-1">Color:</label>
                  <input type="color" @input="applyColor('foreColor', $event.target.value)" class="w-6 h-6 p-0.5 border rounded">
                </div>
                <div class="flex items-center">
                  <label class="text-xs mr-1">BG:</label>
                  <input type="color" @input="applyColor('backColor', $event.target.value)" class="w-6 h-6 p-0.5 border rounded">
                </div>
                <button type="button" @click="insertLink" class="ml-2 px-2 py-1 text-sm border rounded hover:bg-gray-100" title="Insert Link">
                  <span class="material-symbols-outlined text-base">link</span>
                </button>
              </div>
            </div>
            <div v-else @dblclick="isTextEditorFocused = true" class="min-h-[120px] p-3 border-2 border-dashed rounded bg-gray-50 cursor-pointer hover:border-blue-300"
                 v-html="formData.source || '<div class=\'text-gray-400 italic\'>Double-click to edit text...</div>'">
            </div>
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
</template>

<script setup>
import { ref, watch, computed, onMounted, onUnmounted } from 'vue';
import interact from 'interact.js';
import MediaLibrary from './MediaLibrary.vue';

const OBS_CANVAS_WIDTH = 1920;
const OBS_CANVAS_HEIGHT = 1080;

const props = defineProps({
  selectedOverlay: Object,
  allOverlays: Array,
});

const emit = defineEmits(['update', 'delete']);

const formData = ref({});
const editingName = ref(false);
const preview = ref(null);
const interactive = ref(null);
const nameInput = ref(null);
const textEditor = ref(null);
const isTextEditorFocused = ref(false);
const editorContent = ref('');
const isBrowsingFiles = ref(false);

// Methods for text editor formatting
const updateTextContent = () => {
  if (textEditor.value) {
    formData.value.source = textEditor.value.innerHTML;
    editorContent.value = textEditor.value.innerHTML;
  }
};

const applyFormatting = (command) => {
  document.execCommand(command, false, null);
  setTimeout(updateTextContent, 0); // Use timeout to ensure DOM updates
};

const applyColor = (command, value) => {
  document.execCommand(command, false, value);
  setTimeout(updateTextContent, 0);
};

const insertLink = () => {
  const url = prompt('Enter the URL:');
  if (url) {
    document.execCommand('createLink', false, url);
    setTimeout(updateTextContent, 0);
  }
};

const overlaysInPreview = computed(() => {
  if (!props.selectedOverlay) return [];
  if (props.selectedOverlay.type === 'group') {
    return props.allOverlays.filter(o => o.parentId === props.selectedOverlay.id);
  }
  return [props.selectedOverlay];
});

const getInteractiveStyle = (overlay) => {
  if (!overlay || !preview.value) return {};
  const previewRect = preview.value.getBoundingClientRect();
  const scaleX = previewRect.width / OBS_CANVAS_WIDTH;
  const scaleY = previewRect.height / OBS_CANVAS_HEIGHT;

  if (overlay.type === 'text') {
    return {
      width: `${overlay.width * scaleX}px`,
      height: `${overlay.height * scaleY}px`,
      transform: `translate(${overlay.x * scaleX}px, ${overlay.y * scaleY}px)`,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '4px',
      color: 'white',
      fontSize: '14px',
      overflow: 'hidden',
      textAlign: 'center',
      wordWrap: 'break-word',
      wordBreak: 'break-word',
    }
  }

  return {
    width: `${overlay.width * scaleX}px`,
    height: `${overlay.height * scaleY}px`,
    transform: `translate(${overlay.x * scaleX}px, ${overlay.y * scaleY}px)`,
    backgroundImage: `url(${overlay.source})`,
  };
};

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
    editorContent.value = formData.value.source || '';
  } else {
    formData.value = {};
    editorContent.value = '';
  }
}, { immediate: true, deep: true });

const openFileBrowser = async () => {
  isBrowsingFiles.value = true;
  try {
    // Import media service
    const { mediaService } = await import('../services/api.js');
    const response = await mediaService.selectFiles();
    
    if (response.data.files && response.data.files.length > 0) {
      // Use the first selected file
      formData.value.source = response.data.files[0];
      // Update the overlay with the new source
      emit('update', formData.value);
    }
  } catch (error) {
    console.error('Error selecting file:', error);
  } finally {
    isBrowsingFiles.value = false;
  }
};

const enableNameEditing = () => {
  editingName.value = true;
  // Focus the input field after enabling editing
  setTimeout(() => {
    if (nameInput.value) {
      nameInput.value.focus();
      nameInput.value.select(); // Select all text for easier editing
    }
  }, 0);
};

const handleNameBlur = () => {
  editingName.value = false;
  if (formData.value.name.trim()) {
    emit('update', formData.value);
  }
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
