<template>
  <div id="app" class="d-flex flex-column min-vh-100">
    <header class="bg-dark text-white p-3">
      <div class="container d-flex justify-content-between align-items-center">
        <h3>Playout Control</h3>
        <nav v-if="auth.isAuthenticated">
          <router-link to="/" class="text-white me-3">Dashboard</router-link>
          <button @click="logout" class="btn btn-outline-light">Logout</button>
        </nav>
      </div>
    </header>

    <main class="flex-grow-1 container my-4">
      <router-view />
    </main>

    <footer class="bg-light text-center p-3">
      <p>&copy; 2025 Playout System. All rights reserved.</p>
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
