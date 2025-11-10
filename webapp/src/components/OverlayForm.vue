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
            <EditorToolbar v-if="editor" :editor="editor" />
            <EditorContent :editor="editor" class="min-h-[120px] p-3 focus:outline-none border-x border-b border-gray-300 rounded-b-lg" />
          </div>

          <h4 class="text-md font-medium text-gray-800 mt-6 mb-2">Container Styling</h4>
          <div class="grid grid-cols-2 md:grid-cols-3 gap-4 border-t pt-4">
            <div>
              <label class="block text-sm font-medium text-gray-700" for="lineHeight">Line Height</label>
              <input class="mt-1 block w-full rounded-lg bg-gray-100 border-gray-200 shadow-sm focus:border-primary focus:ring-primary sm:text-sm text-gray-900" id="lineHeight" type="number" step="0.1" v-model="formData.lineHeight" />
            </div>
            <div class="flex items-center gap-2 pt-6">
              <input type="checkbox" v-model="formData.wordWrap" class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary" />
              <label class="text-sm font-medium text-gray-700">Word Wrap</label>
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
import { useEditor, EditorContent } from '@tiptap/vue-3';
import { StarterKit } from '@tiptap/starter-kit';
import { TextStyle } from '@tiptap/extension-text-style';
import { FontFamily } from '@tiptap/extension-font-family';
import { Underline } from '@tiptap/extension-underline';
import { Color } from '@tiptap/extension-color';
import { Highlight } from '@tiptap/extension-highlight';
import { FontSize } from '../tiptap-extensions/FontSize.js';
import EditorToolbar from './EditorToolbar.vue';

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
const isBrowsingFiles = ref(false);

const editor = useEditor({
  extensions: [
    StarterKit,
    Underline,
    FontFamily,
    TextStyle,
    Color,
    Highlight.configure({ multicolor: true }),
    FontSize,
  ],
  content: '',
  onUpdate: ({ editor }) => {
    formData.value.source = editor.getHTML();
  },
});

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
    const style = {
      width: `${overlay.width * scaleX}px`,
      height: `${overlay.height * scaleY}px`,
      transform: `translate(${overlay.x * scaleX}px, ${overlay.y * scaleY}px)`,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontFamily: overlay.fontFamily || 'Roboto',
      fontSize: `${(overlay.fontSize || 24) * scaleY}px`,
      lineHeight: overlay.lineHeight || 1.2,
      overflow: 'hidden',
      padding: '4px',
    };
    if (overlay.wordWrap) {
      style.wordWrap = 'break-word';
      style.wordBreak = 'break-word';
    }
    // The v-html directive will handle the rich text content
    return style;
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
    
    if (editor.value && editor.value.getHTML() !== (formData.value.source || '')) {
        editor.value.commands.setContent(formData.value.source || '', false);
    }

    if (formData.value.type === 'text') {
      formData.value.fontFamily = formData.value.fontFamily || 'Roboto';
      formData.value.fontSize = formData.value.fontSize || 24;
      formData.value.lineHeight = formData.value.lineHeight || 1.2;
      formData.value.wordWrap = formData.value.wordWrap === undefined ? true : formData.value.wordWrap;
    }
  } else {
    formData.value = {};
    if (editor.value) {
        editor.value.commands.clearContent();
    }
  }
}, { immediate: true, deep: true });

const openFileBrowser = async () => {
  isBrowsingFiles.value = true;
  try {
    const { mediaService } = await import('../services/api.js');
    const response = await mediaService.selectFiles();
    
    if (response.data.files && response.data.files.length > 0) {
      formData.value.source = response.data.files[0];
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
  setTimeout(() => {
    if (nameInput.value) {
      nameInput.value.focus();
      nameInput.value.select();
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
  if (editor.value) {
    editor.value.destroy();
  }
  if (interactive.value) {
    interact(interactive.value).unset();
  }
});
</script>
