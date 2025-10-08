<template>
  <div class="media-library-container p-4">
    <!-- MediaPlayerModal -->
    <MediaPlayerModal ref="mediaPlayerModalRef" />

    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1 class="h3 mb-0 text-gray-800">Media Library</h1>
      <div class="d-flex align-items-center">
        <button class="btn btn-primary btn-icon-split me-2" @click="handleSelectFiles">
          <span class="icon text-white-50"><i class='bx bx-plus'></i></span>
          <span class="text">Add Files</span>
        </button>
        <button class="btn btn-outline-secondary btn-icon-split" @click="showCreateFolderModal = true">
          <span class="icon text-gray-600"><i class='bx bx-folder-plus'></i></span>
          <span class="text">New Folder</span>
        </button>
      </div>
    </div>

    <!-- Controls -->
    <div class="card shadow-sm mb-4">
      <div class="card-body d-flex justify-content-between align-items-center">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 bg-transparent p-0">
            <li class="breadcrumb-item"><a href="#" @click.prevent="navigateToFolder(null)">Root</a></li>
            <li v-for="folder in breadcrumb" :key="folder.id" class="breadcrumb-item">
              <a href="#" @click.prevent="navigateToFolder(folder.id)">{{ folder.name }}</a>
            </li>
          </ol>
        </nav>
        <div class="d-flex align-items-center">
          <div class="input-group me-2">
            <span class="input-group-text"><i class='bx bx-search'></i></span>
            <input type="text" class="form-control" placeholder="Search..." v-model="searchQuery">
          </div>
          <div class="btn-group">
            <button class="btn" :class="viewType === 'grid' ? 'btn-secondary' : 'btn-outline-secondary'" @click="viewType = 'grid'"><i class='bx bxs-grid-alt'></i></button>
            <button class="btn" :class="viewType === 'list' ? 'btn-secondary' : 'btn-outline-secondary'" @click="viewType = 'list'"><i class='bx bx-list-ul'></i></button>
          </div>
        </div>
      </div>
    </div>

    <!-- Content Area -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" style="width: 3rem; height: 3rem;" role="status"></div>
    </div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>
    <div v-else-if="!folders.length && !media.length" class="text-center py-5 text-gray-500">
      <i class='bx bx-info-circle' style="font-size: 4rem;"></i>
      <h4 class="mt-3">This folder is empty</h4>
      <p>Add files or create a new folder to get started.</p>
    </div>

    <!-- Grid View -->
    <div v-else-if="viewType === 'grid'" class="row">
      <!-- Folders -->
      <div v-for="folder in folders" :key="`folder-${folder.id}`" class="col-xl-2 col-lg-3 col-md-4 col-sm-6 mb-4">
        <div class="card item-card h-100" @dblclick="navigateToFolder(folder.id)">
          <div class="card-body text-center d-flex flex-column justify-content-center align-items-center">
            <i class='bx bxs-folder-open' style="font-size: 4rem; color: #4e73df;"></i>
            <h6 class="card-title mt-3 mb-0 text-truncate w-100" :title="folder.name">{{ folder.name }}</h6>
          </div>
          <div class="item-actions">
            <button class="btn btn-sm btn-light" @click.stop="handleRenameFolder(folder)"><i class='bx bx-pencil'></i></button>
            <button class="btn btn-sm btn-light" @click.stop="handleDeleteFolder(folder.id)"><i class='bx bx-trash'></i></button>
          </div>
        </div>
      </div>
      <!-- Media -->
      <div v-for="item in media" :key="item.id" class="col-xl-2 col-lg-3 col-md-4 col-sm-6 mb-4">
        <div class="card item-card h-100">
          <div class="card-img-top-container">
            <img :src="item.thumbnailPath || 'https://via.placeholder.com/200'" class="card-img-top" alt="Thumbnail">
            <div class="media-overlay" @click="playMedia(item)">
              <i class='bx bx-play-circle'></i>
            </div>
          </div>
          <div class="card-body">
            <h6 class="card-title text-truncate mb-1" :title="item.displayName">{{ item.displayName }}</h6>
            <p class="card-text text-muted small">{{ item.type }}</p>
          </div>
          <div class="item-actions">
             <button class="btn btn-sm btn-light" @click.stop="handleRenameMedia(item)"><i class='bx bx-pencil'></i></button>
            <button class="btn btn-sm btn-light" @click.stop="handleDeleteMedia(item.id)"><i class='bx bx-trash'></i></button>
          </div>
        </div>
      </div>
    </div>

    <!-- List View -->
    <div v-else-if="viewType === 'list'" class="card shadow-sm">
      <ul class="list-group list-group-flush">
        <!-- Folders -->
        <li v-for="folder in folders" :key="`folder-${folder.id}`" class="list-group-item list-group-item-action d-flex align-items-center" @dblclick="navigateToFolder(folder.id)">
          <i class='bx bxs-folder-open me-3' style="font-size: 1.5rem; color: #4e73df;"></i>
          <span class="fw-bold flex-grow-1">{{ folder.name }}</span>
          <div>
            <button class="btn btn-sm btn-outline-secondary me-2" @click.stop="handleRenameFolder(folder)">Rename</button>
            <button class="btn btn-sm btn-outline-danger" @click.stop="handleDeleteFolder(folder.id)">Delete</button>
          </div>
        </li>
        <!-- Media -->
        <li v-for="item in media" :key="item.id" class="list-group-item list-group-item-action d-flex align-items-center">
          <img :src="item.thumbnailPath || 'https://via.placeholder.com/50'" width="50" height="50" class="me-3 rounded">
          <div class="flex-grow-1">
            <div class="fw-bold">{{ item.displayName }}</div>
            <small class="text-muted">{{ item.type }}</small>
          </div>
          <div>
            <button class="btn btn-sm btn-outline-primary me-2" @click="playMedia(item)">Play</button>
            <button class="btn btn-sm btn-outline-secondary me-2" @click.stop="handleRenameMedia(item)">Rename</button>
            <button class="btn btn-sm btn-outline-danger" @click.stop="handleDeleteMedia(item.id)">Delete</button>
          </div>
        </li>
      </ul>
    </div>

    <!-- Pagination -->
    <nav v-if="totalPages > 1" class="mt-4 d-flex justify-content-center">
      <ul class="pagination">
        <li class="page-item" :class="{ disabled: currentPage === 1 }">
          <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">&laquo;</a>
        </li>
        <li v-for="page in totalPages" :key="page" class="page-item" :class="{ active: page === currentPage }">
          <a class="page-link" href="#" @click.prevent="changePage(page)">{{ page }}</a>
        </li>
        <li class="page-item" :class="{ disabled: currentPage === totalPages }">
          <a class="page-link" href="#" @click.prevent="changePage(currentPage + 1)">&raquo;</a>
        </li>
      </ul>
    </nav>

    <!-- Create Folder Modal -->
    <div v-if="showCreateFolderModal" class="modal-backdrop">
      <div class="modal fade show" style="display: block;" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Create New Folder</h5>
              <button type="button" class="btn-close" @click="showCreateFolderModal = false"></button>
            </div>
            <div class="modal-body">
              <input type="text" class="form-control" v-model="newFolderName" placeholder="Enter folder name" @keyup.enter="handleCreateFolder">
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="showCreateFolderModal = false">Cancel</button>
              <button type="button" class="btn btn-primary" @click="handleCreateFolder" :disabled="!newFolderName.trim()">Create</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import mediaLibraryService from '../../services/mediaLibraryService';
