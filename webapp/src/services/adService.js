import { api } from './api';

const API_URL = '/ads';

const getAds = (groupId) => {
  const url = groupId ? `${API_URL}?groupId=${groupId}` : API_URL;
  return api.get(url);
};

const selectFiles = () => {
  return api.get(`${API_URL}/select-files`);
};

const addFiles = (files, parentId) => {
  return api.post(`${API_URL}/files`, { files, parentId });
};

const generateThumbnail = (id) => {
    return api.post(`${API_URL}/${id}/thumbnail`);
};

const renameAd = (id, name) => {
  return api.put(`${API_URL}/${id}/rename`, { name });
};

const createGroup = (name) => {
  return api.post(`${API_URL}/group`, { name });
};

const updateOrder = (orderedIds, parentId) => {
  return api.post(`${API_URL}/order`, { orderedIds, parentId });
};

const deleteAd = (id) => {
  return api.delete(`${API_URL}/${id}`);
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