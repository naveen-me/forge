import authService from './services/authService';
import axios from 'axios'; // Import axios at the top level

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
        if (response.error && response.error.error && response.error.error.includes('401')) {
          authService.logout();
          // Ensure hash-based navigation works inside Electron
          if (typeof window !== 'undefined') {
            window.location.hash = '#/login';
          }
        }
        return null;
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

// Also create an axios-based client for non-electron fallback
const createAxiosClient = () => {
  // In development with Vite proxy, we want to make requests to the frontend server
  // which will proxy them to the backend. In production without Electron, we'd use the backend URL.
  const isDev = process.env.NODE_ENV !== 'production';
  const baseURL = isDev ? '' : 'http://localhost:3001'; // Empty string for proxy in development
  
  const client = axios.create({
    baseURL: baseURL,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  client.interceptors.request.use(
    (config) => {
      const token = authService.getToken();
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  return {
    get: async (url, config = {}) => {
      try {
        const response = await client.get(url, config);
        // Return success and the actual response data directly
        return { success: true, data: response.data };
      } catch (error) {
        if (error.response && error.response.status === 401) {
          authService.logout();
          if (typeof window !== 'undefined') {
            window.location.hash = '#/login';
          }
        }
        return { success: false, error: error.response?.data || { message: error.message } };
      }
    },
    post: async (url, data, config = {}) => {
      try {
        const response = await client.post(url, data, config);
        // Return success and the actual response data directly
        return { success: true, data: response.data };
      } catch (error) {
        if (error.response && error.response.status === 401) {
          authService.logout();
          if (typeof window !== 'undefined') {
            window.location.hash = '#/login';
          }
        }
        return { success: false, error: error.response?.data || { message: error.message } };
      }
    },
    put: async (url, data, config = {}) => {
      try {
        const response = await client.put(url, data, config);
        // Return success and the actual response data directly
        return { success: true, data: response.data };
      } catch (error) {
        if (error.response && error.response.status === 401) {
          authService.logout();
          if (typeof window !== 'undefined') {
            window.location.hash = '#/login';
          }
        }
        return { success: false, error: error.response?.data || { message: error.message } };
      }
    },
    delete: async (url, config = {}) => {
      try {
        const response = await client.delete(url, config);
        // For delete operations, return success and the response data (if any)
        return { success: true, data: response.data };
      } catch (error) {
        if (error.response && error.response.status === 401) {
          authService.logout();
          if (typeof window !== 'undefined') {
            window.location.hash = '#/login';
          }
        }
        return { success: false, error: error.response?.data || { message: error.message } };
      }
    }
  };
};

let apiClient;

if (typeof window !== 'undefined' && window.electronAPI) {
  apiClient = createApiClient();
} else {
  // Fallback for non-Electron environments
  apiClient = createAxiosClient();
}

export default apiClient;