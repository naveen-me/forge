import axios from 'axios';
import dotenv from 'dotenv';

dotenv.config();

const apiClient = axios.create({
  baseURL: `http://localhost:${process.env.PORT || 3001}/api`,
  headers: {
    'Content-Type': 'application/json',
  },
});

export default {
  getSchedule(channelId, date) {
    return apiClient.get(`/schedule/${channelId}/${date}`);
  },
  getMediaItem(id) {
    return apiClient.get(`/media/${id}`);
  },
  getLink(id) {
    return apiClient.get(`/links/${id}`);
  },
};
