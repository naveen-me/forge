<template>
  <div class="dashboard-container min-vh-100">
    <!-- Header -->
    <div class="bg-white shadow-sm">
      <div class="container-fluid px-4 py-3 d-flex justify-content-between align-items-center">
        <h1 class="h2 fw-bold text-dark">Dashboard</h1>
        <button @click="logout" class="btn btn-danger rounded-pill px-4">
          <i class="bi bi-box-arrow-right me-1"></i> Logout
        </button>
      </div>
    </div>

    <!-- Main Content -->
    <div class="container-fluid px-4 py-5">
      <div class="row g-5">


        <!-- Change PIN Card -->
        <div class="col-md-6">
          <div class="card shadow-lg border-0 rounded-4">
            <div class="card-body p-5">
              <div class="text-center mb-4">
                <div class="icon-circle bg-success text-white d-inline-flex align-items-center justify-content-center rounded-circle mb-3">
                  <i class="bi bi-arrow-repeat fs-3"></i>
                </div>
                <h2 class="h4 fw-bold text-dark">Change PIN</h2>
                <p class="text-muted">Update your existing PIN</p>
              </div>

              <div v-if="!hasPin">
                <div class="alert alert-info rounded-3" role="alert">
                  <i class="bi bi-info-circle me-2"></i>
                  You haven't set a PIN yet. Please set one using the form on the left.
                </div>
              </div>

              <form v-else @submit.prevent="changePin">
                <div class="mb-4">
                  <label for="currentPin" class="form-label fw-semibold">Current PIN</label>
                  <div class="input-group">
                    <span class="input-group-text bg-light border-end-0">
                      <i class="bi bi-key"></i>
                    </span>
                    <input
                      id="currentPin"
                      name="currentPin"
                      type="password"
                      class="form-control border-start-0"
                      v-model="currentPin"
                      placeholder="Enter current PIN"
                    />
                  </div>
                </div>

                <div class="mb-4">
                  <label for="newPin" class="form-label fw-semibold">New PIN (4-6 digits)</label>
                  <div class="input-group">
                    <span class="input-group-text bg-light border-end-0">
                      <i class="bi bi-shield-lock"></i>
                    </span>
                    <input
                      id="newPin"
                      name="newPin"
                      type="password"
                      class="form-control border-start-0"
                      v-model="newPin"
                      placeholder="Enter new PIN"
                    />
                  </div>
                </div>

                <div class="mb-4">
                  <label for="confirmNewPin" class="form-label fw-semibold">Confirm New PIN</label>
                  <div class="input-group">
                    <span class="input-group-text bg-light border-end-0">
                      <i class="bi bi-shield-check"></i>
                    </span>
                    <input
                      id="confirmNewPin"
                      name="confirmNewPin"
                      type="password"
                      class="form-control border-start-0"
                      v-model="confirmNewPin"
                      placeholder="Confirm new PIN"
                    />
                  </div>
                </div>

                <div class="d-grid">
                  <button type="submit" class="btn btn-success btn-lg rounded-pill" :disabled="changePinLoading">
                    <span v-if="changePinLoading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    <span v-if="changePinLoading">Changing PIN...</span>
                    <span v-else>Change PIN</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import authService from '../../services/authService'

export default {
  name: 'Dashboard',
  setup() {
    const router = useRouter()

    const loading = ref(false)
    const changePinLoading = ref(false)
    const hasPin = ref(false)
    const currentPin = ref('')
    const newPin = ref('')
    const confirmNewPin = ref('')

    // Check if user already has a PIN
    onMounted(async () => {
      if (!await authService.isAuthenticated()) {
        router.push('/login');
        return;
      }
      const userHasPin = await authService.hasPin();
      hasPin.value = userHasPin;
      // If user doesn't have a PIN, they should set one
      // If they do have a PIN, they can change it on this page
    })

    const changePin = async () => {
      if (newPin.value !== confirmNewPin.value) {
        alert('New PIN and confirmation do not match');
        return;
      }

      if (newPin.value.length < 4 || newPin.value.length > 6) {
        alert('PIN must be between 4 and 6 digits');
        return;
      }

      changePinLoading.value = true;
      try {
        // First validate the current PIN
        const validateResponse = await authService.validatePin(currentPin.value);
        if (!validateResponse.success) {
          alert('Current PIN is incorrect');
          return;
        }

        // Then set the new PIN
        const response = await authService.setPin(newPin.value);
        if (response.success) {
          alert('PIN changed successfully');
          // Reset form
          currentPin.value = '';
          newPin.value = '';
          confirmNewPin.value = '';
        } else {
          alert('Error changing PIN: ' + (response.message || 'Unknown error'));
        }
      } catch (error) {
        alert('Error changing PIN: ' + error.message);
      } finally {
        changePinLoading.value = false;
      }
    };

    const logout = async () => {
      try {
        await authService.logout();
        router.push('/login');
      } catch (error) {
        alert('Error logging out: ' + error.message);
      }
    };

    return {
      loading,
      changePinLoading,
      hasPin,
      currentPin,
      newPin,
      confirmNewPin,
      changePin,
      logout
    }
  }
}
</script>
<style scoped>
.dashboard-container {
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

.btn-success {
  background: linear-gradient(135deg, #28a745 0%, #1e7e34 100%);
  border: none;
  transition: all 0.3s ease;
}

.btn-success:hover {
  background: linear-gradient(135deg, #218838 0%, #19692c 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(40, 167, 69, 0.3);
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