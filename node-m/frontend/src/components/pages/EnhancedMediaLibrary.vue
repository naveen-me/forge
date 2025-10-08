<template>
  <PageLayout>
    <!-- Top Bar -->
    <div class="d-flex justify-content-between align-items-center mb-2">
      <h1 class="mb-0">Enhanced Media Library</h1>
      <div>
        <button class="btn btn-primary me-2" @click="selectFiles">
          <i class="bi bi-plus-circle"></i> Add Files
        </button>
        <button class="btn btn-outline-success me-2" @click="createFolderInCurrent">
          <i class="bi bi-folder-plus"></i> New Folder
        </button>
        <div class="btn-group me-2" role="group">
          <button
            type="button"
            class="btn btn-outline-secondary dropdown-toggle"
            data-bs-toggle="dropdown"
            aria-expanded="false"
          >
            <i class="bi bi-tools"></i> Tools
          </button>
          <ul class="dropdown-menu">
            <li>
              <a class="dropdown-item" href="#" @click.prevent="validateMediaFiles">
                <i class="bi bi-check-circle me-2"></i> Validate Files
              </a>
            </li>
            <li>
              <a class="dropdown-item" href="#" @click.prevent="cleanupMissingFiles">
                <i class="bi bi-trash me-2"></i> Cleanup Missing Files
              </a>
            </li>
          </ul>
        </div>
        <button class="btn btn-outline-secondary" @click="fetchAllData">
          <i class="bi bi-arrow-clockwise"></i> Refresh
        </button>
      </div>
    </div>

    <!-- Breadcrumb Navigation -->
    <div class="row mb-3">
      <div class="col-12">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0">
            <li class="breadcrumb-item">
              <a href="#" @click.prevent="navigateToFolder(null)">Root</a>
            </li>
            <li v-for="folder in breadcrumb" :key="folder.id" class="breadcrumb-item">
              <a href="#" @click.prevent="navigateToFolder(folder.id)">{{ folder.name }}</a>
            </li>
            <li v-if="currentFolder" class="breadcrumb-item active" aria-current="page">
              {{ currentFolder.name }}
            </li>
          </ol>
        </nav>
      </div>
    </div>

    <!-- Search and Filter Bar -->
    <div class="row mb-4">
      <div class="col-md-6">
        <div class="input-group">
          <span class="input-group-text">
            <i class="bi bi-search"></i>
          </span>
          <input
            type="text"
            class="form-control"
            placeholder="Search media..."
            v-model="searchQuery"
            @input="filterMedia"
          >
        </div>
      </div>
      <div class="col-md-6">
        <div class="d-flex justify-content-end">
          <select class="form-select me-2" v-model="sortBy" @change="sortMedia">
            <option value="name">Sort by Name</option>
            <option value="type">Sort by Type</option>
            <option value="date">Sort by Date</option>
          </select>
          <button class="btn btn-outline-secondary me-2" @click="toggleSortDirection" title="Toggle sort direction">
            <i :class="sortDirection === 'asc' ? 'bi bi-sort-down' : 'bi bi-sort-up'"></i>
          </button>
          <select class="form-select" v-model="viewType">
            <option value="grid">Grid View</option>
            <option value="list">List View</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Main Content Area -->
    <div class="row">
      <div class="col-12">
        <!-- Loading Indicator -->
        <div v-if="loading" class="text-center">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
        </div>

        <!-- Error Message -->
        <div v-else-if="error" class="alert alert-danger" role="alert">
          {{ error }}
        </div>

        <!-- Media Content -->
        <div v-else>
          <!-- Grid View -->
          <div v-if="viewType === 'grid'" class="row">
            <!-- Folder and Media Cards -->
          </div>

          <!-- List View -->
          <div v-else class="table-responsive">
            <!-- Table for Folders and Media -->
          </div>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <MediaPlayerModal ref="mediaPlayerModalRef" />
  </PageLayout>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue';
import PageLayout from './PageLayout.vue';
import MediaPlayerModal from '../MediaPlayerModal.vue';
import * as apiClient from '../../services/apiClient.js';

