<template>
  <div class="modal fade" ref="editorModal" tabindex="-1" aria-labelledby="editorModalLabel" aria-hidden="true" @hidden.bs.modal="onModalHidden">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="editorModalLabel">Edit Schedule Item</h5>
          <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div v-if="!editableItem">Loading...</div>
          <div v-else>
            <ul class="nav nav-tabs">
              <li class="nav-item">
                <a class="nav-link" :class="{ active: activeTab === 'general' }" @click="activeTab = 'general'">General</a>
              </li>
              <li class="nav-item" v-if="editableItem.itemType !== 'gap'">
                <a class="nav-link" :class="{ active: activeTab === 'media' }" @click="activeTab = 'media'">Media & Time</a>
              </li>
              <li class="nav-item">
                <a class="nav-link" :class="{ active: activeTab === 'ads' }" @click="activeTab = 'ads'">Manage Ad Placements</a>
              </li>
              <li class="nav-item">
                <a class="nav-link" :class="{ active: activeTab === 'overlays' }" @click="activeTab = 'overlays'">Overlays</a>
              </li>
            </ul>
            <div class="tab-content pt-3">
              <div v-if="error" class="alert alert-danger">{{ error }}</div>

              <!-- General Tab -->
              <div class="tab-pane" :class="{ 'active show': activeTab === 'general' }">
                <div class="mb-3">
                  <label class="form-label">Item Type</label>
                  <input type="text" class="form-control" :value="editableItem.itemType" readonly />
                </div>
                <div class="mb-3">
                    <label for="itemNotes" class="form-label">Notes</label>
                    <textarea class="form-control" id="itemNotes" rows="3" v-model="editableItem.notes"></textarea>
                </div>
              </div>

              <!-- Media & Time Tab -->
              <div class="tab-pane" :class="{ 'active show': activeTab === 'media' }" v-if="editableItem.itemType !== 'gap'">
                <div class="mb-3">
                  <label class="form-label">Select Media Asset</label>
                  <select v-model="currentMediaId" class="form-select">
                    <option value="">Choose a media asset...</option>
                    <option v-for="item in allMediaAssets" :key="item.id" :value="item.id">{{ item.displayName || item.filename }}</option>
                  </select>
                </div>

                <div class="row mb-3">
                  <div class="col-md-6">
                    <label class="form-label">Start From (seconds)</label>
                    <input type="number" class="form-control" v-model.number="editStartTime" min="0" />
                    <div class="form-text">Offset into the media file to start playback</div>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Play Until (seconds)</label>
                    <input type="number" class="form-control" v-model.number="editEndTime" min="0" :max="maxEndTime" />
                    <div class="form-text">Stop playback after this time (0 = play entire file)</div>
                  </div>
                </div>

                <div class="row mb-3">
                  <div class="col-md-6">
                    <label class="form-label">Schedule Start Time</label>
                    <input type="time" class="form-control" v-model="editScheduleStartTime" />
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Calculated Duration</label>
                    <input type="text" class="form-control" :value="calculatedDuration" readonly />
                  </div>
                </div>
              </div>

              <!-- Ads Management Tab -->
              <div class="tab-pane" :class="{ 'active show': activeTab === 'ads' }">
                <h5>Add Ad Placement</h5>
                <div class="p-2 border rounded mb-3">
                  <div class="row g-2 align-items-end">
                    <div class="col-md-4">
                      <label class="form-label">Ad Name</label>
                      <input type="text" class="form-control" v-model="newAdPlacement.name" placeholder="Enter ad placement name">
                    </div>
                    <div class="col-md-3">
                      <label class="form-label">Ad to Insert</label>
                      <select v-model="newAdPlacement.adId" class="form-select">
                        <option disabled value="">Choose ad...</option>
                        <option v-for="ad in allAds" :key="ad.id" :value="ad.id">{{ ad.displayName || ad.filename }}</option>
                      </select>
                    </div>
                    <div class="col-md-2">
                      <label class="form-label">Offset (sec)</label>
                      <input type="number" v-model.number="newAdPlacement.offset" class="form-control" placeholder="Offset in seconds">
                    </div>
                    <div class="col-md-3">
                      <label class="form-label">Duration (sec)</label>
                      <input type="number" v-model.number="newAdPlacement.duration" class="form-control" placeholder="Duration in seconds">
                    </div>
                  </div>
                  <div class="row g-2 mt-2">
                    <div class="col-md-12">
                      <button class="btn btn-sm btn-success me-2" @click="addAdPlacement">Add Ad Placement</button>
                      <button class="btn btn-sm btn-danger" @click="deleteAdPlacements" :disabled="!selectedAdPlacements.length">Delete Selected</button>
                    </div>
                  </div>
                </div>

                <div class="form-check mb-2">
                  <input class="form-check-input" type="checkbox" id="selectAllAds" v-model="selectAllAdPlacements">
                  <label class="form-check-label" for="selectAllAds">
                    Select all ad placements
                  </label>
                </div>

                <ul class="list-group">
                  <li v-for="(adPlacement, index) in editableItem.adPlacements" :key="index" class="list-group-item">
                    <div class="form-check">
                      <input class="form-check-input" type="checkbox" :value="adPlacement.id" v-model="selectedAdPlacements">
                      <div class="d-flex justify-content-between align-items-center">
                        <div>
                          <strong>{{ getAdName(adPlacement.adId) }}</strong>
                          <small class="text-muted ms-2">Name: {{ adPlacement.name || 'Untitled' }}</small>
                          <br>
                          <small class="text-muted">Offset: {{ adPlacement.offset }}s | Duration: {{ adPlacement.duration }}s</small>
                        </div>
                        <button class="btn btn-sm btn-danger" @click="removeAdPlacement(index)">&times;</button>
                      </div>
                    </div>
                  </li>
                  <li v-if="!editableItem.adPlacements || editableItem.adPlacements.length === 0" class="list-group-item text-muted">No ad placements assigned.</li>
                </ul>
              </div>

              <!-- Overlays Tab -->
              <div class="tab-pane" :class="{ 'active show': activeTab === 'overlays' }">
                <h5>Manage Overlays</h5>
                <div class="p-2 border rounded mb-3">
                    <div class="row g-2 align-items-end">
                        <div class="col-md-6">
                            <label class="form-label">Add Overlay</label>
                            <select v-model="newOverlay.overlayId" class="form-select">
                                <option disabled value="">Choose overlay...</option>
                                <option v-for="o in availableOverlays" :key="o.id" :value="o.id">{{ o.name }}</option>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">Start (sec)</label>
                            <input type="number" v-model.number="newOverlay.startTime" class="form-control" placeholder="Offset in seconds">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">Duration (sec)</label>
                            <input type="number" v-model.number="newOverlay.duration" class="form-control" placeholder="Visible for seconds">
                        </div>
                    </div>
                     <button class="btn btn-sm btn-success mt-2" @click="addOverlay">Add</button>
                </div>
                <div class="form-check mb-2">
                  <input class="form-check-input" type="checkbox" id="selectAllOverlays" v-model="selectAllOverlays">
                  <label class="form-check-label" for="selectAllOverlays">
                    Select all overlays
                  </label>
                </div>
                <ul class="list-group">
                    <li v-for="(overlay, index) in editableItem.overlays" :key="index" class="list-group-item">
                      <div class="form-check">
                        <input class="form-check-input" type="checkbox" :value="overlay.id" v-model="selectedOverlays">
                        <div class="d-flex justify-content-between align-items-center">
                          <div>
                              <strong>{{ getOverlayName(overlay.overlayId) }}</strong>
                              <small class="text-muted ms-2">Starts at {{ overlay.startTime }}s, for {{ overlay.duration }}s</small>
                          </div>
                          <button class="btn btn-sm btn-danger" @click="removeOverlay(index)">&times;</button>
                        </div>
                      </div>
                    </li>
                    <li v-if="!editableItem.overlays || editableItem.overlays.length === 0" class="list-group-item text-muted">No overlays assigned.</li>
                </ul>
                <div class="mt-2">
                  <button class="btn btn-sm btn-danger" @click="deleteOverlays" :disabled="!selectedOverlays.length">Delete Selected</button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-danger me-auto" @click="deleteItem" v-if="editableItem">Delete Item</button>
          <button type="button" class="btn btn-secondary" @click="closeModal">Close</button>
          <button type="button" class="btn btn-primary" @click="saveChanges" :disabled="saving">
            <span v-if="saving" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
            Save changes
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, computed } from 'vue';
import { Modal } from 'bootstrap';
import apiClient from '../../api.js';

