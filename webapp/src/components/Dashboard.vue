<template>
  <div>
    <h1 class="text-3xl font-bold text-gray-800 mb-6">Dashboard</h1>

  </div>
</template>

<script setup>
import { onMounted, computed } from 'vue';
import { useDashboardStore } from '../store';

const dashboard = useDashboardStore();

onMounted(() => {
  dashboard.fetchStats();
});

const handleObsConnect = () => {
  // In a real app, you'd get these from a form or settings
  const credentials = {
    address: 'localhost:4455',
    password: 'obs-password'
  };
  dashboard.connectObs(credentials);
};

const obsStatusClass = computed(() => {
  if (dashboard.obsStatus.includes('Successfully')) {
    return 'text-success';
  } else if (dashboard.obsStatus.includes('Failed')) {
    return 'text-danger';
  }
  return 'text-muted';
});
</script>

<style scoped>
.card {
  border-radius: 0.75rem;
}
pre {
  max-height: 200px;
  overflow-y: auto;
}
</style>
