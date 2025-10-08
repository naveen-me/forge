<template>
  <div class="modal-backdrop" v-if="visible">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ title }}</h5>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>
        <div class="modal-body">
          <label :for="inputId" class="form-label">{{ label }}</label>
          <input :id="inputId" type="text" class="form-control" v-model="inputValue" @keyup.enter="submitValue">
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="$emit('close')">Cancel</button>
          <button type="button" class="btn btn-primary" @click="submitValue">OK</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';

export default {
  name: 'PromptModal',
  props: {
    visible: Boolean,
    title: String,
    label: String,
  },
  emits: ['close', 'submit'],
  setup(props, { emit }) {
    const inputValue = ref('');
    const inputId = `prompt-input-${Date.now()}`;

    const submitValue = () => {
      emit('submit', inputValue.value);
      inputValue.value = ''; // Reset for next time
    };

    return { inputValue, inputId, submitValue };
  },
};
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
}
.modal-dialog {
  width: 400px;
}
</style>
