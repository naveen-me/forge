<template>
  <div class="pin-login-container min-vh-100 d-flex align-items-center justify-content-center">
    <div class="col-lg-6">
      <div class="card shadow-lg border-0 rounded-4">
        <div class="card-body p-5">
          <div class="text-center mb-5">
            <h2 class="fw-bold">Enter PIN</h2>
            <p class="text-muted">Please enter your PIN to access the application</p>
          </div>

          <form @submit.prevent="onSubmit">
            <div class="mb-4">
              <label for="pin" class="form-label fw-semibold">PIN (4-6 digits)</label>
              <div class="input-group">
                <span class="input-group-text bg-light border-end-0">
                  <i class="bi bi-shield-lock"></i>
                </span>
                <input
                  id="pin"
                  name="pin"
                  type="password"
                  class="form-control border-start-0"
                  v-model="pin"
                  placeholder="Enter your PIN"
                />
              </div>
            </div>

            <div class="d-grid mb-4">
              <button type="submit" class="btn btn-primary btn-lg rounded-pill" :disabled="loading">
                <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                <span v-if="loading">Unlocking...</span>
                <span v-else>Unlock</span>
              </button>
            </div>
          </form>

          <div class="text-center">
            <button @click="logout" class="btn btn-link text-danger text-decoration-none fw-semibold">
              <i class="bi bi-box-arrow-right me-1"></i> Logout
            </button>
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
  name: 'PinLogin',
  setup() {
    const router = useRouter()

    const loading = ref(false)
    const pin = ref('')

    const onSubmit = async () => {
      if (!pin.value || !/^\d{4,6}$/.test(pin.value)) {
        alert('PIN must be 4-6 digits')
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
        const response = await authService.validatePin(pin.value);
        console.log('PIN validation response:', response);
        if (response.success) {
          // Set session flag when PIN is validated
          window.pinValidatedThisSession = true;
          // Notify main process that PIN is validated
          if (window.electronAPI) {
            window.electronAPI.send('pin-validated', true);
          }
          // Navigate to dashboard
          console.log('PIN validation successful, navigating to dashboard');
          router.push('/dashboard');
        } else {
          alert('Invalid PIN')
        }
      } catch (error) {
        console.error('PIN validation error:', error);
        alert(`PIN validation failed: ${error.message || 'An error occurred during PIN validation'}`);
      } finally {
        loading.value = false;
      }
    }

    const logout = async () => {
      try {
        // Reset session flag on logout
        window.pinValidatedThisSession = false;
        await authService.logout();
        router.push('/login');
      } catch (error) {
        console.error('Logout error:', error);
        alert('Error during logout: ' + error.message);
      }
    }

    return {
      loading,
      pin,
      onSubmit,
      logout
    }
  }
}
</script>

<style scoped>
.pin-login-container {
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