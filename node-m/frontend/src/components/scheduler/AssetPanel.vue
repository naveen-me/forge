<template>
  <div class="asset-panel card">
    <div class="card-header">
      <ul class="nav nav-tabs card-header-tabs">
        <li class="nav-item">
          <a class="nav-link" :class="{ active: activeTab === 'media' }" @click="activeTab = 'media'">Media</a>
        </li>
        <li class="nav-item">
          <a class="nav-link" :class="{ active: activeTab === 'ads' }" @click="activeTab = 'ads'">Ads</a>
        </li>
      </ul>
    </div>
    <div class="card-body">
      <div v-show="activeTab === 'media'">
        <media-library-tab />
      </div>
      <div v-show="activeTab === 'ads'">
        <ads-tab />
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import MediaLibraryTab from './MediaLibraryTab.vue';
import AdsTab from './AdsTab.vue';

export default {
  name: 'AssetPanel',
  components: {
    MediaLibraryTab,
    AdsTab,
  },
  setup() {
    const activeTab = ref('media'); // Default to media tab

    return {
      activeTab,
    };
  },
};
</script>

<style scoped>
.asset-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.card-body {
  overflow-y: auto;
  flex-grow: 1;
}
.nav-link {
  cursor: pointer;
}
</style>