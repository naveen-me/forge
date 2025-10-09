import apiClient from '../api';

const scheduleService = {
  async getSchedulesByDate(date) {
    try {
      const response = await apiClient.get(`/schedules?date=${date}`);
      return response;
    } catch (error) {
      console.error('Error fetching schedule by date:', error);
      return null;
    }
  },

  async getScheduleById(id) {
    try {
      const response = await apiClient.get(`/schedules/${id}`);
      return response;
    } catch (error) {
      console.error('Error fetching schedule by ID:', error);
      return null;
    }
  },

  async createSchedule(data) {
    try {
      const response = await apiClient.post('/schedules', data);
      return response;
    } catch (error) {
      console.error('Error creating schedule:', error);
      return null;
    }
  },

  async updateSchedule(id, data) {
    try {
      const response = await apiClient.put(`/schedules/${id}`, data);
      return response;
    } catch (error) {
      console.error('Error updating schedule:', error);
      return null;
    }
  },

  async deleteSchedule(id) {
    try {
      const response = await apiClient.delete(`/schedules/${id}`);
      return response ? (response.success !== false) : false;
    } catch (error) {
      console.error('Error deleting schedule:', error);
      return false;
    }
  },

  async duplicateSchedule(id, newDate) {
    try {
      const response = await apiClient.post(`/schedules/${id}/duplicate`, { newDate });
      return response;
    } catch (error) {
      console.error('Error duplicating schedule:', error);
      return null;
    }
  },

  async exportSchedule(id) {
    try {
      const response = await apiClient.post(`/schedules/${id}/export`);
      return response;
    } catch (error) {
      console.error('Error exporting schedule:', error);
      return null;
    }
  },

  async importSchedule(scheduleData, date) {
    try {
      const response = await apiClient.post('/schedules/import', { scheduleData, date });
      return response;
    } catch (error) {
      console.error('Error importing schedule:', error);
      return null;
    }
  },

  async createScheduleItem(scheduleId, data) {
    try {
      const response = await apiClient.post(`/schedules/${scheduleId}/items`, data);
      return response;
    } catch (error) {
      console.error('Error creating schedule item:', error);
      return null;
    }
  },

  async updateScheduleItem(scheduleId, itemId, data) {
    try {
      const response = await apiClient.put(`/schedules/${scheduleId}/items/${itemId}`, data);
      return response;
    } catch (error) {
      console.error('Error updating schedule item:', error);
      return null;
    }
  },

  async deleteScheduleItem(scheduleId, itemId) {
    try {
      const response = await apiClient.delete(`/schedules/${scheduleId}/items/${itemId}`);
      return response ? (response.success !== false) : false;
    } catch (error) {
      console.error('Error deleting schedule item:', error);
      return false;
    }
  },

  async reorderScheduleItems(scheduleId, orderedIds) {
    try {
      const response = await apiClient.post(`/schedules/${scheduleId}/items/reorder`, { orderedIds });
      return response ? (response.success !== false) : false;
    } catch (error) {
      console.error('Error reordering schedule items:', error);
      return false;
    }
  },

  async createScheduleItemAdPlacement(scheduleId, itemId, data) {
    try {
      const response = await apiClient.post(`/schedules/${scheduleId}/items/${itemId}/adplacements`, data);
      return response;
    } catch (error) {
      console.error('Error creating schedule item ad placement:', error);
      return null;
    }
  },

  async updateScheduleItemAdPlacement(scheduleId, itemId, adPlacementId, data) {
    try {
      const response = await apiClient.put(`/schedules/${scheduleId}/items/${itemId}/adplacements/${adPlacementId}`, data);
      return response;
    } catch (error) {
      console.error('Error updating schedule item ad placement:', error);
      return null;
    }
  },

  async deleteScheduleItemAdPlacement(scheduleId, itemId, adPlacementId) {
    try {
      const response = await apiClient.delete(`/schedules/${scheduleId}/items/${itemId}/adplacements/${adPlacementId}`);
      return response ? (response.success !== false) : false;
    } catch (error) {
      console.error('Error deleting schedule item ad placement:', error);
      return false;
    }
  }
};

export default scheduleService;