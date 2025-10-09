<template>
  <PageLayout>
    <h1>Live</h1>
    <div class="container-fluid">
      <div class="row">
        <div class="col-12">
          <div class="card">
            <div class="card-header">
              <h5 class="card-title mb-0">Live Preview</h5>
            </div>
            <div class="card-body">
              <div class="preview-container">
                <div v-if="previewData && previewData.imageData" class="preview-image-container">
                  <img :src="`data:image/png;base64,${previewData.imageData}`" alt="Live Preview" class="preview-image" />
                </div>
                <div v-else class="preview-placeholder">
                  <p>No preview available. Connect to Tarva Engine to see live preview.</p>
                </div>
                <div class="preview-controls mt-3">
                  <button @click="startPreview" class="btn btn-primary me-2" :disabled="!connected || previewActive">
                    Start Preview
                  </button>
                  <button @click="stopPreview" class="btn btn-secondary" :disabled="!previewActive">
                    Stop Preview
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </PageLayout>
</template>

<script>
import { ref, inject, onMounted, onUnmounted } from 'vue'
import PageLayout from './PageLayout.vue'

export default {
  name: 'Live',
  components: {
    PageLayout
  },
  setup() {
    const connected = inject('connected')
    const previewData = ref(null)
    const previewActive = ref(false)
    const previewInterval = ref(null)

    // Function to start preview updates
    const startPreview = async () => {
      if (window.electronAPI) {
        try {
          await window.electronAPI.startPreviewUpdates()
          previewActive.value = true
        } catch (error) {
          console.error('Failed to start preview:', error)
        }
      }
    }

    // Function to stop preview updates
    const stopPreview = async () => {
      if (window.electronAPI) {
        try {
          await window.electronAPI.stopPreviewUpdates()
          previewActive.value = false
          previewData.value = null
        } catch (error) {
          console.error('Failed to stop preview:', error)
        }
      }
    }

    // Handle preview data from Tarva Engine
    const handleTarvaEnginePreview = (data) => {
      // The data object contains imageData field with the base64 image
      console.log('Received preview data:', data);
      console.log('Has imageData:', !!data && !!data.imageData);
      previewData.value = data;
    }

    // Initialize event listeners
    onMounted(() => {
      console.log('Live component mounted, setting up preview listener');
      if (window.electronAPI) {
        window.electronAPI.receive('tarva-engine-preview', handleTarvaEnginePreview);
      } else {
        console.log('electronAPI not available');
      }
    })

    // Clean up event listeners and stop preview updates
    onUnmounted(() => {
      if (previewActive.value) {
        stopPreview()
      }

      if (window.electronAPI) {
        window.electronAPI.removeAllListeners('tarva-engine-preview')
      }
    })

    return {
      connected,
      previewData,
      previewActive,
      startPreview,
      stopPreview
    }
  }
}
</script>

<style scoped>
.preview-container {
  text-align: center;
  min-height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.preview-image-container {
  width: 100%;
  max-width: 640px;
  margin: 0 auto;
}

.preview-image {
  width: 100%;
  height: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.preview-placeholder {
  padding: 2rem;
  background-color: #f8f9fa;
  border: 1px dashed #dee2e6;
  border-radius: 4px;
  color: #6c757d;
  width: 100%;
  max-width: 640px;
  margin: 0 auto;
}

.preview-controls {
  margin-top: 1rem;
}
</style>
