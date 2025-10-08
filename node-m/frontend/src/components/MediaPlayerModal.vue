<template>
  <div class="modal fade" id="mediaPlayerModal" tabindex="-1" aria-labelledby="mediaPlayerModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="mediaPlayerModalLabel">{{ mediaItem?.displayName }}</h5>
          <button type="button" class="btn-close" @click="hide" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <!-- Video Player -->
          <div v-if="mediaItem?.type === 'video'" class="video-player-container">
            <video
              ref="videoPlayer"
              class="w-100"
              controls
              autoplay
              :src="'media://' + mediaItem.filepath"
              @loadedmetadata="onVideoMetadataLoaded"
              @error="onVideoError"
            >
              Your device does not support the video.
            </video>
          </div>

          <!-- Image Viewer -->
          <div v-else-if="mediaItem?.type === 'image'" class="image-viewer-container text-center">
            <img
              :src="'media://' + mediaItem.filepath"
              :alt="mediaItem.displayName"
              class="img-fluid"
              style="max-height: 70vh;"
            >
          </div>

          <!-- Loading State -->
          <div v-else class="text-center">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';

export default {
  name: 'MediaPlayerModal',
  setup() {
    const mediaItem = ref(null);
    const videoPlayer = ref(null);

    const show = (item) => {
      mediaItem.value = item;
      // Use nextTick to ensure the DOM is updated before trying to show the modal
      nextTick(() => {
        const modalElement = document.getElementById('mediaPlayerModal');
        if (modalElement) {
          // Check if bootstrap is available, if not, use fallback
          if (window.bootstrap && window.bootstrap.Modal) {
            const modal = new window.bootstrap.Modal(modalElement);
            modal.show();
          } else {
            // Fallback: manually add the show class and backdrop
            modalElement.classList.add('show');
            modalElement.style.display = 'block';
            // Add backdrop
            const backdrop = document.createElement('div');
            backdrop.className = 'modal-backdrop fade show';
            backdrop.id = 'modal-backdrop';
            document.body.appendChild(backdrop);
            document.body.classList.add('modal-open');
          }
        }
      });
    };

    const hide = () => {
      const modalElement = document.getElementById('mediaPlayerModal');
      if (modalElement) {
        // Check if bootstrap is available, if not, use fallback
        if (window.bootstrap && window.bootstrap.Modal) {
          const modal = window.bootstrap.Modal.getInstance(modalElement);
          if (modal) {
            modal.hide();
          }
        } else {
          // Fallback: manually remove the show class and backdrop
          modalElement.classList.remove('show');
          modalElement.style.display = 'none';
          // Remove backdrop
          const backdrop = document.getElementById('modal-backdrop');
          if (backdrop) {
            backdrop.remove();
          }
          document.body.classList.remove('modal-open');
        }
      }
    };

    const onVideoMetadataLoaded = () => {
      // Video metadata loaded successfully
      console.log('Video metadata loaded');
    };

    const onVideoError = (event) => {
      console.error('Video player error:', event);
    };

    // Add event listeners for modal hidden events to clean up fallback handling
    const handleModalHidden = (event) => {
      // Clean up fallback modal handling
      const backdrop = document.getElementById('modal-backdrop');
      if (backdrop) {
        backdrop.remove();
      }
      document.body.classList.remove('modal-open');
    };

    // Store reference to modal element for cleanup
    const modalElementRef = ref(null);

    onMounted(() => {
      // Add event listener for modal hidden event
      const modalElement = document.getElementById('mediaPlayerModal');
      if (modalElement) {
        modalElement.addEventListener('hidden.bs.modal', handleModalHidden);
        modalElementRef.value = modalElement;
      }
    });

    onUnmounted(() => {
      // Clean up event listener
      if (modalElementRef.value) {
        modalElementRef.value.removeEventListener('hidden.bs.modal', handleModalHidden);
      }
    });

    return {
      mediaItem,
      videoPlayer,
      show,
      hide,
      onVideoMetadataLoaded,
      onVideoError,
    };
  }
};
</script>

<style scoped>
.video-player-container,
.image-viewer-container {
  max-height: 70vh;
  overflow: auto;
}

video {
  max-height: 60vh;
  background-color: #000;
}
</style>
