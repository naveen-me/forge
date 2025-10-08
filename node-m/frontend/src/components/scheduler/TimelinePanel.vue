
<template>
  <!-- Template remains the same -->
</template>

<script>
import { ref, watch, onMounted, computed } from 'vue';
import { Modal } from 'bootstrap';
import DatePicker from './DatePicker.vue';
import Timeline from './Timeline.vue';
import ItemEditorModal from './ItemEditorModal.vue';
import TimelineControls from './TimelineControls.vue';
import ConfirmModal from './ConfirmModal.vue';
import PromptModal from './PromptModal.vue';
import apiClient from '../../api.js';

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
        const response = await apiClient.getScheduleByDate(selectedDate.value);
        schedule.value = response.data; // apiClient returns data directly
      } catch (err) {
        error.value = `Failed to load schedule: ${err.message}`;
        schedule.value = null; // Clear schedule on error
      } finally {
        loading.value = false;
      }
    };

    const createSchedule = async () => {
      try {
        await apiClient.createSchedule({ date: selectedDate.value, name: `Schedule for ${selectedDate.value}` });
        fetchSchedule();
      } catch (err) {
        error.value = `Failed to create schedule: ${err.message}`;
      }
    };

    const togglePublish = async () => {
        const newStatus = schedule.value.status === 'published' ? 'draft' : 'published';
        try {
            await apiClient.updateSchedule(schedule.value.id, { status: newStatus });
            fetchSchedule();
        } catch (err) {
            error.value = `Failed to update status: ${err.message}`;
        }
    };

    const clearSchedule = async () => {
        try {
            await apiClient.deleteSchedule(schedule.value.id);
            fetchSchedule();
        } catch (err) {
            error.value = `Failed to delete schedule: ${err.message}`;
        }
    };

    // ... other methods like reordering, adding items would be refactored similarly ...
    // This is a partial refactor to show the pattern.

    onMounted(fetchSchedule);
    watch(selectedDate, fetchSchedule);

    return {
      selectedDate, schedule, loading, error, editingItem,
      fetchSchedule, createSchedule, togglePublish, clearSchedule
      // ... return other methods ...
    };
  },
};
</script>

<style scoped>
.timeline-panel { display: flex; flex-direction: column; height: 100%; }
.card-body { overflow-y: auto; flex-grow: 1; }
</style>
