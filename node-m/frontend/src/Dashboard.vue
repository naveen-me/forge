<template>
  <div class="min-vh-100 d-flex">
    <!-- Menu Bar -->
    <MenuBar />

    <!-- Dashboard Content -->
    <div class="flex-grow-1 d-flex flex-column" style="margin-left: 60px;
    border-left: 1px solid #dfdfdf;
    border-top: 1px solid #dfdfdf;
    border-top-left-radius: 10px;
    margin-top: 10px;
    background-color: #fff;
    box-shadow: 0px 0px 1px rgba(0, 0, 0, 0.1);">
      <router-view />
    </div>
  </div>
</template>

<script>
import { provide, ref, reactive } from 'vue'
import MenuBar from './components/MenuBar.vue';

export default {
  name: 'Dashboard',
  components: {
    MenuBar
  },
  setup() {
    // Provide global state to child components
    const connected = ref(false)
    const previewAvailable = ref(false)
    const messages = ref([])
    const addedFiles = ref([])
    const openModal = (title, content) => {
      // Placeholder for modal functionality
      console.log(`Modal: ${title}`, content)
    }
    
    // Provide these values to child components
    provide('connected', connected)
    provide('previewAvailable', previewAvailable)
    provide('messages', messages)
    provide('addedFiles', addedFiles)
    provide('openModal', openModal)

    return {
      connected,
      previewAvailable,
      messages,
      addedFiles,
      openModal
    }
  }
}
</script>