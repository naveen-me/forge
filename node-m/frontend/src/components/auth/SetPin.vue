<template>
  <div class="set-pin-container min-vh-100 d-flex align-items-center justify-content-center">
    <div class="col-md-4">
      <div class="card shadow-lg border-0 rounded-4">
        <div class="card-body p-5">
          <div class="text-center mb-4">
            <div class="icon-circle bg-primary text-white d-inline-flex align-items-center justify-content-center rounded-circle mb-3">
              <i class="bi bi-shield-lock fs-3"></i>
            </div>
            <h2 class="h4 fw-bold text-dark">Set PIN</h2>
            <p class="text-muted">Create a PIN for additional security</p>
          </div>

          <form @submit.prevent="setPin">
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
                  placeholder="Enter PIN"
                />
              </div>
            </div>

            <div class="mb-4">
              <label for="confirmPin" class="form-label fw-semibold">Confirm PIN</label>
              <div class="input-group">
                <span class="input-group-text bg-light border-end-0">
                  <i class="bi bi-shield-check"></i>
                </span>
                <input
                  id="confirmPin"
                  name="confirmPin"
                  type="password"
                  class="form-control border-start-0"
                  v-model="confirmPin"
                  placeholder="Confirm PIN"
                />
              </div>
            </div>

            <div class="d-grid">
              <button type="submit" class="btn btn-primary btn-lg rounded-pill" :disabled="loading">
                <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                <span v-if="loading">Setting PIN...</span>
                <span v-else>Set PIN</span>
              </button>
            </div>
          </form>
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
  name: 'SetPin',
  setup() {
    const router = useRouter()

    const loading = ref(false)
    const pin = ref('')
    const confirmPin = ref('')

    const setPin = async () => {
      if (!pin.value || !/^\d{4,6}$/.test(pin.value)) {
        alert('PIN must be 4-6 digits')
        return
      }

      if (pin.value !== confirmPin.value) {
        alert('PINs do not match')
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
        const response = await authService.setPin(pin.value)

        if (response.success) {
          // Show success toast
          if (window.showToast) {
            window.showToast('PIN set successfully', 'success');
          } else {
            alert('PIN set successfully')
          }
          pin.value = '';
          confirmPin.value = '';
          // After setting PIN, redirect to dashboard
          router.push('/dashboard')
        } else {
          alert(response.message || 'Failed to set PIN')
        }
      } catch (error) {
        console.error('Set PIN error:', error);
        alert(`Failed to set PIN: ${error.message || 'An error occurred while setting PIN'}`);
      } finally {
        loading.value = false;
      }
    }

    return {
      loading,
      pin,
      confirmPin,
      setPin
    }
  }
}
</script>

<style scoped>
.set-pin-container {
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

.icon-circle {
  width: 60px;
  height: 60px;
}
</style>
