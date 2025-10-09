<template>
  <div class="ads-container p-4">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1 class="h3 mb-0 text-gray-800">Ads Management</h1>
      <div class="d-flex align-items-center">
        <button class="btn btn-primary me-2" @click="showCreateGroupModal = true">
          <i class='bx bx-plus'></i> New Group
        </button>
        <button class="btn btn-outline-secondary" @click="selectAdFiles">
          <i class='bx bx-upload'></i> Upload Ads
        </button>
      </div>
    </div>

    <!-- Ad Groups List -->
    <div class="card shadow-sm mb-4">
      <div class="card-body">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary" style="width: 3rem; height: 3rem;" role="status"></div>
        </div>
        <div v-else-if="error" class="alert alert-danger">{{ error }}</div>
        <div v-else>
          <div class="d-flex flex-wrap gap-3">
            <div 
              v-for="group in adGroups" 
              :key="group.id" 
              class="ad-group-card p-3 border rounded d-flex flex-column"
              :class="{ 'active': selectedGroupId === group.id }"
              @click="selectGroup(group.id)"
            >
              <div class="d-flex justify-content-between align-items-start mb-2">
                <h5 class="mb-0">{{ group.name }}</h5>
                <div class="btn-group">
                  <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown">
                    <i class='bx bx-dots-vertical-rounded'></i>
                  </button>
                  <ul class="dropdown-menu">
                    <li><a class="dropdown-item" href="#" @click.prevent="renameGroup(group)">Rename</a></li>
                    <li><a class="dropdown-item text-danger" href="#" @click.prevent="deleteGroup(group.id)">Delete</a></li>
                  </ul>
                </div>
              </div>
              <p class="text-muted mb-2">{{ group.adCount || group.ads?.length || 0 }} ads</p>
              <div class="mt-auto">
                <button class="btn btn-sm btn-outline-primary w-100" @click.stop="selectGroup(group.id)">
                  <i class='bx bx-play-circle'></i> View Ads
                </button>
              </div>
            </div>
            
            <!-- Unassigned Ads Card -->
            <div 
              class="ad-group-card p-3 border rounded d-flex flex-column"
              :class="{ 'active': selectedGroupId === null }"
              @click="selectGroup(null)"
            >
              <div class="d-flex justify-content-between align-items-start mb-2">
                <h5 class="mb-0">Unassigned Ads</h5>
              </div>
              <p class="text-muted mb-2">{{ unassignedAds.length }} ads</p>
              <div class="mt-auto">
                <button class="btn btn-sm btn-outline-primary w-100" @click.stop="selectGroup(null)">
                  <i class='bx bx-play-circle'></i> View Ads
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Ads List for selected group -->
    <div v-if="selectedGroupId !== undefined">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h3 class="h5 mb-0">
          {{ selectedGroup ? selectedGroup.name : 'Unassigned' }} Ads
          <span class="text-muted">({{ currentAds.length }})</span>
        </h3>
        <div class="d-flex">
          <input type="text" class="form-control me-2" placeholder="Search ads..." v-model="searchQuery">
          <div class="btn-group">
            <button class="btn" :class="viewType === 'grid' ? 'btn-secondary' : 'btn-outline-secondary'" @click="viewType = 'grid'">
              <i class='bx bxs-grid-alt'></i>
            </button>
            <button class="btn" :class="viewType === 'list' ? 'btn-secondary' : 'btn-outline-secondary'" @click="viewType = 'list'">
              <i class='bx bx-list-ul'></i>
            </button>
          </div>
        </div>
      </div>

      <div v-if="loadingAds" class="text-center py-5">
        <div class="spinner-border text-primary" style="width: 3rem; height: 3rem;" role="status"></div>
      </div>
      <div v-else-if="errorAds" class="alert alert-danger">{{ errorAds }}</div>
      <div v-else>
        <!-- Grid View -->
        <div v-if="viewType === 'grid'" class="row">
          <div 
            v-for="ad in currentAds" 
            :key="ad.id" 
            class="col-xl-2 col-lg-3 col-md-4 col-sm-6 mb-4"
          >
            <div class="card ad-card h-100">
              <div class="card-img-top-container">
                <img :src="ad.thumbnailPath || 'https://via.placeholder.com/200x112?text=Ad'" class="card-img-top" alt="Ad Thumbnail">
              </div>
              <div class="card-body">
                <h6 class="card-title text-truncate mb-1" :title="ad.displayName">{{ ad.displayName }}</h6>
                <p class="card-text text-muted small">{{ ad.type || 'Ad' }}</p>
              </div>
              <div class="ad-actions">
                <div class="btn-group">
                  <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown">
                    <i class='bx bx-dots-horizontal-rounded'></i>
                  </button>
                  <ul class="dropdown-menu">
                    <li><a class="dropdown-item" href="#" @click.prevent="renameAd(ad)">Rename</a></li>
                    <li><a class="dropdown-item" href="#" @click.prevent="regenerateThumbnail(ad.id)">Regenerate Thumbnail</a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item text-danger" href="#" @click.prevent="deleteAd(ad.id)">Delete</a></li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- List View -->
        <div v-else class="card shadow-sm">
          <ul class="list-group list-group-flush">
            <li 
              v-for="ad in currentAds" 
              :key="ad.id" 
              class="list-group-item d-flex align-items-center"
            >
              <img :src="ad.thumbnailPath || 'https://via.placeholder.com/50x50?text=Ad'" width="50" height="50" class="me-3 rounded">
              <div class="flex-grow-1">
                <div class="fw-bold">{{ ad.displayName }}</div>
                <small class="text-muted">{{ ad.type || 'Ad' }} • {{ ad.duration }}s</small>
              </div>
              <div>
                <div class="btn-group">
                  <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown">
                    Actions
                  </button>
                  <ul class="dropdown-menu">
                    <li><a class="dropdown-item" href="#" @click.prevent="renameAd(ad)">Rename</a></li>
                    <li><a class="dropdown-item" href="#" @click.prevent="regenerateThumbnail(ad.id)">Regenerate Thumbnail</a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item text-danger" href="#" @click.prevent="deleteAd(ad.id)">Delete</a></li>
                  </ul>
                </div>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Create Group Modal -->
    <div v-if="showCreateGroupModal" class="modal-backdrop" @click="showCreateGroupModal = false">
      <div class="modal fade show d-block" tabindex="-1" @click.stop>
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Create New Ad Group</h5>
              <button type="button" class="btn-close" @click="showCreateGroupModal = false"></button>
            </div>
            <div class="modal-body">
              <input type="text" class="form-control" v-model="newGroupName" placeholder="Enter group name" @keyup.enter="createGroup">
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="showCreateGroupModal = false">Cancel</button>
              <button type="button" class="btn btn-primary" @click="createGroup" :disabled="!newGroupName.trim()">Create</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import adService from '../../services/adService';