export default {
  name: 'ItemEditorModal',
  props: {
    scheduleItem: {
      type: Object,
      default: null,
    },
  },
  emits: ['item-updated', 'close', 'delete-item'],
  setup(props, { emit }) {
    const editorModal = ref(null);
    const modalInstance = ref(null);
    const activeTab = ref('general');
    const editableItem = ref(null);
    const originalItem = ref(null);
    const saving = ref(false);
    const error = ref(null);

    // New state for enhanced functionality
    const availableOverlays = ref([]);
    const newOverlay = ref({ overlayId: '', startTime: 0, duration: 10 });
    const allAds = ref([]);
    const newAdPlacement = ref({ name: '', adId: '', offset: 0, duration: 0 });
    const allMediaAssets = ref([]);
    const selectedAdPlacements = ref([]);
    const selectedOverlays = ref([]);
    const selectAllAdPlacements = ref(false);
    const selectAllOverlays = ref(false);

    // Media & Time controls
    const currentMediaId = ref(null);
    const editStartTime = ref(0);
    const editEndTime = ref(0);
    const editScheduleStartTime = ref('00:00');

    // Fetch all required data
    const fetchSupportData = async () => {
        try {
            const results = await Promise.all([
                window.electronAPI.getAllOverlays(),
                window.electronAPI.getAllAds(),
                window.electronAPI.getMediaLibrary()
            ]);

            if (results[0].success) availableOverlays.value = results[0].data;
            if (results[1].success) allAds.value = results[1].data;
            if (results[2].success) allMediaAssets.value = results[2].data;
        } catch (e) {
            error.value = "Could not load support data (overlays, ads, media).";
        }
    };

    onMounted(() => {
      modalInstance.value = new Modal(editorModal.value);
      fetchSupportData();
    });

    watch(() => props.scheduleItem, async (newItem) => {
      if (newItem) {
        const deepCopy = JSON.parse(JSON.stringify(newItem));
        editableItem.value = deepCopy;
        originalItem.value = JSON.parse(JSON.stringify(newItem));

        // Initialize arrays if they don't exist
        if (!editableItem.value.overlays) editableItem.value.overlays = [];
        if (!originalItem.value.overlays) originalItem.value.overlays = [];

        // Load ad placements from the database
        try {
          const adPlacementsResult = await scheduleService.getScheduleItemAdPlacementsByItem(newItem.id);
          if (adPlacementsResult.success) {
            editableItem.value.adPlacements = adPlacementsResult.data;
            originalItem.value.adPlacements = [...adPlacementsResult.data]; // Create a copy for comparison
          } else {
            editableItem.value.adPlacements = [];
            originalItem.value.adPlacements = [];
          }
        } catch (e) {
          console.error('Error loading ad placements:', e);
          editableItem.value.adPlacements = [];
          originalItem.value.adPlacements = [];
        }

        // Initialize media & time values
        currentMediaId.value = editableItem.value.itemId;
        editStartTime.value = 0; // Default start time
        editEndTime.value = editableItem.value.duration || 0; // Default to full duration
        editScheduleStartTime.value = editableItem.value.startTime ? editableItem.value.startTime.substring(0, 5) : '00:00';

        activeTab.value = 'general';
        error.value = null;
        modalInstance.value?.show();
      } else {
        modalInstance.value?.hide();
      }
    });

    // Watch for select all ad placements
    watch(selectAllAdPlacements, (value) => {
      if (value && editableItem.value && editableItem.value.adPlacements) {
        selectedAdPlacements.value = editableItem.value.adPlacements.map(ap => ap.id);
      } else if (!value) {
        selectedAdPlacements.value = [];
      }
    });

    // Watch for select all overlays
    watch(selectAllOverlays, (value) => {
      if (value && editableItem.value && editableItem.value.overlays) {
        selectedOverlays.value = editableItem.value.overlays.map(o => o.id);
      } else if (!value) {
        selectedOverlays.value = [];
      }
    });

    const getOverlayName = (id) => availableOverlays.value.find(o => o.id === id)?.name || 'Unknown Overlay';
    const getAdName = (id) => allAds.value.find(ad => ad.id === id)?.displayName || allAds.value.find(ad => ad.id === id)?.filename || 'Unknown Ad';

    // Media & time computed properties
    const maxEndTime = computed(() => {
      if (!currentMediaId.value) return 0;
      const media = allMediaAssets.value.find(m => m.id === currentMediaId.value);
      return media ? media.duration : 0;
    });

    const calculatedDuration = computed(() => {
      if (editStartTime.value >= editEndTime || editEndTime.value === 0) {
        // If play until is 0, use full media duration or current item duration
        if (currentMediaId.value) {
          const media = allMediaAssets.value.find(m => m.id === currentMediaId.value);
          return media ? media.duration : editableItem.value.duration;
        }
        return editableItem.value.duration;
      }
      return editEndTime.value - editStartTime.value;
    });

    // Overlay management
    const addOverlay = () => {
        if (!newOverlay.value.overlayId || newOverlay.value.startTime < 0 || newOverlay.value.duration <= 0) {
            error.value = "Please select an overlay and provide valid start time and duration.";
            return;
        }
        // Generate a temporary ID if it's a new overlay
        const overlayToAdd = { ...newOverlay.value, scheduleItemId: editableItem.value.id };
        if (!overlayToAdd.id) {
          overlayToAdd.id = Date.now(); // Temporary ID for new overlays
        }
        editableItem.value.overlays.push(overlayToAdd);
        newOverlay.value = { overlayId: '', startTime: 0, duration: 10 };
        error.value = null;
    };

    const removeOverlay = (index) => editableItem.value.overlays.splice(index, 1);

    const deleteOverlays = () => {
      if (selectedOverlays.value.length === 0) return;
      // Remove all selected overlays
      editableItem.value.overlays = editableItem.value.overlays.filter(
        overlay => !selectedOverlays.value.includes(overlay.id)
      );
      selectedOverlays.value = [];
      selectAllOverlays.value = false;
    };

    // Ad placement management
    const addAdPlacement = () => {
        if (!newAdPlacement.value.adId || newAdPlacement.value.offset < 0) {
            error.value = "Please select an ad and provide a valid offset.";
            return;
        }
        // Generate a temporary ID if it's a new ad placement
        const adPlacementToAdd = {
          ...newAdPlacement.value,
          scheduleItemId: editableItem.value.id,
          id: `temp_${Date.now()}` // Temporary ID for new ad placements
        };
        editableItem.value.adPlacements.push(adPlacementToAdd);
        newAdPlacement.value = { name: '', adId: '', offset: 0, duration: 0 };
        error.value = null;
    };

    const removeAdPlacement = (index) => editableItem.value.adPlacements.splice(index, 1);

    const deleteAdPlacements = () => {
      if (selectedAdPlacements.value.length === 0) return;
      // Remove all selected ad placements
      editableItem.value.adPlacements = editableItem.value.adPlacements.filter(
        adPlacement => !selectedAdPlacements.value.includes(adPlacement.id)
      );
      selectedAdPlacements.value = [];
      selectAllAdPlacements.value = false;
    };

    const closeModal = () => modalInstance.value?.hide();
    const onModalHidden = () => emit('close');

    const deleteItem = async () => {
      if (!editableItem.value) return;
      emit('delete-item', editableItem.value);
      closeModal();
    };

    const saveChanges = async () => {
        if (!editableItem.value) return;
        saving.value = true;
        error.value = null;

        try {
            // Update basic properties
            const updateObj = {
              id: editableItem.value.id,
              notes: editableItem.value.notes
            };

            // Update time properties if changed
            if (editScheduleStartTime.value !== editableItem.value.startTime.substring(0, 5)) {
              // Add seconds to the time if not present
              const fullTime = editScheduleStartTime.value.includes(':') ?
                editScheduleStartTime.value + ':00' :
                editScheduleStartTime.value + ':00:00';
              updateObj.startTime = fullTime;
            }

            // Update duration if changed
            const newDuration = calculatedDuration.value;
            if (newDuration !== editableItem.value.duration) {
              updateObj.duration = newDuration;
            }

            // Update itemId if media was changed
            if (currentMediaId.value && currentMediaId.value !== editableItem.value.itemId) {
              updateObj.itemId = currentMediaId.value;
            }

            // Only send update if there are changes
            if (Object.keys(updateObj).length > 1) { // More than just ID
              await scheduleService.updateScheduleItem(updateObj);
            }

            // Handle ad placements
            const adPlacementsToDelete = originalItem.value.adPlacements.filter(
              ap => !editableItem.value.adPlacements.find(eap => eap.id === ap.id)
            );
            const adPlacementsToAdd = editableItem.value.adPlacements.filter(
              eap => !eap.id || eap.id.toString().startsWith('temp')  // New items have temporary IDs
            );
            const adPlacementsToUpdate = editableItem.value.adPlacements.filter(
              eap => eap.id && !eap.id.toString().startsWith('temp') &&
                     originalItem.value.adPlacements.find(ap => ap.id === eap.id)  // Existing items
            );

            // Delete removed ad placements
            for (const ap of adPlacementsToDelete) {
              if (ap.id && !ap.id.toString().startsWith('temp')) {
                await scheduleService.deleteScheduleItemAdPlacement(ap.id);
              }
            }

            // Add new ad placements
            for (const ap of adPlacementsToAdd) {
              const newAdPlacement = { ...ap };
              // Remove temporary id if present
              if (newAdPlacement.id && newAdPlacement.id.toString().startsWith('temp')) {
                delete newAdPlacement.id;
              }
              await scheduleService.createScheduleItemAdPlacement({
                ...newAdPlacement,
                scheduleItemId: editableItem.value.id
              });
            }

            // Update existing ad placements if changed
            for (const ap of adPlacementsToUpdate) {
              const originalAp = originalItem.value.adPlacements.find(oap => oap.id === ap.id);
              if (!originalAp ||
                  originalAp.name !== ap.name ||
                  originalAp.adId !== ap.adId ||
                  originalAp.offset !== ap.offset ||
                  originalAp.duration !== ap.duration ||
                  originalAp.sortOrder !== ap.sortOrder) {
                await scheduleService.updateScheduleItemAdPlacement(ap.id, ap);
              }
            }

            emit('item-updated');
            closeModal();
        } catch (err) {
            error.value = `Failed to save changes: ${err.message}`;
        } finally {
            saving.value = false;
        }
    };

    return {
      editorModal, activeTab, editableItem, saving, error,
      saveChanges, closeModal, onModalHidden, deleteItem,
      // Enhanced functionality
      availableOverlays, newOverlay, addOverlay, removeOverlay, getOverlayName,
      allAds, newAdPlacement, addAdPlacement, removeAdPlacement, getAdName,
      allMediaAssets, currentMediaId,
      editStartTime, editEndTime, maxEndTime, calculatedDuration, editScheduleStartTime,
      selectedAdPlacements, selectedOverlays, selectAllAdPlacements, selectAllOverlays,
      deleteOverlays, deleteAdPlacements,
    };
  },
};
</script>

<style scoped>
.nav-link {
    cursor: pointer;
}
</style>