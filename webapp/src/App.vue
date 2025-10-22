<template>
  <div id="app" class="flex flex-col min-h-screen bg-gray-100">
    <header class="bg-gray-800 text-white p-4 shadow-md">
      <div class="container mx-auto flex justify-between items-center">
        <h3 class="text-xl font-bold">Playout Control</h3>
        <nav v-if="auth.isAuthenticated">
          <router-link to="/" class="text-gray-300 hover:text-white px-3 py-2 rounded-md text-sm font-medium">Dashboard</router-link>
          <router-link to="/subscription" class="text-gray-300 hover:text-white px-3 py-2 rounded-md text-sm font-medium">Subscription</router-link>
          <button @click="logout" class="ml-4 bg-red-500 hover:bg-red-600 text-white font-bold py-2 px-4 rounded">Logout</button>
        </nav>
      </div>
    </header>

    <main class="flex-grow container mx-auto my-8 p-4">
      <router-view />
    </main>

    <footer class="bg-gray-200 text-center p-4">
      <p class="text-gray-600">&copy; 2025 Playout System. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { useAuthStore } from './store';
import { useRouter } from 'vue-router';

const auth = useAuthStore();
const router = useRouter();

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
