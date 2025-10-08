
<template>
  <div class="login-container min-vh-100 d-flex align-items-center">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-lg-5">
          <div class="card shadow-lg border-0 rounded-4">
            <div class="card-body p-5">
              <div class="text-center mb-5">
                <h2 class="fw-bold">Welcome Back</h2>
                <p class="text-muted">Sign in to your account</p>
              </div>

              <form @submit.prevent="handleLogin">
                <div class="mb-4">
                  <label for="email" class="form-label fw-semibold">Email Address</label>
                  <div class="input-group">
                    <span class="input-group-text bg-light border-end-0">
                      <i class="bi bi-envelope"></i>
                    </span>
                    <input
                      id="email"
                      name="email"
                      type="email"
                      class="form-control border-start-0"
                      v-model="email"
                      placeholder="name@example.com"
                    />
                  </div>
                </div>

                <div class="mb-4">
                  <label for="password" class="form-label fw-semibold">Password</label>
                  <div class="input-group">
                    <span class="input-group-text bg-light border-end-0">
                      <i class="bi bi-lock"></i>
                    </span>
                    <input
                      id="password"
                      name="password"
                      type="password"
                      class="form-control border-start-0"
                      v-model="password"
                      placeholder="Enter your password"
                    />
                  </div>
                </div>

                <div class="d-grid mb-4">
                  <button type="submit" class="btn btn-primary btn-lg rounded-pill" :disabled="loading">
                    <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    <span v-if="loading">Signing In...</span>
                    <span v-else>Sign In</span>
                  </button>
                </div>
              </form>

              <div class="text-center">
                <p class="mb-0">Don't have an account?
                  <router-link to="/register" class="text-decoration-none fw-semibold">
                    Sign up
                  </router-link>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import authService from '../../services/authService'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const email = ref('mail@mail.com') // Pre-fill for convenience
    const password = ref('P@$$w0rd') // Pre-fill for convenience

    const handleLogin = async () => {
      const emailVal = email.value;
      const passwordVal = password.value;

      if (!emailVal || !passwordVal) {
        alert('Please enter both email and password');
        return;
      }

      loading.value = true;

      try {
        const response = await authService.login(emailVal, passwordVal);

        if (response.success) {
          router.push('/dashboard');
        } else {
          alert(response.message || 'Login failed');
        }
      } catch (error) {
        alert(`Login failed: ${error.message}`);
      } finally {
        loading.value = false;
      }
    }

    return {
      loading,
      email,
      password,
      handleLogin
    }
  }
}
</script>

<style scoped>
/* Styles remain the same */
</style>
