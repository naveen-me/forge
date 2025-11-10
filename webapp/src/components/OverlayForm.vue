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
    
    <!-- Konva Canvas Preview -->
    <div class="relative w-full aspect-16/9 bg-gray-200 rounded-xl overflow-hidden mb-6" ref="canvasContainer">
      <v-stage :config="stageConfig" @mousedown="handleStageMouseDown" @touchstart="handleStageMouseDown">
        <v-layer>
          <template v-for="overlay in overlaysInPreview" :key="overlay.id">
            <!-- Image Overlays -->
            <v-image
              v-if="overlay.type === 'image' && imageElements[overlay.id]"
              :config="{ ...getKonvaConfig(overlay), image: imageElements[overlay.id] }"
              @dragend="handleDragEnd"
              @transformend="handleTransformEnd"
              @click="handleShapeClick(overlay.id)"
            />
            <!-- Text Overlays (as SVG Image) -->
            <v-image
              v-if="overlay.type === 'text' && textAsImage[overlay.id]"
              :config="{ ...getKonvaConfig(overlay), image: textAsImage[overlay.id] }"
              @dragend="handleDragEnd"
              @transformend="handleTransformEnd"
              @click="handleShapeClick(overlay.id)"
            />
          </template>
          <v-transformer ref="transformer" />
        </v-layer>
      </v-stage>
    </div>

    <div class="flex-1 flex flex-col">
      <form class="space-y-4 flex-1 overflow-y-auto pr-2">
        <!-- Form content remains largely the same -->
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
import { ref, watch, computed, onMounted, onUnmounted, nextTick } from 'vue';
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
const nameInput = ref(null);
const isBrowsingFiles = ref(false);

// Konva state
const canvasContainer = ref(null);
const stageConfig = ref({ width: 0, height: 0, scaleX: 1, scaleY: 1 });
const transformer = ref(null);
const selectedShapeId = ref(null);
const imageElements = ref({});
const textAsImage = ref({});

const editor = useEditor({
  extensions: [ StarterKit, Underline, FontFamily, TextStyle, Color, Highlight.configure({ multicolor: true }), FontSize ],
  content: '',
  onUpdate: ({ editor }) => {
    if (formData.value.type === 'text') {
      formData.value.source = editor.getHTML();
    }
  },
});

const overlaysInPreview = computed(() => {
  if (!props.selectedOverlay) return [];
  if (props.selectedOverlay.type === 'group') {
    return props.allOverlays.filter(o => o.parentId === props.selectedOverlay.id);
  }
  return [props.selectedOverlay];
});

const updateStageSize = () => {
  if (canvasContainer.value) {
    const { width, height } = canvasContainer.value.getBoundingClientRect();
    stageConfig.value = {
      ...stageConfig.value,
      width,
      height,
      scaleX: width / OBS_CANVAS_WIDTH,
      scaleY: height / OBS_CANVAS_HEIGHT,
    };
  }
};

const getKonvaConfig = (overlay) => ({
  id: overlay.id,
  x: overlay.x,
  y: overlay.y,
  width: overlay.width,
  height: overlay.height,
  draggable: selectedShapeId.value === overlay.id,
  opacity: overlay.opacity,
});

const handleDragEnd = (e) => {
  const node = e.target;
  formData.value.x = Math.round(node.x());
  formData.value.y = Math.round(node.y());
};

const handleTransformEnd = (e) => {
  const node = e.target;
  formData.value.x = Math.round(node.x());
  formData.value.y = Math.round(node.y());
  formData.value.width = Math.round(node.width() * node.scaleX());
  formData.value.height = Math.round(node.height() * node.scaleY());
  node.scaleX(1);
  node.scaleY(1);
};

const handleShapeClick = (id) => {
  selectedShapeId.value = id;
  updateTransformer();
};

const handleStageMouseDown = (e) => {
  if (e.target === e.target.getStage()) {
    selectedShapeId.value = null;
    updateTransformer();
    return;
  }
  const clickedOnTransformer = e.target.getParent().className === 'Transformer';
  if (clickedOnTransformer) {
    return;
  }
  const id = e.target.id();
  if (id) {
    handleShapeClick(id);
  }
};

const updateTransformer = () => {
  const transformerNode = transformer.value.getNode();
  const stage = transformerNode.getStage();
  const selectedNode = stage.findOne('#' + selectedShapeId.value);

  if (selectedNode) {
    transformerNode.nodes([selectedNode]);
  } else {
    transformerNode.nodes([]);
  }
  transformerNode.getLayer().batchDraw();
};

const preloadImage = (overlay) => {
  if (overlay.type === 'image' && overlay.source && !imageElements.value[overlay.id]) {
    const img = new window.Image();
    img.src = overlay.source;
    img.onload = () => {
      imageElements.value[overlay.id] = img;
    };
  }
};

const renderTextAsImage = (overlay) => {
  if (overlay.type !== 'text' || !overlay.source) return;

  const foreignObject = `
    <foreignObject width="${overlay.width}" height="${overlay.height}">
      <div xmlns="http://www.w3.org/1999/xhtml" style="font-family: ${overlay.fontFamily}; font-size: ${overlay.fontSize}px; line-height: ${overlay.lineHeight}; color: black;">
        ${overlay.source}
      </div>
    </foreignObject>
  `;
  
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${overlay.width}" height="${overlay.height}">${foreignObject}</svg>`;
  const svgBlob = new Blob([svg], { type: 'image/svg+xml;charset=utf-8' });
  const url = URL.createObjectURL(svgBlob);

  const img = new window.Image();
  img.src = url;
  img.onload = () => {
    textAsImage.value[overlay.id] = img;
    URL.revokeObjectURL(url);
  };
};

watch(() => props.selectedOverlay, (newOverlay) => {
  if (newOverlay) {
    formData.value = JSON.parse(JSON.stringify(newOverlay));
    
    if (editor.value && editor.value.getHTML() !== (formData.value.source || '')) {
        editor.value.commands.setContent(formData.value.source || '', false);
    }

    // Clean up source if it's an image and contains HTML
    if (formData.value.type === 'image' && formData.value.source && formData.value.source.includes('<')) {
      const tempDiv = document.createElement('div');
      tempDiv.innerHTML = formData.value.source;
      formData.value.source = tempDiv.textContent || '';
    }

    if (formData.value.type === 'text') {
      formData.value.fontFamily = formData.value.fontFamily || 'Roboto';
      formData.value.fontSize = formData.value.fontSize || 24;
      formData.value.lineHeight = formData.value.lineHeight || 1.2;
      formData.value.wordWrap = formData.value.wordWrap === undefined ? true : formData.value.wordWrap;
      renderTextAsImage(formData.value);
    } else if (formData.value.type === 'image') {
      // Use nextTick to ensure reactivity when image loads
      nextTick(() => {
        preloadImage(formData.value);
      });
    }
    
    nextTick(() => {
      handleShapeClick(newOverlay.id);
    });

  } else {
    formData.value = {};
    if (editor.value) {
        editor.value.commands.clearContent();
    }
  }
}, { immediate: true, deep: true });

watch(formData, (newFormData) => {
  if (newFormData.type === 'text') {
    renderTextAsImage(newFormData);
  }
}, { deep: true });

onMounted(() => {
  updateStageSize();
  window.addEventListener('resize', updateStageSize);
});

onUnmounted(() => {
  window.removeEventListener('resize', updateStageSize);
  if (editor.value) {
    editor.value.destroy();
  }
});

// Other existing methods (enableNameEditing, handleNameBlur, openFileBrowser, scrollFilterEnabled) remain the same
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
</script>
