import { defineStore } from 'pinia';

export const useMediaStore = defineStore('media', {
  state: () => ({
    items: [],
    currentFolderId: null,
    currentPath: [],
    selectedItems: [],
    allFolders: []
  }),

  actions: {
    async fetchFolderContents(folderId = null) {
      try {
        const response = await fetch(`${process.env.VUE_APP_API_BASE_URL || 'http://localhost:3001'}/api/media/folder/${folderId || ''}`);
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        this.items = await response.json();
        this.currentFolderId = folderId;
      } catch (error) {
        console.error('Error fetching folder contents:', error);
        throw error;
      }
    },

    async setCurrentFolder(folderId) {
      this.currentFolderId = folderId;
    },

    async createFolder(folderData) {
      try {
        const response = await fetch(`${process.env.VUE_APP_API_BASE_URL || 'http://localhost:3001'}/api/media/folder`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(folderData),
        });
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const newFolder = await response.json();
        // Add the new folder to the current items list
        this.items.push(newFolder);
        return newFolder;
      } catch (error) {
        console.error('Error creating folder:', error);
        throw error;
      }
    },

    async addFiles(fileData) {
      try {
        const response = await fetch(`${process.env.VUE_APP_API_BASE_URL || 'http://localhost:3001'}/api/media/files`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(fileData),
        });
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const newFiles = await response.json();
        // Add the new files to the current items list
        this.items.push(...newFiles);
        return newFiles;
      } catch (error) {
        console.error('Error adding files:', error);
        throw error;
      }
    },

    async renameItem(itemData) {
      const { id, name } = itemData;
      try {
        const response = await fetch(`${process.env.VUE_APP_API_BASE_URL || 'http://localhost:3001'}/api/media/${id}/rename`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ name }),
        });
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const updatedItem = await response.json();
        
        // Update the item in the current items list
        const index = this.items.findIndex(item => item.id === id);
        if (index !== -1) {
          this.items[index] = updatedItem;
        }
        
        // Update any references in other parts of the state if needed
        
        return updatedItem;
      } catch (error) {
        console.error('Error renaming item:', error);
        throw error;
      }
    },

    async moveItem(itemData) {
      const { id, parentId } = itemData;
      try {
        const response = await fetch(`${process.env.VUE_APP_API_BASE_URL || 'http://localhost:3001'}/api/media/${id}/move`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ parentId }),
        });
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const movedItem = await response.json();
        
        // Update the item in the current items list
        const index = this.items.findIndex(item => item.id === id);
        if (index !== -1) {
          this.items[index] = movedItem;
        }
        
        // Remove item from current list if it was moved to a different folder
        if (this.currentFolderId !== parentId) {
          this.items.splice(index, 1);
        }
        
        return movedItem;
      } catch (error) {
        console.error('Error moving item:', error);
        throw error;
      }
    },

    async deleteItem(itemId) {
      try {
        const response = await fetch(`${process.env.VUE_APP_API_BASE_URL || 'http://localhost:3001'}/api/media/${itemId}`, {
          method: 'DELETE',
        });
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        // Remove the item from the current items list
        this.items = this.items.filter(item => item.id !== itemId);
        
        // Also remove from selected items if it was selected
        this.selectedItems = this.selectedItems.filter(id => id !== itemId);
        
        return await response.json();
      } catch (error) {
        console.error('Error deleting item:', error);
        throw error;
      }
    },

    async searchItems(query) {
      if (!query) {
        // If no query, return to normal folder view
        await this.fetchFolderContents(this.currentFolderId);
        return;
      }
      
      try {
        const response = await fetch(`${process.env.VUE_APP_API_BASE_URL || 'http://localhost:3001'}/api/media/search/${encodeURIComponent(query)}`);
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        this.items = await response.json();
      } catch (error) {
        console.error('Error searching items:', error);
        throw error;
      }
    },

    async fetchAllFolders() {
      try {
        // Fetch all folders recursively
        const allFolders = await this.fetchFoldersRecursively();
        this.allFolders = allFolders;
        return allFolders;
      } catch (error) {
        console.error('Error fetching all folders:', error);
        throw error;
      }
    },

    async fetchFoldersRecursively(parentId = null, level = 0) {
      try {
        const response = await fetch(`${process.env.VUE_APP_API_BASE_URL || 'http://localhost:3001'}/api/media/folder/${parentId || ''}`);
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const items = await response.json();
        const folders = items.filter(item => item.type === 'folder');
        
        // Add level property for indentation purposes
        folders.forEach(folder => folder.level = level);
        
        // Recursively fetch subfolders
        for (const folder of folders) {
          const subfolders = await this.fetchFoldersRecursively(folder.id, level + 1);
          folders.push(...subfolders);
        }
        
        return folders;
      } catch (error) {
        console.error('Error fetching folders recursively:', error);
        throw error;
      }
    },

    async fetchFolderPath(folderId) {
      try {
        // This would require a backend endpoint that returns the path to a folder
        // For now, we'll implement a simple version that builds the path
        const path = await this.buildFolderPath(folderId);
        this.breadcrumb = path;
        return path;
      } catch (error) {
        console.error('Error fetching folder path:', error);
        throw error;
      }
    },

    async buildFolderPath(folderId) {
      if (!folderId) return [];
      
      try {
        // Fetch folder info
        const allFolders = this.allFolders.length > 0 ? this.allFolders : await this.fetchAllFolders();
        
        // Find the folder
        let currentFolder = allFolders.find(f => f.id === folderId);
        if (!currentFolder) {
          // If not in cache, fetch directly
          // This would require a specific API endpoint; for now we'll skip
          return [];
        }
        
        const path = [];
        while (currentFolder) {
          path.unshift({ id: currentFolder.id, name: currentFolder.name });
          
          // Find parent
          if (currentFolder.parentId) {
            currentFolder = allFolders.find(f => f.id === currentFolder.parentId);
          } else {
            break; // Reached root
          }
        }
        
        return path;
      } catch (error) {
        console.error('Error building folder path:', error);
        return [];
      }
    },

    async fetchCurrentPath(folderId) {
      if (!folderId) {
        this.currentPath = [];
        return;
      }
      
      try {
        // Fetch all folders if not already cached
        if (this.allFolders.length === 0) {
          await this.fetchAllFolders();
        }
        
        // Build the path to the current folder
        const path = await this.buildFolderPath(folderId);
        this.currentPath = path;
      } catch (error) {
        console.error('Error fetching current path:', error);
        this.currentPath = [];
      }
    }
  }
});