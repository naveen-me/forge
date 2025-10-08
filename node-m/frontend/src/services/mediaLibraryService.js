import apiClient from '../api';

const mediaLibraryService = {
  async getMedia(params) {
    const response = await apiClient.get('/media', { params });
    return response.data;
  },

  async getFolders(params) {
    const response = await apiClient.get('/folders', { params });
    return response.data;
  },

  async createFolder(data) {
    const response = await apiClient.post('/folders', data);
    return response.data;
  },

  async updateFolder(id, data) {
    const response = await apiClient.put(`/folders/${id}`, data);
    return response.data;
  },

  async deleteFolder(id) {
    await apiClient.delete(`/folders/${id}`);
  },

  async addMediaFiles(files, folderId) {
    const response = await apiClient.post('/media/add-files', { files, folderId });
    return response.data;
  },

  async deleteMedia(id) {
    await apiClient.delete(`/media/${id}`);
  },

  async updateMedia(id, data) {
    const response = await apiClient.put(`/media/${id}`, data);
    return response.data;
  },

  async selectFiles() {
    if (window.electronAPI && window.electronAPI.dialogs && window.electronAPI.dialogs.showOpenDialog) {
      const result = await window.electronAPI.dialogs.showOpenDialog({
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