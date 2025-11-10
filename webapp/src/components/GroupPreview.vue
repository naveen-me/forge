<template>
  <div class="p-6">
    <h2 class="text-lg font-bold text-gray-900 mb-4">Group Preview: {{ group.name }}</h2>
    <div class="relative w-full h-96 bg-gray-200 border rounded-lg overflow-hidden">
      <!-- Children will be rendered here with absolute positioning -->
      <div v-for="child in children" :key="child.id" :style="getOverlayStyle(child)">
        <img v-if="child.type === 'image'" :src="child.src" class="w-full h-full" />
        <video v-if="child.type === 'video'" :src="child.src" class="w-full h-full" autoplay loop muted></video>
        <div v-if="child.type === 'text'" class="p-2">{{ child.text }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';

const props = defineProps({
  group: {
    type: Object,
    required: true,
  },
  children: {
    type: Array,
    required: true,
  },
});

const getOverlayStyle = (overlay) => {
  // This assumes overlays have position and size properties.
  // e.g., x, y, width, height, z-index (order)
  return {
    position: 'absolute',
    left: `${overlay.x || 0}px`,
    top: `${overlay.y || 0}px`,
    width: `${overlay.width || 100}px`,
    height: `${overlay.height || 50}px`,
    zIndex: overlay.order || 0,
    border: '1px dashed #999', // for visualization
  };
};
</script>
