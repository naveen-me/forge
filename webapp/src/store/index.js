import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import api from '../services/api';

export const useAuthStore = defineStore('auth', () => {
    const isAuthenticated = ref(false);

    function login(username, password) {
        // Mock login
        if (username === 'admin' && password === 'password') {
            isAuthenticated.value = true;
            return true;
        }
        return false;
    }

    function logout() {
        isAuthenticated.value = false;
    }

    return { isAuthenticated, login, logout };
});

export const useDashboardStore = defineStore('dashboard', () => {
    const stats = ref([]);
    const apiResponse = ref(null);
    const obsStatus = ref('Disconnected');

    const formattedStats = computed(() => {
        const statsObj = {};
        stats.value.forEach(stat => {
            statsObj[stat.name] = stat.value;
        });
        return statsObj;
    });

    async function fetchStats() {
        try {
            const response = await api.get('/stats');
            stats.value = response.data.data;
        } catch (error) {
            console.error('Failed to fetch stats:', error);
        }
    }

    async function testApi() {
        try {
            const response = await api.post('/test-api');
            apiResponse.value = response.data;
        } catch (error) {
            console.error('Failed to test API:', error);
            apiResponse.value = { error: error.message };
        }
    }

    async function connectObs(credentials) {
        try {
            const response = await api.post('/connect-obs', credentials);
            obsStatus.value = response.data.message;
        } catch (error) {
            console.error('Failed to connect to OBS:', error);
            obsStatus.value = 'Connection Failed';
        }
    }

    return { stats, apiResponse, obsStatus, formattedStats, fetchStats, testApi, connectObs };
});
