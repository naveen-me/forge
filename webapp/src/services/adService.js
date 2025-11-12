import axios from 'axios';

const API_URL = '/api/ads';

const getAds = () => {
  return axios.get(API_URL);
};

const selectFiles = () => {
  return axios.get(`${API_URL}/select-files`);
};

const addFiles = (files, parentId) => {
  return axios.post(`${API_URL}/files`, { files, parentId });
};

const generateThumbnail = (id) => {
    return axios.post(`${API_URL}/${id}/thumbnail`);
};

const renameAd = (id, name) => {
  return axios.put(`${API_URL}/${id}/rename`, { name });
};

const createGroup = (name, adIds) => {
  return axios.post(`${API_URL}/group`, { name, adIds });
};

const updateOrder = (orderedIds, parentId) => {
  return axios.post(`${API_URL}/order`, { orderedIds, parentId });
};

const deleteAd = (id) => {
  return axios.delete(`${API_URL}/${id}`);
};

export default {
  getAds,
  selectFiles,
  addFiles,
  generateThumbnail,
  renameAd,
  createGroup,
  updateOrder,
  deleteAd,
};