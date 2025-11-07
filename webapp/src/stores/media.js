import { defineStore } from 'pinia';
import { mediaService } from '../services/api';

export const useMediaStore = defineStore('media', {
  state: () => ({
    items: [],
    currentFolderId: null,
    currentPath: [],
    selectedItems: [],
  }),

  getters: {
    folders: (state) => state.items.filter(item => item.type === 'folder'),
    videos: (state) => state.items.filter(item => item.type === 'file'),
    allItems: (state) => state.items,
  },

  actions: {
    async fetchFolderContents(folderId = null) {
      try {
        const response = await mediaService.getFolderContents(folderId);
        this.items = response.data;
        this.currentFolderId = folderId;
        // We will need a separate action to fetch the breadcrumb path
      } catch (error) {
        console.error('Error fetching folder contents:', error);
        // Here you might want to set an error state
      }
    },

    async createFolder(name, parentId = null) {
        await mediaService.createFolder(name, parentId);
        await this.fetchFolderContents(parentId);
    },

    async addFiles(files, parentId = null) {
        await mediaService.addFiles(files, parentId);
        await this.fetchFolderContents(parentId);
    },
    
    async renameItem(id, name) {
        await mediaService.renameItem(id, name);
        await this.fetchFolderContents(this.currentFolderId);
    },

    async moveItems(itemIds, destinationFolderId) {
        for (const id of itemIds) {
            await mediaService.moveItem(id, destinationFolderId);
        }
        this.selectedItems = [];
        await this.fetchFolderContents(this.currentFolderId);
    },

    async deleteSelectedItems() {
        for (const id of this.selectedItems) {
            await mediaService.deleteItem(id);
        }
        this.selectedItems = [];
        await this.fetchFolderContents(this.currentFolderId);
    },

    async search(query) {
        if (!query) {
            await this.fetchFolderContents(this.currentFolderId);
            return;
        }
        try {
            const response = await mediaService.searchItems(query);
            this.items = response.data;
        } catch (error) {
            console.error('Error searching items:', error);
        }
    },

    setCurrentFolderId(folderId) {
        this.currentFolderId = folderId;
        this.selectedItems = [];
        this.fetchFolderContents(folderId);
    },

    toggleItemSelected(itemId) {
        const index = this.selectedItems.indexOf(itemId);
        if (index > -1) {
            this.selectedItems.splice(index, 1);
        } else {
            this.selectedItems.push(itemId);
        }
    },

    clearSelection() {
        this.selectedItems = [];
    },

    selectAll(itemIds) {
        this.selectedItems = [...itemIds];
    }
  },
});
