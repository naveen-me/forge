<template>
  <div class="media-library-tab">
    <input type="text" v-model="searchQuery" placeholder="Search media..." class="form-control mb-2">
    <div v-if="loading" class="text-center">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>
    <draggable v-else class="list-group"
               :list="filteredMedia"
               :group="{ name: 'schedule', pull: 'clone', put: false }"
               item-key="id">
      <template #item="{element}">
        <li class="list-group-item">
          {{ element.displayName || element.filename }}
        </li>
      </template>
    </draggable>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import draggable from 'vuedraggable';
import mediaLibraryService from '../../services/mediaLibraryService';

export default {
  name: 'MediaLibraryTab',
  components: {
    draggable,
  },
  setup() {
    const media = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const searchQuery = ref('');

    const fetchMedia = async () => {
      try {
        const result = await mediaLibraryService.getMediaLibrary();
        if (result.success) {
          media.value = result.data;
        } else {
          throw new Error(result.error);
        }
      } catch (err) {
        error.value = `Failed to load media library: ${err.message}`;
        console.error(err);
      } finally {
        loading.value = false;
      }
    };

    onMounted(fetchMedia);

    const filteredMedia = computed(() => {
      if (!searchQuery.value) {
        return media.value;
      }
      return media.value.filter(item =>
        (item.displayName || item.filename).toLowerCase().includes(searchQuery.value.toLowerCase())
      );
    });

    return {
      loading,
      error,
      searchQuery,
      filteredMedia,
    };
  },
};
</script>