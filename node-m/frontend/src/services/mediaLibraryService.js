import apiClient from '../api';

const mediaLibraryService = {
  async getMedia(params) {
    const response = await apiClient.get('/media', { params });
    return response && response.success ? response.data : null;
  },

  async getFolders(params) {
    const response = await apiClient.get('/folders', { params });
    return response && response.success ? response.data : null;
  },

  async createFolder(data) {
    const response = await apiClient.post('/folders', data);
    return response && response.success ? response.data : null;
  },

  async updateFolder(id, data) {
    const response = await apiClient.put(`/folders/${id}`, data);
    return response && response.success ? response.data : null;
  },

  async deleteFolder(id) {
    try {
      const response = await apiClient.delete(`/folders/${id}`);
      // For delete operations, a successful response means the operation succeeded
      return response ? (response.success !== false) : false;
    } catch (error) {
      console.error('Error deleting folder:', error);
      return false;
    }
  },

  async addMediaFiles(files, folderId) {
    console.log('Adding media files:', { files, folderId });
    const response = await apiClient.post('/media/add-files', { files, folderId });
    return response && response.success ? response.data : null;
  },

  async deleteMedia(id) {
    try {
      const response = await apiClient.delete(`/media/${id}`);
      // For delete operations, a successful response means the operation succeeded
      return response ? (response.success !== false) : false;
    } catch (error) {
      console.error('Error deleting media:', error);
      return false;
    }
  },

  async updateMedia(id, data) {
    const response = await apiClient.put(`/media/${id}`, data);
    return response && response.success ? response.data : null;
  },

  async selectFiles() {
    if (window.electronAPI && window.electronAPI['dialog:showOpenDialog']) {
      const result = await window.electronAPI['dialog:showOpenDialog']({
        properties: ['openFile', 'multiSelections'],
        filters: [
          { name: 'Media Files', extensions: ['mp4', 'avi', 'mov', 'jpg', 'jpeg', 'png'] },
        ],
      });
      if (result.canceled) {
        return [];
      }
      return result.filePaths;
    }
    console.error('File selection is not available in this environment.');
    alert('File selection is not available. This feature requires the application to be run in Electron.');
    return [];
  }
};

export default mediaLibraryService;