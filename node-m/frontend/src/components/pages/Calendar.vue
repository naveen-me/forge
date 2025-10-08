<template>
  <PageLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1>Schedule</h1>
      <button class="btn btn-primary" @click="loadSchedules">
        <i class="bi bi-arrow-clockwise"></i> Refresh
      </button>
    </div>

    <div v-if="loading" class="text-center">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>

    <div v-else-if="error" class="alert alert-danger" role="alert">
      {{ error }}
    </div>

    <div v-else>
      <div class="row">
        <div class="col-12">
          <div class="table-responsive">
            <table class="table table-striped table-hover">
              <thead>
                <tr>
                  <th>Media ID</th>
                  <th>Type</th>
                  <th>Start Time</th>
                  <th>End Time</th>
                  <th>Status</th>
                  <th>Recurrence</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="schedule in schedules" :key="schedule.id">
                  <td>{{ schedule.mediaId }}</td>
                  <td>{{ schedule.type }}</td>
                  <td>{{ new Date(schedule.startTime).toLocaleString() }}</td>
                  <td>{{ schedule.endTime ? new Date(schedule.endTime).toLocaleString() : 'N/A' }}</td>
                  <td>{{ schedule.status }}</td>
                  <td>{{ schedule.recurrence || 'None' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </PageLayout>
</template>

<script>
import { ref, onMounted } from 'vue';
import PageLayout from './PageLayout.vue';
import apiClient from '../../api.js';

export default {
  name: 'Calendar',
  components: {
    PageLayout
  },
  setup() {
    const schedules = ref([]);
    const loading = ref(false);
    const error = ref(null);

    const loadSchedules = async () => {
      loading.value = true;
      error.value = null;

      try {
        const response = await apiClient.getSchedules();
        if (result.success) {
          schedules.value = result.data;
        } else {
          error.value = result.error;
        }
      } catch (err) {
        error.value = err.message;
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      loadSchedules();
    });

    return {
      schedules,
      loading,
      error,
      loadSchedules
    };
  }
};
</script>