const adGroups = ref([]);
const allAds = ref([]);
const unassignedAds = ref([]);
const loading = ref(false);
const error = ref(null);
const loadingAds = ref(false);
const errorAds = ref(null);
const selectedGroupId = ref(null);
const viewType = ref('grid');
const searchQuery = ref('');
const showCreateGroupModal = ref(false);
const newGroupName = ref('');

// Computed properties
const selectedGroup = computed(() => {
  if (selectedGroupId.value === null) return null;
  return adGroups.value.find(g => g.id === selectedGroupId.value);
});

const currentAds = computed(() => {
  let ads = selectedGroupId.value === null ? unassignedAds.value : 
            allAds.value.filter(ad => ad.adGroupId === selectedGroupId.value);
  
  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    ads = ads.filter(ad => 
      ad.displayName.toLowerCase().includes(query) || 
      ad.filename.toLowerCase().includes(query)
    );
  }
  
  return ads;
});

// Methods
const refreshAdGroups = async () => {
  loading.value = true;
  error.value = null;
  try {
    adGroups.value = await adService.getAdGroups();
  } catch (err) {
    error.value = 'Failed to load ad groups';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const refreshAdsForCurrentGroup = async () => {
  loadingAds.value = true;
  errorAds.value = null;
  try {
    if (selectedGroupId.value === null) {
      // Load unassigned ads
      unassignedAds.value = await adService.getUnassignedAds();
    } else {
      // Load ads for the selected group
      allAds.value = await adService.getAdsByGroup(selectedGroupId.value);
    }
  } catch (err) {
    errorAds.value = 'Failed to load ads';
    console.error(err);
  } finally {
    loadingAds.value = false;
  }
};

const selectGroup = async (groupId) => {
  selectedGroupId.value = groupId;
  await refreshAdsForCurrentGroup();
};

const createGroup = async () => {
  if (!newGroupName.value.trim()) return;
  
  try {
    const newGroup = await adService.createAdGroup({
      name: newGroupName.value.trim()
    });
    if (newGroup) {
      showCreateGroupModal.value = false;
      newGroupName.value = '';
      await refreshAdGroups();
    } else {
      error.value = 'Failed to create ad group';
    }
  } catch (err) {
    error.value = 'Failed to create ad group';
    console.error(err);
  }
};

const renameGroup = async (group) => {
  const newName = prompt('Enter new group name:', group.name);
  if (newName && newName.trim() && newName.trim() !== group.name) {
    try {
      const updatedGroup = await adService.updateAdGroup(group.id, {
        name: newName.trim()
      });
      if (updatedGroup) {
        await refreshAdGroups();
      } else {
        error.value = 'Failed to rename group';
      }
    } catch (err) {
      error.value = 'Failed to rename group';
      console.error(err);
    }
  }
};

const deleteGroup = async (groupId) => {
  if (!confirm('Are you sure you want to delete this ad group and all its ads?')) return;
  
  try {
    const result = await adService.deleteAdGroup(groupId);
    if (result) {
      await refreshAdGroups();
      if (selectedGroupId.value === groupId) {
        selectedGroupId.value = null;
        await refreshAdsForCurrentGroup();
      }
    } else {
      error.value = 'Failed to delete ad group';
    }
  } catch (err) {
    error.value = 'Failed to delete ad group';
    console.error(err);
  }
};

const deleteAd = async (adId) => {
  if (!confirm('Are you sure you want to delete this ad?')) return;
  
  try {
    const result = await adService.deleteAd(adId);
    if (result) {
      await refreshAdsForCurrentGroup();
    } else {
      errorAds.value = 'Failed to delete ad';
    }
  } catch (err) {
    errorAds.value = 'Failed to delete ad';
    console.error(err);
  }
};

const renameAd = async (ad) => {
  const newName = prompt('Enter new display name:', ad.displayName);
  if (newName && newName.trim() && newName.trim() !== ad.displayName) {
    try {
      const updatedAd = await adService.updateAd(ad.id, {
        displayName: newName.trim()
      });
      if (updatedAd) {
        await refreshAdsForCurrentGroup();
      } else {
        errorAds.value = 'Failed to rename ad';
      }
    } catch (err) {
      errorAds.value = 'Failed to rename ad';
      console.error(err);
    }
  }
};

const regenerateThumbnail = async (adId) => {
  try {
    const result = await adService.regenerateAdThumbnail(adId);
    if (result) {
      await refreshAdsForCurrentGroup();
      console.log('Thumbnail regenerated successfully');
    } else {
      errorAds.value = 'Failed to regenerate thumbnail';
    }
  } catch (err) {
    errorAds.value = 'Failed to regenerate thumbnail';
    console.error(err);
  }
};

const selectAdFiles = async () => {
  try {
    const filePaths = await adService.selectFiles();
    if (filePaths && filePaths.length > 0) {
      // For now, add to the currently selected group or unassigned
      const adGroupId = selectedGroupId.value; // Could be null for unassigned
      
      // This would typically call a method to add ad files
      // For now, we'll use the media service to add files and then convert them to ads
      const result = await adService.addAdFiles(filePaths, adGroupId);
      if (result) {
        await refreshAdsForCurrentGroup();
      } else {
        errorAds.value = 'Failed to add ad files';
      }
    }
  } catch (err) {
    errorAds.value = 'Failed to add ad files';
    console.error(err);
  }
};

onMounted(async () => {
  await refreshAdGroups();
  // Load unassigned ads initially
  unassignedAds.value = await adService.getUnassignedAds();
});
</script>

<style scoped>
.ads-container {
  background-color: #f8f9fc;
  color: #5a5c69;
}

.ad-group-card {
  width: 250px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease-in-out;
}

.ad-group-card:hover {
  border-color: #4e73df;
  transform: translateY(-3px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

.ad-group-card.active {
  border-color: #4e73df;
  background-color: #eef2f7;
}

.ad-card {
  border: 1px solid #e3e6f0;
  box-shadow: 0 0.15rem 1.75rem 0 rgba(58, 59, 69, 0.15);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
  position: relative;
  overflow: hidden;
}

.ad-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 0.5rem 1.75rem 0 rgba(58, 59, 69, 0.25);
}

.card-img-top-container {
  height: 150px;
  background-color: #eaecf4;
}

.card-img-top {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ad-actions {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  opacity: 0;
  transition: opacity 0.2s ease-in-out;
}

.ad-card:hover .ad-actions {
  opacity: 1;
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
</style>