import { createRouter, createWebHistory } from 'vue-router';
import Dashboard from '../components/Dashboard.vue';
import Login from '../components/Login.vue';
import Register from '../components/Register.vue';
import Subscription from '../components/Subscription.vue';
import Features from '../components/Features.vue';
import UPIXManagement from '../components/UPIXManagement.vue';
import { useAuthStore } from '../store';
import { useSubscriptionStore } from '../store';

const routes = [
    {
        path: '/',
        name: 'Dashboard',
        component: Dashboard,
        meta: { requiresAuth: true, requiresSubscription: true }, // Requires active subscription
    },
    {
        path: '/login',
        name: 'Login',
        component: Login,
    },
    {
        path: '/register',
        name: 'Register',
        component: Register,
    },
    {
        path: '/subscription',
        name: 'Subscription',
        component: Subscription,
        meta: { requiresAuth: true },
    },
    {
        path: '/features',
        name: 'Features',
        component: Features,
        meta: { requiresAuth: true },
    },
    {
        path: '/upi-management',
        name: 'UPIXManagement',
        component: UPIXManagement,
        meta: { requiresAuth: true },
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach(async (to, from, next) => {
    const auth = useAuthStore();
    
    if (to.meta.requiresAuth && !auth.isAuthenticated) {
        next('/login');
        return;
    }
    
    // If the route requires subscription check or is the dashboard, 
    // fetch user subscription status
    if (to.meta.requiresAuth && auth.isAuthenticated) {
        const subscription = useSubscriptionStore();
        await subscription.fetchUserSubscription();
        
        // Check if the route requires an active subscription
        if (to.meta.requiresSubscription) {
            if (!subscription.userSubscription || subscription.userSubscription.status !== 'active') {
                // Redirect to subscription page if no active subscription
                next('/subscription');
                return;
            }
        }
    }
    
    next();
});

export default router;
