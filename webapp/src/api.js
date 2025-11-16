import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:3001/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

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
