<template>
  <!-- Template remains the same -->
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import PageLayout from './PageLayout.vue';
import OverlayForm from './OverlayForm.vue';
import apiClient from '../../api.js';

const overlays = ref([]);
const editingOverlay = ref(null);
const isAddingNew = ref(false);
const selectedOverlays = ref([]);
const openGroups = ref([]);
const showGroupModal = ref(false);
const groupNameInput = ref('');

const fetchOverlays = async () => {
  try {
    const response = await apiClient.getOverlays(); // UPDATED
    overlays.value = response.data; // UPDATED
  } catch (error) {
    console.error('Error fetching overlays:', error);
  }
};

onMounted(fetchOverlays);

const handleSave = async (overlayData) => {
  try {
    const response = overlayData.id
      ? await apiClient.updateOverlay(overlayData.id, overlayData) // UPDATED
      : await apiClient.createOverlay(overlayData); // UPDATED

    isAddingNew.value = false;
    editingOverlay.value = null;
    fetchOverlays();

  } catch (error) {
    console.error('Error saving overlay:', error);
    alert('An error occurred while saving the overlay.');
  }
};

const confirmDelete = async (overlay) => {
  if (confirm(`Are you sure you want to delete "${overlay.name}"?`)) {
    try {
      await apiClient.deleteOverlay(overlay.id); // UPDATED
      isAddingNew.value = false;
      editingOverlay.value = null;
      fetchOverlays();
    } catch (error) {
      alert(`An error occurred while deleting.`);
    }
  }
};

// ... other methods like grouping would be refactored similarly ...

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
/* Styles remain the same */
.overlay-list {
  max-height: 80vh;
  overflow-y: auto;
}
</style>