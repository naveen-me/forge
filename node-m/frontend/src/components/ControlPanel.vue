<template>
  <section class="control-panel">
    <div class="container-fluid">
      <div class="row">
        <div class="col">
          <h2 class="mb-4">Controls</h2>
          <div class="main-controls">
            <div class="d-grid gap-2">
              <button
                @click="addVideoFile"
                :disabled="!connected"
                class="btn btn-primary"
              >
                Add Video File and Play
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="row mt-4" v-if="!connected">
        <div class="col">
          <div class="alert alert-warning" role="alert">
            Please wait for Tarva Engine connection to be established before adding video files
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import { inject } from 'vue'

export default {
  name: 'ControlPanel',
  setup() {
    const connected = inject('connected')
    const messages = inject('messages')
    const addedFiles = inject('addedFiles')

    const addVideoFile = async () => {
      messages.value.unshift({
        type: 'info',
        text: 'Opening file dialog...',
        timestamp: new Date()
      })

      try {
        // Check if we're in Electron environment
        if (window.electronAPI && window.electronAPI.addVideoFile) {
          const result = await window.electronAPI.addVideoFile()
          if (result.success) {
            addedFiles.value.push(result)
            messages.value.unshift({
              type: 'info',
              text: `Added video file: ${result.sceneName}`,
              timestamp: new Date()
            })
          } else {
            console.log('Add video file was cancelled or failed.')
            messages.value.unshift({
              type: 'error',
              text: 'Failed to add video file',
              timestamp: new Date()
            })
          }
        } else {
          // Mock response for browser environment
          console.log('Electron API not available, simulating file addition in browser');
          const mockResult = {
            success: true,
            sceneName: 'Mock Video File',
            filePath: 'mock/video/file.mp4',
            timestamp: new Date()
          };
          addedFiles.value.push(mockResult);
          messages.value.unshift({
            type: 'info',
            text: `Added video file: ${mockResult.sceneName}`,
            timestamp: new Date()
          })
        }
      } catch (error) {
        messages.value.unshift({
          type: 'error',
          text: `Error adding video file: ${error.message}`,
          timestamp: new Date()
        })
      }
    }

    return {
      connected,
      addVideoFile
    }
  }
}
</script>

<style scoped>
.control-panel {
  margin-bottom: 30px;
}

.main-controls {
  margin-top: 15px;
}

/* Dark theme support */
@media (prefers-color-scheme: dark) {
  .alert-warning {
    background-color: #333;
    border-color: #555;
    color: #fff;
  }
}
</style>