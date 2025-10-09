<template>
  <div class="overlays-container p-4">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1 class="h3 mb-0 text-gray-800">Overlays Management</h1>
      <button class="btn btn-primary" @click="openAddForm">
        <i class='bx bx-plus'></i> New Overlay
      </button>
    </div>

    <!-- Overlay List -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" style="width: 3rem; height: 3rem;" role="status"></div>
    </div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>
    <div v-else class="row">
      <div v-for="overlay in overlays" :key="overlay.id" class="col-xl-3 col-lg-4 col-md-6 col-sm-12 mb-4">
        <div class="card overlay-card h-100">
          <div class="card-body">
            <h5 class="card-title">{{ overlay.name }}</h5>
            <p class="card-text text-muted">{{ overlay.type }}</p>
            <p class="card-text">{{ overlay.description || 'No description' }}</p>
          </div>
          <div class="card-footer d-flex justify-content-between">
            <button class="btn btn-sm btn-outline-primary" @click="selectOverlayForEditing(overlay)">
              <i class='bx bx-edit'></i> Edit
            </button>
            <button class="btn btn-sm btn-outline-danger" @click="confirmDelete(overlay)">
              <i class='bx bx-trash'></i> Delete
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Overlay Form Modal -->
    <div v-if="isAddingNew || editingOverlay" class="modal-backdrop" @click="cancelEditing">
      <div class="modal fade show d-block" tabindex="-1" @click.stop>
        <div class="modal-dialog modal-dialog-centered modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">{{ editingOverlay ? 'Edit Overlay' : 'Create New Overlay' }}</h5>
              <button type="button" class="btn-close" @click="cancelEditing"></button>
            </div>
            <div class="modal-body">
              <OverlayForm 
                :overlay="editingOverlay || {}" 
                @save="handleSave" 
                @cancel="cancelEditing"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import PageLayout from './PageLayout.vue';
import OverlayForm from './OverlayForm.vue';
import overlayService from '../../services/overlayService';

const overlays = ref([]);
const editingOverlay = ref(null);
const isAddingNew = ref(false);
const selectedOverlays = ref([]);
const openGroups = ref([]);
const showGroupModal = ref(false);
const groupNameInput = ref('');
const loading = ref(false);
const error = ref(null);

const fetchOverlays = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await overlayService.getOverlays();
    if (response) {
      overlays.value = response;
    } else {
      throw new Error('Failed to load overlays');
    }
  } catch (error) {
    console.error('Error fetching overlays:', error);
    error.value = 'Failed to load overlays';
  } finally {
    loading.value = false;
  }
};

onMounted(fetchOverlays);

const handleSave = async (overlayData) => {
  try {
    let response;
    if (overlayData.id) {
      response = await overlayService.updateOverlay(overlayData.id, overlayData);
    } else {
      response = await overlayService.createOverlay(overlayData);
    }

    if (response) {
      isAddingNew.value = false;
      editingOverlay.value = null;
      await fetchOverlays();
    } else {
      throw new Error('Failed to save overlay');
    }
  } catch (error) {
    console.error('Error saving overlay:', error);
    alert('An error occurred while saving the overlay.');
  }
};

const confirmDelete = async (overlay) => {
  if (confirm(`Are you sure you want to delete "${overlay.name}"?`)) {
    try {
      const response = await overlayService.deleteOverlay(overlay.id);
      if (response) {
        isAddingNew.value = false;
        editingOverlay.value = null;
        await fetchOverlays();
      } else {
        throw new Error('Failed to delete overlay');
      }
    } catch (error) {
      alert(`An error occurred while deleting: ${error.message}`);
    }
  }
};

const topLevelOverlays = computed(() => overlays.value.filter(o => !o.groupId));
const getGroupMembers = (groupId) => overlays.value.filter(o => o.groupId === groupId);
const toggleGroup = (groupId) => {
  const index = openGroups.value.indexOf(groupId);
  if (index > -1) openGroups.value.splice(index, 1);
  else openGroups.value.push(groupId);
};
const isGroupOpen = (groupId) => openGroups.value.includes(groupId);
const openAddForm = () => {
  editingOverlay.value = null;
  isAddingNew.value = true;
};
const selectOverlayForEditing = (overlay) => {
  isAddingNew.value = false;
  editingOverlay.value = { ...overlay };
};
const cancelEditing = () => {
  isAddingNew.value = false;
  editingOverlay.value = null;
};
</script>

<style scoped>
.overlays-container {
  background-color: #f8f9fc;
  color: #5a5c69;
}

.overlay-card {
  border: 1px solid #e3e6f0;
  box-shadow: 0 0.15rem 1.75rem 0 rgba(58, 59, 69, 0.15);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.overlay-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 0.5rem 1.75rem 0 rgba(58, 59, 69, 0.25);
}

.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0,0,0,0.5);
  z-index: 1050;
}

.btn {
  border-radius: 0.35rem;
  font-weight: 600;
}

.btn-primary {
  background-color: #4e73df;
  border-color: #4e73df;
}

.overlay-list {
  max-height: 80vh;
  overflow-y: auto;
}
</style>