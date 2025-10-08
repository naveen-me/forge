<template>
  <div class="preview-container" :style="containerStyle">
    <video class="preview-video" :src="previewVideoUrl" autoplay loop muted playsinline key="preview-video"></video>

    <div v-for="overlay in overlays" :key="overlay.id" class="overlay-element" :style="getOverlayStyle(overlay)">
      <img v-if="overlay.type === 'image' && overlay.content" :src="`media://${overlay.content}`" :style="getMediaStyle(overlay)" />
      <video v-if="overlay.type === 'video' && overlay.content" :src="`media://${overlay.content}`" autoplay loop muted :style="getMediaStyle(overlay)"></video>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, defineProps } from 'vue';
import apiClient from '../../api.js';

const props = defineProps({
  overlays: {
    type: Array,
    required: true,
    default: () => []
  }
});

const canvasWidth = ref(1920);
const canvasHeight = ref(1080);
const containerWidth = ref(450);
const previewVideoUrl = 'https://file-examples.com/wp-content/storage/2017/04/file_example_MP4_480_1_5MG.mp4';

onMounted(async () => {
  if (window.electronAPI) {
    const response = await apiClient.getSystemDefaults();
    if (response.success) {
      canvasWidth.value = parseInt(response.data.canvasWidth, 10) || 1920;
      canvasHeight.value = parseInt(response.data.canvasHeight, 10) || 1080;
    }
  }
});

const aspectRatio = computed(() => canvasHeight.value / canvasWidth.value);
const scale = computed(() => containerWidth.value / canvasWidth.value);

const containerStyle = computed(() => ({
  width: `${containerWidth.value}px`,
  height: `${containerWidth.value * aspectRatio.value}px`,
  position: 'relative',
  backgroundColor: '#000',
  overflow: 'hidden',
  border: '1px solid #ccc'
}));

const getOverlayStyle = (overlay) => {
  if (!overlay) return {};
  return {
    position: 'absolute',
    left: `${overlay.x * scale.value}px`,
    top: `${overlay.y * scale.value}px`,
    width: `${overlay.width * scale.value}px`,
    height: `${overlay.height * scale.value}px`,
    border: '1px dashed #fff',
    boxSizing: 'border-box',
  };
};

const getMediaStyle = (overlay) => {
  if (!overlay) return {};
  const fit = overlay.fit;
  return {
    width: '100%',
    height: '100%',
    objectFit: fit === 'fill' ? 'fill' : (fit === 'cover' ? 'cover' : 'contain'),
  };
};
</script>

<style scoped>
.preview-video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.overlay-element {
  position: absolute; /* Changed from relative */
}
</style>
