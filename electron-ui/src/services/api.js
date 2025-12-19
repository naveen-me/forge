/**
 * API service for communicating with the playout engine
 * This service handles communication between the UI and the .NET engine
 */
class ApiService {
  constructor() {
    this.engineConnected = false;
  }

  async connectToEngine() {
    try {
      const result = await window.electronAPI.connectEngine();
      this.engineConnected = result.success;
      return result;
    } catch (error) {
      console.error('Error connecting to engine:', error);
      return { success: false, error: error.message };
    }
  }

  async disconnectFromEngine() {
    try {
      const result = await window.electronAPI.disconnectEngine();
      this.engineConnected = false;
      return result;
    } catch (error) {
      console.error('Error disconnecting from engine:', error);
      return { success: false, error: error.message };
    }
  }

  async getEngineStatus() {
    try {
      return await window.electronAPI.getEngineStatus();
    } catch (error) {
      console.error('Error getting engine status:', error);
      return { connected: false };
    }
  }

  async loadSchedule(schedulePath) {
    try {
      return await window.electronAPI.loadSchedule(schedulePath);
    } catch (error) {
      console.error('Error loading schedule:', error);
      return { success: false, error: error.message };
    }
  }

  async saveSchedule(schedulePath, scheduleData) {
    try {
      return await window.electronAPI.saveSchedule(schedulePath, scheduleData);
    } catch (error) {
      console.error('Error saving schedule:', error);
      return { success: false, error: error.message };
    }
  }

  async validateSchedule(scheduleData) {
    try {
      return await window.electronAPI.validateSchedule(scheduleData);
    } catch (error) {
      console.error('Error validating schedule:', error);
      return { success: false, error: error.message };
    }
  }
}

export default new ApiService();