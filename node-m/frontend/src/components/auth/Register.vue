<template>
  <div class="register-container min-vh-100 d-flex align-items-center">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-lg-6">
          <div class="card shadow-lg border-0 rounded-4">
            <div class="card-body p-5">
              <div class="text-center mb-5">
                <h2 class="fw-bold">Create Account</h2>
                <p class="text-muted">Join us today and get started</p>
              </div>

              <form @submit.prevent="onSubmit">
                <div class="mb-4">
                  <label for="name" class="form-label fw-semibold">Full Name</label>
                  <div class="input-group">
                    <span class="input-group-text bg-light border-end-0">
                      <i class="bi bi-person"></i>
                    </span>
                    <input
                      id="name"
                      name="name"
                      type="text"
                      class="form-control border-start-0"
                      v-model="name"
                      placeholder="Enter your full name"
                    />
                  </div>
                </div>

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
                      placeholder="Create a strong password"
                    />
                  </div>
                </div>

                <div class="d-grid mb-4">
                  <button type="submit" class="btn btn-primary btn-lg rounded-pill" :disabled="loading">
                    <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    <span v-if="loading">Creating Account...</span>
                    <span v-else>Create Account</span>
                  </button>
                </div>
              </form>

              <div class="text-center">
                <p class="mb-0">Already have an account?
                  <router-link to="/login" class="text-decoration-none fw-semibold">
                    Sign in
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
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import authService from '../../services/authService'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()

    const loading = ref(false)
    const name = ref('')
    const email = ref('')
    const password = ref('')

    const onSubmit = async () => {
      if (!name.value) {
        alert('Name is required')
        return
      }

      if (!email.value || !/\S+@\S+\.\S+/.test(email.value)) {
        alert('Email address is invalid')
        return
      }

      if (!password.value || password.value.length < 6) {
        alert('Password must be at least 6 characters')
        return
      }

      loading.value = true

      // Check connectivity first
      const isOnline = await authService.checkConnectivity()
      if (!isOnline) {
        loading.value = false
        alert('No internet connection. Please check your network and try again.')
        return
      }

      try {
        const response = await authService.register(name.value, email.value, password.value)

        if (response.success) {
          // Save token - check where the token is in the response
          const token = response.token || (response.data && response.data.token);
          if (token) {
            await authService.saveTokenAndPin(token, null);
          }

          // Show success toast
          if (window.showToast) {
            window.showToast('Registration successful!', 'success');
          } else {
            alert('Registration successful')
          }

          // Check if user has set a PIN
          nextTick(async () => {
            const userHasPin = await authService.hasPin();
            if (userHasPin) {
              router.push('/pin-login')
            } else {
              router.push('/set-pin')
            }
          });
        } else {
          // Show error toast
          const message = response.message || (response.data && response.data.message) || 'Registration failed';
          alert(message)
        }
      } catch (error) {
        console.error('Registration error:', error);
        alert(`Registration failed: ${error.message || 'An error occurred during registration'}`);
      } finally {
        loading.value = false;
      }
    }

    return {
      loading,
      name,
      email,
      password,
      onSubmit
    }
  }
}
</script>

<style scoped>
.register-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.card {
  backdrop-filter: blur(10px);
  background-color: rgba(255, 255, 255, 0.85);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.form-label {
  color: #2c3e50;
}

.input-group-text {
  background-color: #f8f9fa;
}

.btn-primary {
  background: linear-gradient(135deg, #3498db 0%, #1a5f9e 100%);
  border: none;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  background: linear-gradient(135deg, #2980b9 0%, #154a7c 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(52, 152, 219, 0.3);
}

.rounded-4 {
  border-radius: 1rem !important;
}

.shadow-lg {
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15) !important;
}
</style>
