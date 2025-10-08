<template>
  <div class="date-time-picker-container">
    <label class="form-label">Schedule Date & Time</label>
    <div class="input-group">
      <input
        type="date"
        class="form-control"
        :value="parsedDate"
        @input="updateDate($event.target.value)"
      />
      <input
        type="time"
        class="form-control"
        :value="parsedTime"
        @input="updateTime($event.target.value)"
      />
      <button
        class="btn btn-outline-secondary"
        type="button"
        @click="setCurrentDateTime"
      >
        <i class="bx bx-current-location"></i>
      </button>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue';

export default {
  name: 'DatePicker',
  props: {
    modelValue: {
      type: String, // Expected format: "YYYY-MM-DD HH:MM:SS" or "YYYY-MM-DD"
      required: true,
    },
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const parsedDateTime = computed(() => {
      // Parse the date-time string, handle both formats (with and without time)
      let dateObj;
      if (props.modelValue.includes(' ')) {
        // Full datetime format
        dateObj = new Date(`${props.modelValue.replace(' ', 'T')}Z`);
      } else if (props.modelValue.includes('T')) {
        // ISO string format
        dateObj = new Date(props.modelValue);
      } else {
        // Date only format
        dateObj = new Date(`${props.modelValue}T00:00:00Z`);
      }

      // Adjust for local timezone
      const offset = dateObj.getTimezoneOffset() * 60000; // in milliseconds
      return new Date(dateObj.getTime() + offset);
    });

    const parsedDate = computed(() => {
      return parsedDateTime.value.toISOString().split('T')[0];
    });

    const parsedTime = computed(() => {
      const time = parsedDateTime.value.toTimeString().substring(0, 5);
      return time;
    });

    const updateDate = (newDate) => {
      const time = parsedTime.value;
      emit('update:modelValue', `${newDate} ${time}`);
    };

    const updateTime = (newTime) => {
      const date = parsedDate.value;
      emit('update:modelValue', `${date} ${newTime}`);
    };

    const setCurrentDateTime = () => {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');

      emit('update:modelValue', `${year}-${month}-${day} ${hours}:${minutes}:00`);
    };

    return {
      parsedDate,
      parsedTime,
      updateDate,
      updateTime,
      setCurrentDateTime,
    };
  },
};
</script>

<style scoped>
.date-time-picker-container {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  margin-bottom: 1rem;
}

.input-group {
  display: flex;
  gap: 0.25rem;
}

.form-control {
  flex: 1;
}
</style>