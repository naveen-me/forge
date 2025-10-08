<template>
  <div class="ads-tab">
    <input type="text" v-model="searchQuery" placeholder="Search ads..." class="form-control mb-2">
    <div v-if="loading" class="text-center">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>
    <draggable v-else class="list-group"
               :list="filteredAds"
               :group="{ name: 'schedule', pull: 'clone', put: false }"
               item-key="id">
      <template #item="{element}">
        <li class="list-group-item">
          {{ element.filename }}
        </li>
      </template>
    </draggable>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import draggable from 'vuedraggable';
import apiClient from '../../api.js';

export default {
  name: 'AdsTab',
  components: {
    draggable,
  },
  setup() {
    const ads = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const searchQuery = ref('');

    const fetchAds = async () => {
      try {
        const result = await apiClient.getAllAds();
        if (result.success) {
          ads.value = result.data;
        } else {
          throw new Error(result);
        }
      } catch (err) {
        error.value = `Failed to load ads: ${err.message}`;
        console.error(err);
      } finally {
        loading.value = false;
      }
    };

    onMounted(fetchAds);

    const filteredAds = computed(() => {
      if (!searchQuery.value) {
        return ads.value;
      }
      return ads.value.filter(ad =>
        ad.filename.toLowerCase().includes(searchQuery.value.toLowerCase())
      );
    });

    return {
      loading,
      error,
      searchQuery,
      filteredAds,
    };
  },
};
</script>

<style scoped>
.ads-tab {
  display: flex;
  flex-direction: column;
}
.list-group-item {
  cursor: grab;
}
</style>