<template>
  <div class="calendar-container p-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1 class="h3 mb-0 text-gray-800">Calendar</h1>
      <button class="btn btn-primary" @click="loadSchedules">
        <i class="bx bx-refresh"></i> Refresh
      </button>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" style="width: 3rem; height: 3rem;" role="status"></div>
    </div>

    <div v-else-if="error" class="alert alert-danger" role="alert">
      {{ error }}
    </div>

    <div v-else class="card shadow-sm">
      <div class="card-body">
        <div class="table-responsive">
          <table class="table table-striped table-hover">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Date</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="schedule in schedules" :key="schedule.id">
                <td>{{ schedule.id }}</td>
                <td>{{ schedule.name }}</td>
                <td>{{ schedule.date }}</td>
                <td>
                  <span :class="`badge ${schedule.status === 'published' ? 'bg-success' : 'bg-warning'}`">
                    {{ schedule.status }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-primary me-1" @click="viewSchedule(schedule.id)">
                    <i class='bx bx-show'></i> View
                  </button>
                  <button class="btn btn-sm btn-outline-secondary me-1" @click="editSchedule(schedule.id)">
                    <i class='bx bx-edit'></i> Edit
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import scheduleService from '../../services/scheduleService';
import { useRouter } from 'vue-router';

export default {
  name: 'Calendar',
  setup() {
    const schedules = ref([]);
    const loading = ref(false);
    const error = ref(null);
    const router = useRouter();

    const loadSchedules = async () => {
      loading.value = true;
      error.value = null;

      try {
        // For now, we'll fetch all schedules or recent schedules
        // In a real implementation, you might want to fetch schedules by date range
        const response = await scheduleService.getSchedulesByDate(new Date().toISOString().split('T')[0]);
        // Calendar should probably fetch multiple schedules, so we'll need a different endpoint
        // For now, let's assume we need to fetch all schedules differently
        console.warn('Calendar needs to implement proper schedule fetching');
        schedules.value = []; // Placeholder until we implement proper fetching
      } catch (err) {
        error.value = err.message;
      } finally {
        loading.value = false;
      }
    };

    const viewSchedule = (id) => {
      router.push(`/dashboard/scheduler?scheduleId=${id}`);
    };

    const editSchedule = (id) => {
      router.push(`/dashboard/scheduler?scheduleId=${id}&edit=true`);
    };

    onMounted(() => {
      loadSchedules();
    });

    return {
      schedules,
      loading,
      error,
      loadSchedules,
      viewSchedule,
      editSchedule
    };
  }
};
</script>

<style scoped>
.calendar-container {
  background-color: #f8f9fc;
  color: #5a5c69;
}

.card {
  border: 1px solid #e3e6f0;
  box-shadow: 0 0.15rem 1.75rem 0 rgba(58, 59, 69, 0.15);
}

.btn {
  border-radius: 0.35rem;
  font-weight: 600;
}

.btn-primary {
  background-color: #4e73df;
  border-color: #4e73df;
}
</style>
