import apiClient from '../api';

const systemDefaultsService = {
  async getAllSystemDefaults() {
    try {
      const response = await apiClient.get('/system-defaults');
      return response ? response : [];
    } catch (error) {
      console.error('Error fetching system defaults:', error);
      return [];
    }
  },

  async getSystemDefaultById(id) {
    try {
      const response = await apiClient.get(`/system-defaults/${id}`);
      return response;
    } catch (error) {
      console.error('Error fetching system default by ID:', error);
      return null;
    }
  },

  async createSystemDefault(data) {
    try {
      const response = await apiClient.post('/system-defaults', data);
      return response;
    } catch (error) {
      console.error('Error creating system default:', error);
      return null;
    }
  },

  async updateSystemDefault(id, data) {
    try {
      const response = await apiClient.put(`/system-defaults/${id}`, data);
      return response;
    } catch (error) {
      console.error('Error updating system default:', error);
      return null;
    }
  },

  async deleteSystemDefault(id) {
    try {
      const response = await apiClient.delete(`/system-defaults/${id}`);
      return response ? (response.success !== false) : false;
    } catch (error) {
      console.error('Error deleting system default:', error);
      return false;
    }
  },

  async setSystemDefault(key, value) {
    try {
      // Look for existing system default with this key
      const allDefaults = await this.getAllSystemDefaults();
      const existingDefault = allDefaults.find(def => def.key === key);
      
      if (existingDefault) {
        // Update existing
        return await this.updateSystemDefault(existingDefault.id, { key, value });
      } else {
        // Create new
        return await this.createSystemDefault({ key, value });
      }
    } catch (error) {
      console.error('Error setting system default:', error);
      return null;
    }
  }
};

export default systemDefaultsService;