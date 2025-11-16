import { createRouter, createWebHistory } from 'vue-router';
import Dashboard from '../components/Dashboard.vue';
import Login from '../components/Login.vue';
import Register from '../components/Register.vue';
import Subscription from '../components/Subscription.vue';
import Features from '../components/Features.vue';
import UPIXManagement from '../components/UPIXManagement.vue';
import MediaLibrary from '../components/MediaLibrary.vue';
import Overlays from '../views/Overlays.vue';
import Ads from '../components/Ads.vue';
import Links from '../components/Live.vue';
import Scheduler from '../views/Scheduler.vue';
import UserProfile from '../components/UserProfile.vue';
import Settings from '../views/Settings.vue';
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
    {
        path: '/media-library',
        name: 'MediaLibrary',
        component: MediaLibrary,
        meta: { requiresAuth: true },
    },
    {
        path: '/overlays',
        name: 'Overlays',
        component: Overlays,
        meta: { requiresAuth: true },
    },
    {
        path: '/ads',
        name: 'Ads',
        component: Ads,
        meta: { requiresAuth: true },
    },
    {
        path: '/links',
        name: 'Links',
        component: Links,
        meta: { requiresAuth: true },
    },
    {
        path: '/scheduler',
        name: 'Scheduler',
        component: Scheduler,
        meta: { requiresAuth: true },
    },
    {
        path: '/profile',
        name: 'UserProfile',
        component: UserProfile,
        meta: { requiresAuth: true },
    },
    {
        path: '/settings',
        name: 'Settings',
        component: Settings,
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
