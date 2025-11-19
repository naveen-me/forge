import { defineStore } from 'pinia';
import { mediaService } from '../services/api';
import { initWebSocket } from '../services/websocket';

export const useMediaStore = defineStore('media', {
  state: () => ({
    items: [],
    currentFolderId: null,
    currentPath: [],
    selectedItems: [],
  }),

  getters: {
    folders: (state) => Array.isArray(state.items) ? state.items.filter(item => item.type === 'folder') : [],
    videos: (state) => Array.isArray(state.items) ? state.items.filter(item => item.type === 'file') : [],
    allItems: (state) => state.items || [],
  },

  actions: {
    init() {
      // Initialize WebSocket connection - no need to call this multiple times
      // since we have a singleton connection in websocket.js
      initWebSocket();
    },

    async fetchFolderContents(folderId = null) {
      try {
        const response = await mediaService.getFolderContents(folderId);
        // Handle API response structure - response.data contains the actual response object
        // response.data.data contains the actual array of items
        this.items = response.data.success ? response.data.data : response.data;
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
        await mediaService.deleteItems(this.selectedItems);
        this.selectedItems = [];
        await this.fetchFolderContents(this.currentFolderId);
    },

    async deleteItem(id) {
        await mediaService.deleteItem(id);
        await this.fetchFolderContents(this.currentFolderId);
    },

    async search(query) {
        if (!query) {
            await this.fetchFolderContents(this.currentFolderId);
            return;
        }
        try {
            const response = await mediaService.searchItems(query);
            this.items = response.data.success ? response.data.data : response.data;
        } catch (error) {
            console.error('Error searching items:', error);
        }
    },

    async fetchPath(folderId = null) {
        try {
            const response = await mediaService.getFolderPath(folderId);
            this.currentPath = response.data.success ? response.data.data : response.data;
        } catch (error) {
            console.error('Error fetching folder path:', error);
            this.currentPath = [];
        }
    },

    setCurrentFolderId(folderId) {
        this.currentFolderId = folderId;
        this.selectedItems = [];
        this.fetchFolderContents(folderId);
        this.fetchPath(folderId);
    },

    toggleItemSelected(itemId) {
        const index = this.selectedItems.indexOf(itemId);
        if (index > -1) {
            this.selectedItems.splice(index, 1);
        }
        else {
            this.selectedItems.push(itemId);
        }
    },

    clearSelection() {
        this.selectedItems = [];
    },

    selectAll(itemIds) {
        this.selectedItems = [...itemIds];
    },

    sortItems(field, direction) {
      this.items.sort((a, b) => {
        let valA = a[field];
        let valB = b[field];

        if (field === 'name') {
          valA = valA.toLowerCase();
          valB = valB.toLowerCase();
        }

        if (valA < valB) {
          return direction === 'asc' ? -1 : 1;
        }
        if (valA > valB) {
          return direction === 'asc' ? 1 : -1;
        }
        return 0;
      });
    },

    updateItem(item) {
      // The item might be the full item object from WebSocket or a response wrapped in data
      const actualItem = item.data ? item.data : item;
      const index = this.items.findIndex(i => i.id === actualItem.id);
      if (index !== -1) {
        // Create a new array to ensure reactivity in Vue
        const newItems = [...this.items];
        newItems[index] = { ...newItems[index], ...actualItem };
        this.items = newItems;
      } else {
        // If item is not in current view but matches current folder context, we might need to add it
        // Only add if it belongs to the current folder
        if (actualItem.parentId === this.currentFolderId) {
          this.items = [...this.items, actualItem];
        }
      }
    }
  },
});