export default {
  name: 'EnhancedMediaLibrary',
  components: {
    PageLayout,
    MediaPlayerModal
  },
  setup() {
    const mediaLibrary = ref([]);
    const folders = ref([]);
    const loading = ref(false);
    const error = ref(null);
    const mediaPlayerModalRef = ref(null);

    // UI State
    const viewType = ref('grid');
    const searchQuery = ref('');
    const sortBy = ref('date');
    const sortDirection = ref('desc');
    const currentFolder = ref(null);
    const breadcrumb = ref([]);
    
    const editingId = ref(null);
    const editName = ref('');
    const editInput = ref(null);

    const fetchAllData = async () => {
      loading.value = true;
      error.value = null;
      try {
        // Load folders first
        const foldersRes = await apiClient.getFolders();
        folders.value = foldersRes.data;

        // Then load and validate media
        const mediaRes = await apiClient.getMedia();
        const mediaItems = mediaRes.data || mediaRes;
        let validatedItems = [];
        
        if (Array.isArray(mediaItems)) {
          validatedItems = mediaItems;
        } else if (mediaItems && Array.isArray(mediaItems.data)) {
          validatedItems = mediaItems.data;
        } else {
          validatedItems = [];
        }

        // Validate file existence
        try {
          const validationResponse = await apiClient.validateMediaFiles();
          const validatedData = validationResponse.data || validationResponse;
          if (Array.isArray(validatedData)) {
            mediaLibrary.value = validatedData;
          } else {
            validatedItems.forEach(item => item.exists = true);
            mediaLibrary.value = validatedItems;
          }
        } catch (validationErr) {
          console.warn('Media file validation failed, assuming all files exist:', validationErr);
          validatedItems.forEach(item => item.exists = true);
          mediaLibrary.value = validatedItems;
        }
      } catch (err) {
        error.value = 'Failed to load data from the server.';
        console.error(err);
      } finally {
        loading.value = false;
      }
    };

    const deleteMedia = async (mediaId) => {
      try {
        await apiClient.deleteMedia(mediaId);
        await fetchAllData(); // Refresh data
      } catch (err) {
        error.value = 'Failed to delete media item.';
        console.error(err);
      }
    };
    
    const saveRename = async (media) => {
      const newName = editName.value.trim();
      if (newName && newName !== media.displayName) {
        try {
          await apiClient.updateMediaDisplayName(media.id, newName);
          await fetchAllData();
        } catch (err) {
          error.value = `Failed to rename: ${err.message}`;
        }
      }
      editingId.value = null;
    };

    const createFolder = async (name, parentId) => {
        try {
            await apiClient.createFolder(name, parentId);
            await fetchAllData();
        } catch(err) {
            error.value = 'Failed to create folder.';
            console.error(err);
        }
    };

    const deleteFolder = async (folderId) => {
        try {
            await apiClient.deleteFolder(folderId);
            await fetchAllData();
        } catch(err) {
            error.value = 'Failed to delete folder.';
            console.error(err);
        }
    };
    
    const saveFolderRename = async (folder) => {
      const newName = editName.value.trim();
      if (newName && newName !== folder.name) {
        try {
          await apiClient.renameFolder(folder.id, newName);
          await fetchAllData();
        } catch (err) {
          error.value = `Failed to rename folder: ${err.message}`;
        }
      }
      editingId.value = null;
    };

    // Missing methods needed by the template
    const createFolderInCurrent = async () => {
      const folderName = prompt('Enter folder name:');
      if (folderName && folderName.trim()) {
        try {
          const parentId = currentFolder.value ? currentFolder.value.id : null;
          await apiClient.createFolder(folderName.trim(), parentId);
          await fetchAllData();
        } catch (err) {
          error.value = 'Failed to create folder: ' + (err.message || 'Unknown error');
          console.error('Error creating folder:', err);
        }
      }
    };

    const navigateToFolder = async (folderId) => {
      try {
        // Update current folder and breadcrumb as needed
        currentFolder.value = folderId ? folders.value.find(f => f.id === folderId) : null;
        
        // Update breadcrumb - this would need proper implementation
        // For now, we'll just refresh data
        await fetchAllData();
      } catch (err) {
        error.value = 'Failed to navigate to folder: ' + (err.message || 'Unknown error');
        console.error('Error navigating to folder:', err);
      }
    };

    const validateMediaFiles = async () => {
      try {
        await apiClient.validateMediaFiles();
        await fetchAllData();
        // Optionally show success message
      } catch (err) {
        error.value = 'Failed to validate media files: ' + (err.message || 'Unknown error');
        console.error('Error validating media files:', err);
      }
    };

    const cleanupMissingFiles = async () => {
      // This would be implemented as an API endpoint to clean up missing files
      // For now, it's a placeholder
      error.value = 'Cleanup missing files functionality not implemented yet';
    };

    // Search and filter functionality
    const filterMedia = () => {
      // This would implement client-side filtering
      // For now, we'll just log the search query
      console.log('Searching for:', searchQuery.value);
    };

    // Sorting functionality
    const sortMedia = () => {
      // This would implement client-side sorting
      console.log('Sorting by:', sortBy.value, 'direction:', sortDirection.value);
    };

    const toggleSortDirection = () => {
      sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc';
      sortMedia(); // Re-sort after direction change
    };

    // NOTE: selectFiles still uses electronAPI because file dialogs cannot be opened from a web context.
    // This is an architectural issue to be addressed later. The data handling part is updated.
    const selectFiles = async () => {
      try {
        if (window.electronAPI && window.electronAPI.showOpenDialog) {
          const result = await window.electronAPI.showOpenDialog({
            properties: ['openFile', 'multiSelections'],
          });

          if (!result.canceled && result.filePaths.length > 0) {
            loading.value = true;
            const folderId = currentFolder.value ? currentFolder.value.id : null;
            await apiClient.addMediaFiles(result.filePaths, folderId);
            await fetchAllData();
          }
        } else {
          error.value = 'File selection not available in this environment';
        }
      } catch (err) {
        error.value = err.message;
      } finally {
        loading.value = false;
      }
    };

    onMounted(fetchAllData);

    // Return all reactive properties and methods
    return {
      mediaLibrary,
      folders,
      loading,
      error,
      viewType,
      searchQuery,
      sortBy,
      sortDirection,
      currentFolder,
      breadcrumb,
      editingId,
      editName,
      editInput,
      fetchAllData,
      deleteMedia,
      saveRename,
      createFolder,
      deleteFolder,
      saveFolderRename,
      createFolderInCurrent,
      navigateToFolder,
      validateMediaFiles,
      cleanupMissingFiles,
      filterMedia,
      sortMedia,
      toggleSortDirection,
      selectFiles,
      mediaPlayerModalRef,
    };
  }
};
</script>

<style scoped>
/* Styles remain the same */
.breadcrumb {
  font-size: 0.9rem;
}
.card-img-top {
  object-fit: cover;
}
</style>
