import axios from 'axios';
import { useAuthStore } from './store/index';

const apiClient = axios.create({
  baseURL: 'http://localhost:3001/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  async (config) => {
    const authStore = useAuthStore();
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle auth errors
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      const authStore = useAuthStore();
      authStore.logout();
      window.location.href = '/login'; // Force redirect to login
    }
    return Promise.reject(error);
  }
);

export default {
  getMedia() {
    return apiClient.get('/media');
  },
  getAds() {
    return apiClient.get('/ads');
  },
  getLinks() {
    return apiClient.get('/links');
  },
  createLink(data) {
    return apiClient.post('/links', data);
  },
  updateLink(id, data) {
    return apiClient.put(`/links/${id}`, data);
  },
  deleteLink(id) {
    return apiClient.delete(`/links/${id}`);
  },
  getSchedule(channelId, date) {
    return apiClient.get(`/schedule/${channelId}/${date}`);
  },
  addScheduleItem(channelId, data) {
    return apiClient.post(`/schedule/${channelId}`, data);
  },
  updateScheduleItem(channelId, scheduleId, data) {
    return apiClient.put(`/schedule/${channelId}/${scheduleId}`, data);
  },
  deleteScheduleItem(channelId, scheduleId) {
    return apiClient.delete(`/schedule/${channelId}/${scheduleId}`);
  },
};
