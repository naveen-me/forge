import authService from './services/authService';

const createApiClient = () => {
  const request = async (method, url, data = null, headers = {}) => {
    try {
      const token = authService.getToken();
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      const response = await window.electronAPI.api.call(method, url, data, headers);

      if (response.success) {
        return response.data;
      } else {
        if (response.error?.message.includes('401')) {
          authService.logout();
          // Ensure hash-based navigation works inside Electron
          if (typeof window !== 'undefined') {
            window.location.hash = '#/login';
          }
        }
        throw new Error(response.error?.message || 'API call failed');
      }
    } catch (error) {
      console.error(`API call failed: ${method.toUpperCase()} ${url}`, error);
      throw error;
    }
  };

  return {
    get: (url, config = {}) => request('GET', url, null, config.headers),
    post: (url, data, config = {}) => request('POST', url, data, config.headers),
    put: (url, data, config = {}) => request('PUT', url, data, config.headers),
    delete: (url, config = {}) => request('DELETE', url, null, config.headers),
  };
};

let apiClient;

if (typeof window !== 'undefined' && window.electronAPI) {
  apiClient = createApiClient();
} else {
  // Fallback for non-Electron environments (if any)
  const axios = require('axios');
  apiClient = axios.create({
    baseURL: 'http://localhost:3001/api',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  apiClient.interceptors.request.use(
    (config) => {
      const token = authService.getToken();
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response && error.response.status === 401) {
        authService.logout();
        if (typeof window !== 'undefined') {
          window.location.hash = '#/login';
        }
      }
      return Promise.reject(error);
    }
  );
}

export default apiClient;