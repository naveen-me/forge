<template>
  <div class="min-vh-100 d-flex">
    <menu-bar v-if="auth.isAuthenticated" />
    
    <div class="flex-grow-1 d-flex flex-column" :style="auth.isAuthenticated ? {
    marginLeft: '60px',
    borderLeft: '1px solid #dfdfdf',
    borderTop: '1px solid #dfdfdf',
    backgroundColor: '#fff',
    boxShadow: '0px 0px 1px rgba(0, 0, 0, 0.1)'
    } : {}">
      <main class="p-3">
        <router-view />
      </main>
    </div>
    
</div>
</template>

<script setup>
import { useAuthStore } from './store';
import { useRouter } from 'vue-router';
import MenuBar from './MenuBar.vue';

const auth = useAuthStore();
const router = useRouter();
console.log('User authenticated:', auth.isAuthenticated);
const logout = () => {
  auth.logout();
  router.push('/login');
};
</script>

<style>
body {
  background-color: #f8f9fa;
}
</style>