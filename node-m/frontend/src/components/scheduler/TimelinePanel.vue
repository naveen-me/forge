
<template>
  <div class="timeline-panel card">
    <div class="card-header d-flex justify-content-between align-items-center">
      <h5 class="mb-0">Schedule for {{ selectedDate }}</h5>
      <div>
        <button 
          v-if="!schedule" 
          class="btn btn-primary me-2" 
          @click="createSchedule"
          :disabled="loading"
        >
          <span v-if="loading" class="spinner-border spinner-border-sm me-1" role="status"></span>
          Create Schedule
        </button>
        <button 
          v-else 
          class="btn btn-outline-secondary me-2" 
          @click="togglePublish"
          :class="schedule.status === 'published' ? 'btn-success' : 'btn-warning'"
          :disabled="loading"
        >
          {{ schedule.status === 'published' ? 'Unpublish' : 'Publish' }}
        </button>
        <button 
          v-if="schedule" 
          class="btn btn-outline-danger" 
          @click="clearSchedule"
          :disabled="loading"
        >
          Clear Schedule
        </button>
      </div>
    </div>
    <div class="card-body">
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" style="width: 3rem; height: 3rem;" role="status"></div>
      </div>
      <div v-else-if="error" class="alert alert-danger">{{ error }}</div>
      <div v-else-if="!schedule">
        <div class="text-center py-5 text-muted">
          <i class='bx bx-calendar' style="font-size: 4rem;"></i>
          <h4 class="mt-3">No schedule for this date</h4>
          <p>Create a schedule to get started.</p>
        </div>
      </div>
      <div v-else>
        <timeline :schedule="schedule" @item-edit="editingItem = $event" />
        <item-editor-modal 
          :item="editingItem" 
          @close="editingItem = null"
          @save="onItemSaved"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch, onMounted } from 'vue';
import DatePicker from './DatePicker.vue';
import Timeline from './Timeline.vue';
import ItemEditorModal from './ItemEditorModal.vue';
import TimelineControls from './TimelineControls.vue';
import ConfirmModal from './ConfirmModal.vue';
import PromptModal from './PromptModal.vue';
import scheduleService from '../../services/scheduleService';

const getToday = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

export default {
  name: 'TimelinePanel',
  components: {
    DatePicker,
    Timeline,
    ItemEditorModal,
    TimelineControls,
    ConfirmModal,
    PromptModal,
  },
  setup() {
    const selectedDate = ref(getToday()); // Use date only
    const schedule = ref(null);
    const loading = ref(false);
    const error = ref(null);
    const editingItem = ref(null);

    const fetchSchedule = async () => {
      if (!selectedDate.value) return;
      loading.value = true;
      error.value = null;
      try {
        const response = await scheduleService.getSchedulesByDate(selectedDate.value);
        if (response) {
          schedule.value = response;
        } else {
          schedule.value = null;
        }
      } catch (err) {
        error.value = `Failed to load schedule: ${err.message}`;
        schedule.value = null; // Clear schedule on error
      } finally {
        loading.value = false;
      }
    };

    const createSchedule = async () => {
      try {
        await scheduleService.createSchedule({ date: selectedDate.value, name: `Schedule for ${selectedDate.value}` });
        fetchSchedule();
      } catch (err) {
        error.value = `Failed to create schedule: ${err.message}`;
      }
    };

    const togglePublish = async () => {
        if (!schedule.value) return;
        const newStatus = schedule.value.status === 'published' ? 'draft' : 'published';
        try {
            await scheduleService.updateSchedule(schedule.value.id, { status: newStatus });
            fetchSchedule();
        } catch (err) {
            error.value = `Failed to update status: ${err.message}`;
        }
    };

    const clearSchedule = async () => {
        if (!schedule.value) return;
        if (!confirm('Are you sure you want to clear this schedule?')) return;
        try {
            await scheduleService.deleteSchedule(schedule.value.id);
            fetchSchedule();
        } catch (err) {
            error.value = `Failed to delete schedule: ${err.message}`;
        }
    };

    const onItemSaved = () => {
      // Refresh the schedule to show the updated item
      fetchSchedule();
      editingItem.value = null;
    };

    onMounted(fetchSchedule);
    watch(selectedDate, fetchSchedule);

    return {
      selectedDate, schedule, loading, error, editingItem,
      fetchSchedule, createSchedule, togglePublish, clearSchedule, onItemSaved
    };
  },
};
</script>

<style scoped>
.timeline-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.card-body {
  overflow-y: auto;
  flex-grow: 1;
}
</style>
