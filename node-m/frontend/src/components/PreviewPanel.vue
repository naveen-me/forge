<template>
  <section class="preview-panel" v-if="connected">
    <div class="card shadow-sm">
      <div class="card-header">
        <h2 class="mb-0">Preview (Virtual Camera Feed)</h2>
      </div>
      <div class="card-body">
        <div class="preview-container">
          <div class="ratio ratio-16x9 bg-light">
            <video
              ref="previewVideo"
              controls
              autoplay
              v-show="previewAvailable"
              class="w-100 h-100"
            >
              Your browser does not support the video tag.
            </video>
            <div class="no-preview d-flex align-items-center justify-content-center" v-if="!previewAvailable">
              <div class="text-center p-5">
                <div class="spinner-border text-primary" role="status" v-if="!userInteractionRequired">
                  <span class="visually-hidden">Loading...</span>
                </div>
                <button
                  v-if="userInteractionRequired"
                  @click="initPreview"
                  class="btn btn-primary mb-3"
                >
                  Start Virtual Camera Preview
                </button>
                <p class="h5 mt-3" v-if="!userInteractionRequired">Virtual camera feed loading or unavailable...</p>
                <p class="h5 mt-3" v-else>Click button to start virtual camera preview</p>
                <p class="text-muted mt-2">Please ensure Tarva Engine virtual camera is started</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

                <script>
                import { inject, ref, onMounted, onBeforeUnmount, watch } from 'vue'

                export default {
                  name: 'PreviewPanel',
                  setup() {
                    const connected = inject('connected')
                    const previewAvailable = inject('previewAvailable')
                    const messages = inject('messages')
                    const previewVideo = ref(null)
                    const userInteractionRequired = ref(true) // Start with user interaction required
                    let stream = null

                    const initPreview = async () => {
                      // Mark that user interaction has occurred
                      userInteractionRequired.value = false;

                      if (!previewVideo.value) return

                      try {
                        // First, enumerate all video devices to find the Tarva Engine Virtual Camera
                        const devices = await navigator.mediaDevices.enumerateDevices()
                        console.log('Available media devices:', devices);
                        const videoDevices = devices.filter(device => device.kind === 'videoinput');
                        console.log('Video input devices:', videoDevices);

                        // Look for OBS or virtual camera devices with more flexible matching
                        const obsCamera = videoDevices.find(device => {
                          const label = device.label.toLowerCase();
                          return label.includes('tarva engine virtual camera') ||
                                 label.includes('obs virtual camera') ||
                                 label.includes('obs-camera') ||
                                 label.includes('virtualcam') ||
                                 label.includes('obs');
                        })

                        if (obsCamera) {
                          const constraints = {
                            video: {
                              deviceId: { exact: obsCamera.deviceId }
                            }
                          }
                          messages.value.unshift({
                            type: 'info',
                            text: `Found OBS Virtual Camera: ${obsCamera.label} (ID: ${obsCamera.deviceId})`,
                            timestamp: new Date()
                          })

                          if (stream) {
                            const tracks = stream.getTracks()
                            tracks.forEach(track => track.stop())
                          }

                          stream = await navigator.mediaDevices.getUserMedia(constraints)
                          if (previewVideo.value) {
                            previewVideo.value.srcObject = stream
                            previewAvailable.value = true
                            messages.value.unshift({
                              type: 'info',
                              text: 'Virtual camera feed connected.',
                              timestamp: new Date()
                            })
                          }
                        } else {
                          messages.value.unshift({
                            type: 'warning',
                            text: 'OBS Virtual Camera not found. Please ensure it is running. Available video devices: ' + videoDevices.map(d => d.label).join(', '),
                            timestamp: new Date()
                          })
                          previewAvailable.value = false
                        }
                      } catch (err) {
                        console.error('Error accessing virtual camera:', err)
                        messages.value.unshift({
                          type: 'error',
                          text: `Failed to access virtual camera: ${err.message}`,
                          timestamp: new Date()
                        })
                        previewAvailable.value = false
                      }
                    }

                    const disposePreview = () => {
                      if (stream) {
                        const tracks = stream.getTracks()
                        tracks.forEach(track => track.stop())
                        stream = null
                      }
                      if (previewVideo.value && previewVideo.value.srcObject) {
                        previewVideo.value.srcObject = null
                      }
                      previewAvailable.value = false
                    }

                    // Watch for connection changes
                    watch(connected, (newVal) => {
                      if (newVal) {
                        // Don't automatically init preview, wait for user interaction
                        // User needs to click the button to start preview
                      } else {
                        disposePreview();
                      }
                    });

                    onMounted(() => {
                      // Listen for virtual camera status changes
                      if (window.electronAPI && window.electronAPI.onVirtualCameraStatus) {
                        window.electronAPI.onVirtualCameraStatus((status) => {
                          if (status === 'started') {
                            // Don't automatically init preview, wait for user interaction
                            // initPreview() will be called when user clicks the button
                          }
                        });
                      } else {
                        console.log('Virtual camera monitoring not available in browser environment');
                      }

                      // Don't automatically initialize preview when component mounts
                      // Wait for user interaction
                    });

                    onBeforeUnmount(() => {
                      disposePreview()
                    })

                    return {
                      connected,
                      previewAvailable,
                      previewVideo,
                      userInteractionRequired,
                      initPreview
                    }
  }
}
</script>

<style scoped>
.preview-panel {
  margin: 30px 0;
}

.preview-container {
  text-align: center;
  margin-top: 15px;
}

.preview-container video {
  max-width: 100%;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.no-preview {
  border: 1px dashed #ccc;
  border-radius: 5px;
  padding: 80px 40px;
  color: #777;
}
</style>