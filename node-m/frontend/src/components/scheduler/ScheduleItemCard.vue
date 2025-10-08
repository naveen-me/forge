<template>
  <div
    class="schedule-item-card card mb-2"
    :class="cardClasses"
    @mouseenter="showOverlay = true"
    @mouseleave="showOverlay = false"
  >
    <div class="card-body p-2">
      <div class="d-flex">
        <!-- Thumbnail with play icon overlay -->
        <div class="thumbnail-container position-relative me-2" style="width: 60px; height: 40px;">
          <img
            v-if="thumbnailPath"
            :src="thumbnailPath"
            alt="Thumbnail"
            class="img-fluid rounded"
            style="width: 60px; height: 40px; object-fit: cover;"
            @click="playMedia"
          />
          <div
            v-else
            class="thumbnail-placeholder d-flex align-items-center justify-content-center"
            style="width: 60px; height: 40px; background-color: #f8f9fa; border: 1px solid #dee2e6; border-radius: 0.25rem;"
          >
            <i class="bx bx-image-alt text-muted"></i>
          </div>

          <!-- Play icon overlay -->
          <div
            v-if="showOverlay || isPlaying"
            class="play-overlay position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center"
            style="background-color: rgba(0,0,0,0.5); border-radius: 0.25rem;"
            @click="playMedia"
          >
            <i
              :class="isPlaying ? 'bx bx-pause-circle bx-lg text-white' : 'bx bx-play-circle bx-lg text-white'"
              style="cursor: pointer;"
            ></i>
          </div>
        </div>

        <!-- Media info section -->
        <div class="flex-grow-1">
          <!-- Name and item type -->
          <div class="d-flex justify-content-between align-items-start">
            <div>
              <span class="fw-bold text-truncate" style="max-width: 200px; display: inline-block;">
                {{ item.itemType === 'gap' ? 'Gap' : (item.displayName || item.filename || 'Unnamed Item') }}
              </span>
              <span class="badge bg-secondary ms-2">{{ item.itemType }}</span>
            </div>

            <!-- Action buttons -->
            <div class="d-flex">
              <!-- 3-dot menu -->
              <div class="dropdown">
                <button
                  ref="dropdown"
                  class="btn btn-sm btn-outline-secondary dropdown-toggle dropdown-no-caret"
                  type="button"
                  data-bs-toggle="dropdown"
                  aria-expanded="false"
                >
                  <i class="bx bx-dots-vertical-rounded"></i>
                </button>
                <ul class="dropdown-menu">
                  <li><a class="dropdown-item" href="#" @click.prevent="addGap('up')"><i class="bx bx-up-arrow-alt me-2"></i>Add Gap Above</a></li>
                  <li><a class="dropdown-item" href="#" @click.prevent="addGap('down')"><i class="bx bx-down-arrow-alt me-2"></i>Add Gap Below</a></li>
                  <li><hr class="dropdown-divider"></li>
                  <li><a class="dropdown-item" href="#" @click.prevent="$emit('edit', item)"><i class="bx bx-edit me-2"></i>Edit</a></li>
                  <li><a class="dropdown-item" href="#" @click.prevent="duplicateItem"><i class="bx bx-copy me-2"></i>Duplicate</a></li>
                  <li><a class="dropdown-item" href="#" @click.prevent="repeatItem"><i class="bx bx-repeat me-2"></i>Repeat</a></li>
                  <li><hr class="dropdown-divider"></li>
                  <li><a class="dropdown-item text-danger" href="#" @click.prevent="deleteItem"><i class="bx bx-trash me-2"></i>Delete</a></li>
                </ul>
              </div>
            </div>
          </div>

          <!-- Duration and time info -->
          <div class="d-flex justify-content-between mt-1">
            <div>
              <small class="text-muted">{{ formattedDuration }}</small>
            </div>
            <div>
              <small class="text-muted">{{ formattedStartTime }} - {{ formattedEndTime }}</small>
            </div>
          </div>

          <!-- Notes if present -->
          <div v-if="item.notes" class="mt-1">
            <small class="text-muted">{{ item.notes }}</small>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, inject, ref, onMounted } from 'vue';
