import axios from 'axios';
// The store import is intentionally commented out here.
// Pinia stores should not be used directly in service files like this
// because it creates a circular dependency issue during app initialization.
// Instead, the interceptor should get the store instance when the request is made.
import { useAuthStore } from '../store'; 

// API instance for regular endpoints (under /api)
const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// API instance for auth endpoints (under /auth)
const authApi = axios.create({
    baseURL: '/auth', // Different base URL for authentication endpoints
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor to add auth token to regular API calls
api.interceptors.request.use(
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

// Response interceptor to handle auth errors for regular API calls
api.interceptors.response.use(
    (response) => {
        return response;
    },
    async (error) => {
        if (error.response?.status === 401 || error.response?.status === 403) {
            // Dynamically import the store
            const authStore = useAuthStore();
            authStore.logout();
            window.location.href = '/login'; // Force redirect to login
        }
        return Promise.reject(error);
    }
);

// --- Media Library API Methods ---

const mediaApi = axios.create({
  baseURL: '/api/media',  // Use relative path to go through Vite proxy
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add the same auth interceptor for media API calls
mediaApi.interceptors.request.use(
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

// Add the same response interceptor for media API calls
mediaApi.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      // Dynamically import the store
      const authStore = useAuthStore();
      authStore.logout();
      window.location.href = '/login'; // Force redirect to login
    }
    return Promise.reject(error);
  }
);


const mediaService = {
  getFolderContents(parentId = null) {
    const params = parentId ? { parentId } : {};
    return mediaApi.get('/', { params });
  },

  createFolder(name, parentId = null) {
    return mediaApi.post('/folder', { name, parentId });
  },

  selectFiles() {
    return mediaApi.get('/select-files');
  },

  addFiles(files, parentId = null) {
    return mediaApi.post('/files', { files, parentId });
  },

  renameItem(id, name) {
    return mediaApi.put(`/${id}/rename`, { name });
  },

  moveItem(id, parentId) {
    return mediaApi.put(`/${id}/move`, { parentId });
  },

  deleteItem(id) {
    return mediaApi.delete(`/${id}`);
  },

  searchItems(query) {
    return mediaApi.get(`/search/${query}`);
  },
  
  getAllFolders() {
    return mediaApi.get('/folder'); 
  },

  getFolderPath(folderId) {
    const url = folderId ? `/folder/${folderId}/path` : '/folder/null/path';
    return mediaApi.get(url);
  }
};


authApi.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Export all instances and the new service
export { api, authApi, mediaService };
export default api;