<template>
  <div class="card">
    <div class="card-header">
      <h5 class="mb-0">{{ formTitle }}</h5>
    </div>
    <div class="card-body">
      <div class="row">
        <!-- Left Column: Form Fields -->
        <div class="col-md-7">
          <form @submit.prevent="submitForm">
            <div class="mb-3">
              <label for="overlayName" class="form-label">Name</label>
              <input type="text" class="form-control" id="overlayName" v-model="form.name" required>
            </div>
            <div v-if="!isGroup">
              <div class="mb-3">
                <label for="overlayType" class="form-label">Type</label>
                <select class="form-select" id="overlayType" v-model="form.type">
                  <option value="image">Image</option>
                  <option value="video">Video</option>
                </select>
              </div>
              <div class="mb-3">
                <label for="overlayContent" class="form-label">Content Path</label>
                <div class="input-group">
                  <input type="text" class="form-control" id="overlayContent" v-model="form.content" required readonly>
                  <button class="btn btn-outline-secondary" type="button" @click="selectFile">Browse...</button>
                </div>
              </div>
              <div class="row">
                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="overlayX" class="form-label">Pos X</label>
                    <input type="number" class="form-control" id="overlayX" v-model.number="form.x">
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="overlayY" class="form-label">Pos Y</label>
                    <input type="number" class="form-control" id="overlayY" v-model.number="form.y">
                  </div>
                </div>
              </div>
              <div class="row">
                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="overlayWidth" class="form-label">Width</label>
                    <input type="number" class="form-control" id="overlayWidth" v-model.number="form.width">
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="overlayHeight" class="form-label">Height</label>
                    <input type="number" class="form-control" id="overlayHeight" v-model.number="form.height">
                  </div>
                </div>
              </div>
              <div class="mb-3">
                <label for="overlayFit" class="form-label">Fit</label>
                <select class="form-select" id="overlayFit" v-model="form.fit">
                  <option value="fit">Fit (preserve aspect)</option>
                  <option value="cover">Cover (fill area, crop)</option>
                  <option value="fill">Fill (stretch to area)</option>
                </select>
              </div>
            </div>
          </form>
        </div>
        <!-- Right Column: Preview -->
        <div class="col-md-5">
          <h6>Preview</h6>
          <OverlayPreview :overlays="overlaysForPreview" />
        </div>
      </div>
    </div>
    <div class="card-footer d-flex justify-content-end">
      <button type="button" class="btn btn-danger me-auto" @click="emitDelete" v-if="!isNew">Delete</button>
      <button type="button" class="btn btn-secondary me-2" @click="$emit('cancel')">Cancel</button>
      <button type="button" class="btn btn-primary" @click="submitForm" :disabled="isGroup">Save</button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, defineProps, defineEmits } from 'vue';
import OverlayPreview from './OverlayPreview.vue';
import apiClient from '../../api.js';

const props = defineProps({
  overlay: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['save', 'delete', 'cancel']);

const isNew = computed(() => !props.overlay || !props.overlay.id);
const isGroup = computed(() => props.overlay && props.overlay.type === 'group');

const formTitle = computed(() => {
  if (isNew.value) return 'Add New Overlay';
  if (isGroup.value) return `Viewing Group: ${props.overlay.name}`;
  return `Edit Overlay: ${props.overlay.name}`;
});

const form = ref({});
const groupMembers = ref([]);
const overlaysForPreview = computed(() => {
  if (isGroup.value) {
    return groupMembers.value;
  }
  return form.value.content ? [form.value] : [];
});

watch(() => props.overlay, async (newOverlay) => {
  if (newOverlay) {
    form.value = { ...newOverlay };
    if (newOverlay.type === 'group') {
      const response = await apiClient.getOverlaysByGroupId(newOverlay.id);
      if (response.success) {
        groupMembers.value = response.data;
      }
    } else {
      groupMembers.value = [];
    }
  } else {
    // Reset for "Add New"
    form.value = {
      name: '',
      type: 'image',
      content: '',
      x: 0,
      y: 0,
      width: 400,
      height: 300,
      fit: 'fit'
    };
    groupMembers.value = [];
  }
}, { immediate: true, deep: true });

const selectFile = async () => {
  if (window.electronAPI) {
    const result = await window.electronAPI.showOpenDialog({
      properties: ['openFile'],
      filters: form.value.type === 'image'
        ? [{ name: 'Images', extensions: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'] }]
        : [{ name: 'Videos', extensions: ['mp4', 'mov', 'mkv', 'webm'] }]
    });
    if (!result.canceled && result.filePaths.length > 0) {
      form.value.content = result.filePaths[0];
    }
  }
};

const submitForm = () => {
  // Create a clean copy of the form data
  const cleanData = {
    name: form.value.name,
    type: form.value.type,
    content: form.value.content,
    x: form.value.x,
    y: form.value.y,
    width: form.value.width,
    height: form.value.height,
    fit: form.value.fit
  };

  // Only include groupId if it exists
  if (form.value.groupId !== undefined) {
    cleanData.groupId = form.value.groupId;
  }

  // Only include id if it exists (for updates)
  if (form.value.id !== undefined) {
    cleanData.id = form.value.id;
  }

  emit('save', cleanData);
};
</script>

<style scoped>
.card {
  height: 85vh;
  display: flex;
  flex-direction: column;
}
.card-body {
  flex-grow: 1;
  overflow-y: auto;
}
</style>
