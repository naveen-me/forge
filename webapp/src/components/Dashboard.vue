<template>
  <div>
    <h1 class="mb-4">Dashboard</h1>

    <!-- Stats -->
    <div class="row mb-4">
      <div class="col-md-6">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Playout Stats</h5>
            <p>Videos Played: {{ dashboard.formattedStats['Videos Played'] || 0 }}</p>
            <p>Errors: {{ dashboard.formattedStats['Errors'] || 0 }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Actions -->
    <div class="row mb-4">
      <div class="col-md-6">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Actions</h5>
            <button @click="dashboard.testApi" class="btn btn-info me-2">Call API Test</button>
            <button @click="handleObsConnect" class="btn btn-primary">Connect to OBS</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Status & Responses -->
    <div class="row">
      <div class="col-md-6">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">System Status</h5>
            <p><strong>OBS Status:</strong> <span :class="obsStatusClass">{{ dashboard.obsStatus }}</span></p>
            <div v-if="dashboard.apiResponse">
              <strong>API Test Response:</strong>
              <pre class="bg-light p-2 rounded"><code>{{ dashboard.apiResponse }}</code></pre>
            </div>
          </div>
        </div>
      </div>
    </div>
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
