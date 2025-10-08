<template>
  <div class="scheduler-view">
    <h1>Scheduler</h1>
    <div class="scheduler-layout">
      <asset-panel class="asset-panel-container" />
      <timeline-panel class="timeline-panel-container" />
    </div>
  </div>
</template>

<script>
import AssetPanel from './AssetPanel.vue';
import TimelinePanel from './TimelinePanel.vue';

export default {
  name: 'SchedulerView',
  components: {
    AssetPanel,
    TimelinePanel,
  },
};
</script>

<style scoped>
.scheduler-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 1rem;
}

.scheduler-layout {
  display: flex;
  flex-grow: 1;
  gap: 1rem;
  overflow: hidden;
}

.asset-panel-container {
  flex: 1;
  min-width: 300px;
  overflow-y: auto;
}

.timeline-panel-container {
  flex: 3;
  overflow-y: auto;
}
</style>