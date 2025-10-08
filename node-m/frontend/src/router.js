import { createRouter, createWebHashHistory } from 'vue-router';
import authService from './services/authService';

import Login from './components/auth/Login.vue';
import Register from './components/auth/Register.vue';
import PinLogin from './components/auth/PinLogin.vue';
import Dashboard from './Dashboard.vue';

// Import page components
import MainContent from './components/MainContent.vue';
import MediaLibraryPage from './components/pages/MediaLibrary.vue';
import AdsPage from './components/pages/Ads.vue';
import OverlaysPage from './components/pages/Overlays.vue';
import LivePage from './components/pages/Live.vue';
import CalendarPage from './components/pages/Calendar.vue';
import SchedulerPage from './components/scheduler/SchedulerView.vue';

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login, meta: { public: true } },
  { path: '/register', name: 'Register', component: Register, meta: { public: true } },
  { path: '/pin-login', name: 'PinLogin', component: PinLogin, meta: { public: true } },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    redirect: '/dashboard/main',
    children: [
      { path: 'main', name: 'MainContent', component: MainContent },
      { path: 'media-library', name: 'MediaLibrary', component: MediaLibraryPage },
      { path: 'ads', name: 'Ads', component: AdsPage },
      { path: 'overlays', name: 'Overlays', component: OverlaysPage },
      { path: 'live', name: 'Live', component: LivePage },
      { path: 'calendar', name: 'Calendar', component: CalendarPage },
      { path: 'scheduler', name: 'Scheduler', component: SchedulerPage },
    ]
  },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const isAuthenticated = authService.isAuthenticated();
  const isPublicRoute = to.matched.some(record => record.meta.public);

  // If trying to access a protected route without being authenticated,
  // redirect to the login page.
  if (!isAuthenticated && !isPublicRoute) {
    return next('/login');
  }

  // If authenticated and trying to access a public route (like login),
  // redirect to the dashboard.
  if (isAuthenticated && isPublicRoute) {
    return next('/dashboard');
  }

  // Otherwise, allow navigation.
  next();
});

export default router;