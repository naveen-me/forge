<template>
  <div>
    <h1 class="text-3xl font-bold text-gray-800 mb-6">Dashboard</h1>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-6">
      <div class="bg-white p-6 rounded-lg shadow-md">
        <h5 class="text-gray-600 font-semibold">Playout Stats</h5>
        <p class="text-2xl font-bold text-gray-800 mt-2">{{ dashboard.formattedStats['Videos Played'] || 0 }}</p>
        <p class="text-sm text-gray-500">Videos Played</p>
      </div>
      <div class="bg-white p-6 rounded-lg shadow-md">
        <h5 class="text-gray-600 font-semibold">System Health</h5>
        <p class="text-2xl font-bold text-red-500 mt-2">{{ dashboard.formattedStats['Errors'] || 0 }}</p>
        <p class="text-sm text-gray-500">Errors</p>
      </div>
    </div>

    <!-- Actions -->
    <div class="bg-white p-6 rounded-lg shadow-md mb-6">
      <h5 class="text-xl font-semibold mb-4">Actions</h5>
      <div class="flex space-x-4">
        <button @click="dashboard.testApi" class="bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded">
          Call API Test
        </button>
        <button @click="handleObsConnect" class="bg-purple-500 hover:bg-purple-600 text-white font-bold py-2 px-4 rounded">
          Connect to OBS
        </button>
      </div>
    </div>

    <!-- Status & Responses -->
    <div class="bg-white p-6 rounded-lg shadow-md">
        <h5 class="text-xl font-semibold mb-4">System Status</h5>
        <p class="mb-2"><strong>OBS Status:</strong> <span :class="obsStatusClass" class="font-semibold">{{ dashboard.obsStatus }}</span></p>
        <div v-if="dashboard.apiResponse">
            <h6 class="font-semibold mt-4">API Test Response:</h6>
            <pre class="bg-gray-100 p-3 rounded-md text-sm text-gray-800 overflow-x-auto"><code>{{ dashboard.apiResponse }}</code></pre>
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
