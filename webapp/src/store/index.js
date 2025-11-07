import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import api, { authApi } from '../services/api';

export const useAuthStore = defineStore('auth', () => {
    const token = ref(localStorage.getItem('token') || null);
    const isAuthenticated = computed(() => !!token.value);

    async function register(name, email, password) {
        const response = await authApi.post('/register', { name, email, password });
        if (response.data.success) {
            return response.data.message;
        } else {
            throw new Error(response.data.message);
        }
    }

    async function login(email, password) {
        const response = await authApi.post('/login', { email, password });
        if (response.data.success && response.data.token) {
            token.value = response.data.token;
            localStorage.setItem('token', token.value);
        } else {
            throw new Error(response.data.message);
        }
    }

    function logout() {
        token.value = null;
        localStorage.removeItem('token');
        localStorage.removeItem('userSubscription');
    }

    return { token, isAuthenticated, register, login, logout };
});

export const useSubscriptionStore = defineStore('subscription', () => {
    const storedSubscriptionData = JSON.parse(localStorage.getItem('userSubscription'));
    const now = new Date().getTime();
    const twelveHours = 12 * 60 * 60 * 1000;

    let initialSubscription = null;
    let initialPurchasedFeatures = [];
    let initialAvailableFeatures = [];

    if (storedSubscriptionData && (now - storedSubscriptionData.timestamp < twelveHours)) {
        initialSubscription = storedSubscriptionData.subscription;
        initialPurchasedFeatures = storedSubscriptionData.purchased_features || [];
        initialAvailableFeatures = storedSubscriptionData.available_features || [];
    }

    const plans = ref([]);
    const userSubscription = ref(initialSubscription);
    const purchasedFeatures = ref(initialPurchasedFeatures);
    const availableFeatures = ref(initialAvailableFeatures);
    const paymentHistory = ref([]);
    const upiDetails = ref([]);

    async function fetchPlans() {
        try {
            const response = await api.get('/subscription/plans');
            if (response.data.success) {
                plans.value = response.data.data;
            }
        } catch (error) {
            console.error('Failed to fetch plans:', error);
        }
    }

    async function subscribe(planId) {
        try {
            const response = await api.post('/subscription/subscribe', { planId });
            if (response.data.success) {
                return response.data;
            } else {
                throw new Error(response.data.error || 'Failed to create subscription request');
            }
        } catch (error) {
            console.error('Subscription request error:', error);
            throw error;
        }
    }

    async function purchaseFeature(featureId) {
        try {
            const response = await api.post('/subscription/purchase-feature', { featureId });
            if (response.data.success) {
                return response.data;
            } else {
                throw new Error(response.data.error || 'Failed to create feature purchase request');
            }
        } catch (error) {
            console.error('Feature purchase request error:', error);
            throw error;
        }
    }

    async function fetchUserSubscription() {
        const storedSubscriptionData = JSON.parse(localStorage.getItem('userSubscription'));
        const now = new Date().getTime();
        const twelveHours = 12 * 60 * 60 * 1000;

        if (storedSubscriptionData && (now - storedSubscriptionData.timestamp < twelveHours)) {
            userSubscription.value = storedSubscriptionData.subscription;
            purchasedFeatures.value = storedSubscriptionData.purchased_features || [];
            availableFeatures.value = storedSubscriptionData.available_features || [];
            return; // Exit if cached data is fresh
        }

        try {
            const response = await api.get('/subscription/my-subscription');
            if (response.data.success) {
                const newSubscriptionData = {
                    subscription: response.data.subscription,
                    purchased_features: response.data.purchased_features || [],
                    available_features: response.data.available_features || [],
                    timestamp: new Date().getTime(),
                };
                localStorage.setItem('userSubscription', JSON.stringify(newSubscriptionData));

                userSubscription.value = newSubscriptionData.subscription;
                purchasedFeatures.value = newSubscriptionData.purchased_features;
                availableFeatures.value = newSubscriptionData.available_features;
            }
        } catch (error) {
            console.error('Failed to fetch user subscription:', error);
        }
    }

    async function cancelSubscription() {
        try {
            const response = await api.delete('/subscription/cancel');
            if (response.data.success) {
                userSubscription.value = null; // Clear the subscription
                localStorage.removeItem('userSubscription');
                return response.data;
            } else {
                throw new Error(response.data.error || 'Failed to cancel subscription');
            }
        } catch (error) {
            console.error('Cancel subscription error:', error);
            throw error;
        }
    }

    async function fetchPaymentHistory() {
        try {
            const response = await api.get('/payment/user');
            if (response.data.success) {
                paymentHistory.value = response.data.data;
            }
        } catch (error) {
            console.error('Failed to fetch payment history:', error);
        }
    }

    // UPI Management Functions
    async function fetchUPIXDetails() {
        try {
            const response = await api.get('/upi/upi-details');
            if (response.data.success) {
                upiDetails.value = response.data.data;
            }
        } catch (error) {
            console.error('Failed to fetch UPI details:', error);
        }
    }

    async function addUPIXDetail(upiData) {
        try {
            const response = await api.post('/upi/upi-details', upiData);
            if (response.data.success) {
                // Refresh the list
                await fetchUPIXDetails();
                return response.data;
            } else {
                throw new Error(response.data.error || 'Failed to add UPI detail');
            }
        } catch (error) {
            console.error('Failed to add UPI detail:', error);
            throw error;
        }
    }

    async function updateUPIXDetail(id, upiData) {
        try {
            const response = await api.put(`/upi/upi-details/${id}`, upiData);
            if (response.data.success) {
                // Refresh the list
                await fetchUPIXDetails();
                return response.data;
            } else {
                throw new Error(response.data.error || 'Failed to update UPI detail');
            }
        } catch (error) {
            console.error('Failed to update UPI detail:', error);
            throw error;
        }
    }

    async function setPrimaryUPIXDetail(id) {
        try {
            const response = await api.put(`/upi/upi-details/${id}/set-primary`);
            if (response.data.success) {
                // Refresh the list
                await fetchUPIXDetails();
                return response.data;
            } else {
                throw new Error(response.data.error || 'Failed to set primary UPI detail');
            }
        } catch (error) {
            console.error('Failed to set primary UPI detail:', error);
            throw error;
        }
    }

    async function deleteUPIXDetail(id) {
        try {
            const response = await api.delete(`/upi/upi-details/${id}`);
            if (response.data.success) {
                // Refresh the list
                await fetchUPIXDetails();
                return response.data;
            } else {
                throw new Error(response.data.error || 'Failed to delete UPI detail');
            }
        } catch (error) {
            console.error('Failed to delete UPI detail:', error);
            throw error;
        }
    }

    return { 
        plans, 
        userSubscription, 
        purchasedFeatures, 
        availableFeatures,
        paymentHistory,
        upiDetails,
        fetchPlans, 
        subscribe,
        purchaseFeature,
        fetchUserSubscription,
        cancelSubscription,
        fetchPaymentHistory,
        // UPI Management
        fetchUPIXDetails,
        addUPIXDetail,
        updateUPIXDetail,
        setPrimaryUPIXDetail,
        deleteUPIXDetail
    };
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