<template>
  <div class="row justify-content-center">
    <div class="col-md-6 col-lg-4">
      <div class="card shadow-sm">
        <div class="card-body">
          <h2 class="card-title text-center mb-4">Login</h2>
          <form @submit.prevent="handleLogin">
            <div v-if="error" class="alert alert-danger">{{ error }}</div>
            <div class="mb-3">
              <label for="username" class="form-label">Username</label>
              <input type="text" id="username" v-model="username" class="form-control" required autofocus>
            </div>
            <div class="mb-3">
              <label for="password" class="form-label">Password</label>
              <input type="password" id="password" v-model="password" class="form-control" required>
            </div>
            <div class="d-grid">
              <button type="submit" class="btn btn-primary">Login</button>
            </div>
          </form>
          <div class="mt-3 text-center text-muted">
            <small>Hint: Use admin / password</small>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../store';

const username = ref('admin');
const password = ref('password');
const error = ref('');
const router = useRouter();
const auth = useAuthStore();

const handleLogin = () => {
  if (auth.login(username.value, password.value)) {
    router.push('/');
  } else {
    error.value = 'Invalid username or password';
  }
};
</script>

<style scoped>
.card {
  border: none;
  border-radius: 0.75rem;
}
</style>
