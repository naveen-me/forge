import apiClient from '../api';

const overlayService = {
  async getOverlays() {
    try {
      const response = await apiClient.get('/overlays');
      return response ? response : [];
    } catch (error) {
      console.error('Error fetching overlays:', error);
      return [];
    }
  },

  async getOverlayById(id) {
    try {
      const response = await apiClient.get(`/overlays/${id}`);
      return response;
    } catch (error) {
      console.error('Error fetching overlay by ID:', error);
      return null;
    }
  },

  async createOverlay(data) {
    try {
      const response = await apiClient.post('/overlays', data);
      return response;
    } catch (error) {
      console.error('Error creating overlay:', error);
      return null;
    }
  },

  async updateOverlay(id, data) {
    try {
      const response = await apiClient.put(`/overlays/${id}`, data);
      return response;
    } catch (error) {
      console.error('Error updating overlay:', error);
      return null;
    }
  },

  async deleteOverlay(id) {
    try {
      const response = await apiClient.delete(`/overlays/${id}`);
      return response ? (response.success !== false) : false;
    } catch (error) {
      console.error('Error deleting overlay:', error);
      return false;
    }
  },

  async getOverlayTypes() {
    try {
      const response = await apiClient.get('/overlays/types');
      return response ? response : [];
    } catch (error) {
      console.error('Error fetching overlay types:', error);
      return [];
    }
  }
};

export default overlayService;