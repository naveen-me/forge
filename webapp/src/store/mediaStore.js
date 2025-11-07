import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import api from '../services/api';

export const useMediaStore = defineStore('media', () => {
    const currentFolderId = ref(null);
    const mediaItems = ref([]);
    const selectedItems = ref([]);
    const isLoading = ref(false);
    const error = ref(null);

    const currentPath = computed(() => {
        // TODO: Implement logic to build the current path based on currentFolderId
        return 'Media Library';
    });

    async function fetchMediaItems(parentId = null) {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await api.get('/media', { params: { parentId } });
            if (response.data.success) {
                mediaItems.value = response.data.data;
                currentFolderId.value = parentId;
            } else {
                error.value = response.data.message || 'Failed to fetch media items.';
            }
        } catch (err) {
            console.error('Error fetching media items:', err);
            error.value = 'Failed to fetch media items.';
        } finally {
            isLoading.value = false;
        }
    }

    async function createFolder(name, parentId = null) {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await api.post('/media/folder', { name, parentId });
            if (response.data.success) {
                await fetchMediaItems(parentId); // Refresh current view
                return true;
            } else {
                error.value = response.data.message || 'Failed to create folder.';
                return false;
            }
        } catch (err) {
            console.error('Error creating folder:', err);
            error.value = 'Failed to create folder.';
            return false;
        } finally {
            isLoading.value = false;
        }
    }

    async function addFiles(files, parentId = null) {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await api.post('/media/files', { files, parentId });
            if (response.data.success) {
                await fetchMediaItems(parentId); // Refresh current view
                return true;
            } else {
                error.value = response.data.message || 'Failed to add files.';
                return false;
            }
        } catch (err) {
            console.error('Error adding files:', err);
            error.value = 'Failed to add files.';
            return false;
        } finally {
            isLoading.value = false;
        }
    }

    async function renameMediaItem(id, newName) {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await api.put(`/media/${id}/rename`, { newName });
            if (response.data.success) {
                await fetchMediaItems(currentFolderId.value); // Refresh current view
                return true;
            } else {
                error.value = response.data.message || 'Failed to rename item.';
                return false;
            }
        } catch (err) {
            console.error('Error renaming media item:', err);
            error.value = 'Failed to rename item.';
            return false;
        } finally {
            isLoading.value = false;
        }
    }

    async function deleteMediaItems(ids) {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await api.post('/media/delete', { ids });
            if (response.data.success) {
                selectedItems.value = []; // Clear selection
                await fetchMediaItems(currentFolderId.value); // Refresh current view
                return true;
            } else {
                error.value = response.data.message || 'Failed to delete items.';
                return false;
            }
        } catch (err) {
            console.error('Error deleting media items:', err);
            error.value = 'Failed to delete items.';
            return false;
        } finally {
            isLoading.value = false;
        }
    }

    async function moveMediaItems(ids, newParentId) {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await api.post('/media/move', { ids, newParentId });
            if (response.data.success) {
                selectedItems.value = []; // Clear selection
                await fetchMediaItems(currentFolderId.value); // Refresh current view
                return true;
            } else {
                error.value = response.data.message || 'Failed to move items.';
                return false;
            }
        } catch (err) {
            console.error('Error moving media items:', err);
            error.value = 'Failed to move items.';
            return false;
        } finally {
            isLoading.value = false;
        }
    }

    async function copyMediaItems(ids, newParentId) {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await api.post('/media/copy', { ids, newParentId });
            if (response.data.success) {
                selectedItems.value = []; // Clear selection
                await fetchMediaItems(currentFolderId.value); // Refresh current view
                return true;
            } else {
                error.value = response.data.message || 'Failed to copy items.';
                return false;
            }
        } catch (err) {
            console.error('Error copying media items:', err);
            error.value = 'Failed to copy items.';
            return false;
        } finally {
            isLoading.value = false;
        }
    }

    return {
        currentFolderId,
        mediaItems,
        selectedItems,
        isLoading,
        error,
        currentPath,
        fetchMediaItems,
        createFolder,
        addFiles,
        renameMediaItem,
        deleteMediaItems,
        moveMediaItems,
        copyMediaItems,
    };
});
