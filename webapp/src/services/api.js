import axios from 'axios';
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
    baseURL: '/auth',  // Different base URL for authentication endpoints
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor to add auth token to regular API calls
api.interceptors.request.use(
    (config) => {
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
    (error) => {
        if (error.response?.status === 401 || error.response?.status === 403) {
            // Clear auth data and redirect to login for auth-related errors
            const authStore = useAuthStore();
            authStore.logout();
            window.location.href = '/login'; // Force redirect to login
        }
        return Promise.reject(error);
    }
);

// No auth interceptor needed for auth API calls since they don't require auth headers
authApi.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Export both instances
export { api, authApi };
export default api;