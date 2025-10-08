<template>
  <main class="app-main">
    <div class="container-fluid">
      <div class="row">
        <div class="col-12">
          <ControlPanel />
        </div>
      </div>

      <div class="row mt-4">
        <div class="col-12 col-lg-6">
          <PreviewPanel />
        </div>
        <div class="col-12 col-lg-6">
          <FileList />
        </div>
      </div>

      <div class="row mt-4">
        <div class="col-12">
          <LogPanel />
        </div>
      </div>
    </div>
  </main>
</template>

<script>
import { provide, ref, reactive } from 'vue'
import ControlPanel from './ControlPanel.vue'
import PreviewPanel from './PreviewPanel.vue'
import FileList from './FileList.vue'
import LogPanel from './LogPanel.vue'

export default {
  name: 'MainContent',
  components: {
    ControlPanel,
    PreviewPanel,
    FileList,
    LogPanel
  },
  setup() {
    // Provide reactive state to child components
    const connected = ref(false)
    const previewAvailable = ref(false)
    const messages = ref([])
    const addedFiles = ref([])
    
    // Provide these values to child components
    provide('connected', connected)
    provide('previewAvailable', previewAvailable)
    provide('messages', messages)
    provide('addedFiles', addedFiles)

    return {
      connected,
      previewAvailable,
      messages,
      addedFiles
    }
  }
}
</script>

<style scoped>
.app-main {
  padding: 20px;
}
</style>