import { debounce } from 'lodash';
import MediaPlayerModal from '../MediaPlayerModal.vue';

const mediaPlayerModalRef = ref(null);
const media = ref([]);
const folders = ref([]);
const loading = ref(false);
const error = ref(null);
const viewType = ref('grid');
const searchQuery = ref('');
const currentPage = ref(1);
const totalPages = ref(1);
const currentFolderId = ref(null);
const breadcrumb = ref([]);
const showCreateFolderModal = ref(false);
const newFolderName = ref('');

const loadMedia = async () => {
  loading.value = true;
  error.value = null;
  try {
    const params = {
      page: currentPage.value,
      limit: 12,
      search: searchQuery.value,
      folderId: currentFolderId.value || ''
    };
    const data = await mediaLibraryService.getMedia(params);
    media.value = data.media;
    totalPages.value = data.totalPages;
  } catch (err) {
    error.value = 'Failed to load media. Please ensure the backend is running and accessible.';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const loadFolders = async () => {
  try {
    const params = { parentId: currentFolderId.value || '' };
    const allFolders = await mediaLibraryService.getFolders();
    folders.value = allFolders.filter(f => f.parentId === currentFolderId.value);
  } catch (err) {
    error.value = 'Failed to load folders.';
    console.error(err);
  }
};

const refresh = () => {
  loadMedia();
  loadFolders();
};

const navigateToFolder = async (folderId) => {
  currentFolderId.value = folderId;
  currentPage.value = 1;
  await updateBreadcrumb(folderId);
  refresh();
};

const updateBreadcrumb = async (folderId) => {
    if (folderId === null) {
        breadcrumb.value = [];
        return;
    }

    // This is a simplified approach. For a deep hierarchy, a recursive fetch would be better.
    // For now, we'll just rebuild based on the current folders list.
    const allFolders = await mediaLibraryService.getFolders();
    const newBreadcrumb = [];
    let currentId = folderId;
    while (currentId) {
        const folder = allFolders.find(f => f.id === currentId);
        if (folder) {
            newBreadcrumb.unshift(folder);
            currentId = folder.parentId;
        } else {
            break;
        }
    }
    breadcrumb.value = newBreadcrumb;
};


const handleSelectFiles = async () => {
  try {
    const filePaths = await mediaLibraryService.selectFiles();
    if (filePaths && filePaths.length > 0) {
      await mediaLibraryService.addMediaFiles(filePaths, currentFolderId.value);
      refresh();
    }
  } catch (err) {
    error.value = 'Failed to add files.';
    console.error(err);
  }
};

const handleCreateFolder = async () => {
  if (!newFolderName.value.trim()) return;
  try {
    await mediaLibraryService.createFolder({
      name: newFolderName.value,
      parentId: currentFolderId.value,
    });
    showCreateFolderModal.value = false;
    newFolderName.value = '';
    loadFolders();
  } catch (err) {
    error.value = 'Failed to create folder.';
    console.error(err);
  }
};

const handleDeleteFolder = async (folderId) => {
  if (!confirm('Are you sure you want to delete this folder? Its contents will be moved to the parent folder.')) return;
  try {
    await mediaLibraryService.deleteFolder(folderId);
    refresh();
  } catch (err) {
    error.value = 'Failed to delete folder.';
    console.error(err);
  }
};

const handleRenameFolder = async (folder) => {
  const newName = prompt('Enter new folder name:', folder.name);
  if (newName && newName.trim() && newName.trim() !== folder.name) {
    try {
      await mediaLibraryService.updateFolder(folder.id, { name: newName.trim() });
      loadFolders();
    } catch (err) {
      error.value = 'Failed to rename folder.';
      console.error(err);
    }
  }
};

const handleRenameMedia = async (item) => {
  const newName = prompt('Enter new display name:', item.displayName);
  if (newName && newName.trim() && newName.trim() !== item.displayName) {
    try {
      await mediaLibraryService.updateMedia(item.id, { displayName: newName.trim() });
      loadMedia();
    } catch (err) {
      error.value = 'Failed to rename media item.';
      console.error(err);
    }
  }
};


const handleDeleteMedia = async (mediaId) => {
  if (!confirm('Are you sure you want to delete this media?')) return;
  try {
    await mediaLibraryService.deleteMedia(mediaId);
    loadMedia();
  } catch (err) {
    error.value = 'Failed to delete media.';
    console.error(err);
  }
};

const playMedia = (item) => {
  if (mediaPlayerModalRef.value) {
    mediaPlayerModalRef.value.show(item);
  }
};

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  loadMedia();
};

const debouncedSearch = debounce(() => {
  currentPage.value = 1;
  loadMedia();
}, 300);

watch(searchQuery, debouncedSearch);

onMounted(refresh);
</script>

<style scoped>
/* Base styles */
.media-library-container {
  background-color: #f8f9fc;
  color: #5a5c69;
}
.btn {
  border-radius: 0.35rem;
  font-weight: 600;
}
.btn-primary {
  background-color: #4e73df;
  border-color: #4e73df;
}
.btn-icon-split {
  display: inline-flex;
  align-items: center;
  padding: 0;
}
.btn-icon-split .icon {
  padding: 0.375rem 0.75rem;
  background-color: rgba(0,0,0,0.1);
}
.btn-icon-split .text {
  padding: 0.375rem 0.75rem;
}

/* Card styles */
.item-card {
  border: 1px solid #e3e6f0;
  box-shadow: 0 0.15rem 1.75rem 0 rgba(58, 59, 69, 0.15);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
  position: relative;
  overflow: hidden;
}
.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 0.5rem 1.75rem 0 rgba(58, 59, 69, 0.25);
}
.item-card .card-img-top-container {
  position: relative;
  height: 150px;
  background-color: #eaecf4;
}
.item-card .card-img-top {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.item-card .card-body {
  padding: 1rem;
}
.item-card .card-title {
  font-weight: 700;
}

/* Item actions */
.item-actions {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  display: flex;
  gap: 0.25rem;
  opacity: 0;
  transition: opacity 0.2s ease-in-out;
}
.item-card:hover .item-actions {
  opacity: 1;
}

/* Media overlay */
.media-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-size: 3rem;
  opacity: 0;
  transition: opacity 0.2s ease-in-out;
  cursor: pointer;
}
.item-card:hover .media-overlay {
  opacity: 1;
}

/* Modal styles */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0,0,0,0.5);
  z-index: 1050;
}
.modal {
  z-index: 1055;
}

/* Breadcrumb */
.breadcrumb {
  font-size: 0.9rem;
}
.breadcrumb-item a {
  color: #4e73df;
  text-decoration: none;
  font-weight: 600;
}
.breadcrumb-item.active {
  color: #858796;
}
</style>