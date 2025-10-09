import apiClient from '../api';

const adService = {
  // Ad group methods
  async getAdGroups() {
    try {
      const response = await apiClient.get('/ads/ad-groups');
      return response ? response : [];
    } catch (error) {
      console.error('Error fetching ad groups:', error);
      return [];
    }
  },

  async createAdGroup(data) {
    try {
      const response = await apiClient.post('/ads/ad-groups', data);
      return response;
    } catch (error) {
      console.error('Error creating ad group:', error);
      return null;
    }
  },

  async updateAdGroup(id, data) {
    try {
      const response = await apiClient.put(`/ads/ad-groups/${id}`, data);
      return response;
    } catch (error) {
      console.error('Error updating ad group:', error);
      return null;
    }
  },

  async deleteAdGroup(id) {
    try {
      const response = await apiClient.delete(`/ads/ad-groups/${id}`);
      return response ? (response.success !== false) : false;
    } catch (error) {
      console.error('Error deleting ad group:', error);
      return false;
    }
  },

  // Ad methods
  async getAds() {
    try {
      const response = await apiClient.get('/ads');
      return response ? response : [];
    } catch (error) {
      console.error('Error fetching ads:', error);
      return [];
    }
  },

  async getAdById(id) {
    try {
      const response = await apiClient.get(`/ads/${id}`);
      return response;
    } catch (error) {
      console.error('Error fetching ad by ID:', error);
      return null;
    }
  },

  async createAd(data) {
    try {
      const response = await apiClient.post('/ads', data);
      return response;
    } catch (error) {
      console.error('Error creating ad:', error);
      return null;
    }
  },

  async updateAd(id, data) {
    try {
      const response = await apiClient.put(`/ads/${id}`, data);
      return response;
    } catch (error) {
      console.error('Error updating ad:', error);
      return null;
    }
  },

  async deleteAd(id) {
    try {
      const response = await apiClient.delete(`/ads/${id}`);
      return response ? (response.success !== false) : false;
    } catch (error) {
      console.error('Error deleting ad:', error);
      return false;
    }
  },

  async getAdsByGroup(groupId) {
    try {
      const response = await apiClient.get(`/ads/group/${groupId}`);
      return response ? response : [];
    } catch (error) {
      console.error('Error fetching ads by group:', error);
      return [];
    }
  },

  async getUnassignedAds() {
    try {
      const response = await apiClient.get('/ads/unassigned');
      return response ? response : [];
    } catch (error) {
      console.error('Error fetching unassigned ads:', error);
      return [];
    }
  },

  async moveAdToGroup(adId, groupId) {
    try {
      const response = await apiClient.put(`/ads/${adId}/group/${groupId}`);
      return response;
    } catch (error) {
      console.error('Error moving ad to group:', error);
      return null;
    }
  },

  async updateAdSortOrder(adId, sortOrder) {
    try {
      const response = await apiClient.put(`/ads/${adId}/sort-order`, { sortOrder });
      return response;
    } catch (error) {
      console.error('Error updating ad sort order:', error);
      return null;
    }
  },

  async regenerateAdThumbnail(adId) {
    try {
      const response = await apiClient.put(`/media/${adId}/regenerate-thumbnail`);
      return response;
    } catch (error) {
      console.error('Error regenerating ad thumbnail:', error);
      return null;
    }
  },

  async selectFiles() {
    if (window.electronAPI && window.electronAPI['dialog:showOpenDialog']) {
      const result = await window.electronAPI['dialog:showOpenDialog']({
        properties: ['openFile', 'multiSelections'],
        filters: [
          { name: 'Media Files', extensions: ['mp4', 'avi', 'mov', 'jpg', 'jpeg', 'png', 'mp3', 'wav'] },
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

export default adService;