import { Dropdown } from 'bootstrap';
import { Modal } from 'bootstrap';

// Helper functions for time formatting
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const [h, m] = timeStr.split(':');
  return `${h}:${m}`;
};

const formatDuration = (seconds) => {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  return [h, m, s]
    .map(v => v < 10 ? '0' + v : v)
    .filter((v, i) => v !== '00' || i > 0)
    .join(':');
};

const addSecondsToTime = (timeStr, seconds) => {
  const [h, m, s] = timeStr.split(':').map(Number);
  const date = new Date();
  date.setHours(h, m, s + seconds);
  return date.toTimeString().split(' ')[0];
};

export default {
  name: 'ScheduleItemCard',
  props: {
    item: {
      type: Object,
      required: true,
    },
  },
  emits: ['edit', 'add-gap', 'duplicate', 'repeat', 'delete', 'play-media'],
  setup(props, { emit }) {
    const playbackState = inject('playbackState');
    const showOverlay = ref(false);
    const dropdown = ref(null);

    onMounted(() => {
      if (dropdown.value) {
        new Dropdown(dropdown.value);
      }
    });

    const itemDetails = ref(null);

    // Load item details if not already present
    const loadItemDetails = async () => {
      if (!window.electron) return;
      if (!itemDetails.value) {
        try {
          let result;
          if (props.item.itemType === 'media') {
            result = await window.electron.ipcRenderer.invoke('db-get-media-by-id', props.item.itemId);
          } else if (props.item.itemType === 'ad') {
            result = await window.electron.ipcRenderer.invoke('ad-get-by-id', props.item.itemId);
          }

          if (result.success) {
            itemDetails.value = result.data;
          }
        } catch (error) {
          console.error('Error loading item details:', error);
        }
      }
    };

    // Load item details when component is accessed
    loadItemDetails();

    const formattedStartTime = computed(() => formatTime(props.item.startTime));
    const formattedDuration = computed(() => formatDuration(props.item.duration));
    const formattedEndTime = computed(() => {
      if (!props.item.startTime || !props.item.duration) return '';
      return formatTime(addSecondsToTime(props.item.startTime, props.item.duration));
    });

    const isPlaying = computed(() => playbackState?.currentItemId === props.item.id);

    const thumbnailPath = computed(() => {
      if (itemDetails.value?.thumbnailPath) {
        // If it's a thumbnail from our service
        if (itemDetails.value.thumbnailPath.startsWith('thumbnails/')) {
          return `thumbnail://${itemDetails.value.thumbnailPath}`;
        } else {
          return `thumbnail://${itemDetails.value.thumbnailPath}`;
        }
      } else if (props.item.thumbnailPath) {
        return `thumbnail://${props.item.thumbnailPath}`;
      }
      return null;
    });

    const cardClasses = computed(() => ({
      'border-primary': !isPlaying.value,
      'border-success playing': isPlaying.value,
    }));

    const playMedia = () => {
      emit('play-media', props.item);
    };

    const addGap = (position) => {
      emit('add-gap', { item: props.item, position });
    };

    const duplicateItem = () => {
      emit('duplicate', props.item);
    };

    const repeatItem = () => {
      emit('repeat', props.item);
    };

    const deleteItem = () => {
      emit('delete', props.item);
    };

    return {
      formattedStartTime,
      formattedDuration,
      formattedEndTime,
      cardClasses,
      isPlaying,
      thumbnailPath,
      showOverlay,
      playMedia,
      addGap,
      duplicateItem,
      repeatItem,
      deleteItem,
      dropdown,
    };
  },
};
</script>

<style scoped>
.schedule-item-card {
  cursor: grab;
  transition: all 0.2s ease-in-out;
  border-left-width: 5px;
}

.playing {
  background-color: #e6ffed;
  border-left-color: #198754 !important; /* Bootstrap success green */
}

.thumbnail-container {
  flex-shrink: 0;
}

.play-overlay {
  cursor: pointer;
  opacity: 0.9;
}

.dropdown-no-caret::after {
  display: none !important;
}

.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>