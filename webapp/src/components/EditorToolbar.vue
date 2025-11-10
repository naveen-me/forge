<template>
  <div v-if="editor" class="flex flex-wrap items-center gap-2 p-2 border rounded-t-lg bg-gray-50">
    <!-- Font Family Dropdown -->
    <select @change="editor.chain().focus().setFontFamily($event.target.value).run()" class="text-sm border-gray-300 rounded-md shadow-sm focus:border-indigo-300 focus:ring focus:ring-indigo-200 focus:ring-opacity-50">
      <option value="">Default</option>
      <option v-for="font in fontFamilies" :key="font" :value="font">{{ font }}</option>
    </select>

    <!-- Font Size Input -->
    <input 
      type="number" 
      @change="editor.chain().focus().setFontSize($event.target.value + 'px').run()" 
      :value="getFontSize()"
      class="w-20 text-sm border-gray-300 rounded-md shadow-sm focus:border-indigo-300 focus:ring focus:ring-indigo-200 focus:ring-opacity-50"
      placeholder="Size"
    />

    <!-- Bold Button -->
    <button type="button" @click="editor.chain().focus().toggleBold().run()" :class="{ 'is-active': editor.isActive('bold') }" class="px-3 py-1 text-sm border rounded hover:bg-gray-200" title="Bold">
      <span class="font-bold">B</span>
    </button>

    <!-- Italic Button -->
    <button type="button" @click="editor.chain().focus().toggleItalic().run()" :class="{ 'is-active': editor.isActive('italic') }" class="px-3 py-1 text-sm border rounded hover:bg-gray-200" title="Italic">
      <span class="italic">I</span>
    </button>

    <!-- Underline Button -->
    <button type="button" @click="editor.chain().focus().toggleUnderline().run()" :class="{ 'is-active': editor.isActive('underline') }" class="px-3 py-1 text-sm border rounded hover:bg-gray-200" title="Underline">
      <span class="underline">U</span>
    </button>

    <!-- Text Color Picker -->
    <div class="flex items-center">
      <label class="text-xs mr-1">Color:</label>
      <input type="color" @input="editor.chain().focus().setColor($event.target.value).run()" :value="editor.getAttributes('textStyle').color" class="w-7 h-7 p-0.5 border rounded">
    </div>

    <!-- Background Color (Highlight) Picker -->
    <div class="flex items-center">
      <label class="text-xs mr-1">BG:</label>
      <input type="color" @input="editor.chain().focus().toggleHighlight({ color: $event.target.value }).run()" class="w-7 h-7 p-0.5 border rounded">
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';

const props = defineProps({
  editor: {
    type: Object,
    required: true,
  },
});

const fontFamilies = [
  'Montserrat', 'Poppins', 'Roboto', 'Open Sans', 'Inter', 'Lato', 
  'Bebas Neue', 'Rajdhani', 'Oswald', 'Anton', 
  'Noto Sans Telugu', 'NTR', 'Tiro Telugu'
];

const getFontSize = () => {
  const size = props.editor.getAttributes('textStyle').fontSize;
  return size ? parseInt(size) : '';
};
</script>

<style>
.is-active {
  background-color: #e0e0e0;
}
</style>
