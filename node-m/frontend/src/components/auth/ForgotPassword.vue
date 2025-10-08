<template>
  <div class="forgot-password-container min-vh-100 d-flex align-items-center">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-lg-6">
          <div class="card shadow-lg border-0 rounded-4">
            <div class="card-body p-5">
              <div class="text-center mb-5">
                <h2 class="fw-bold">Forgot Password</h2>
                <p class="text-muted">Enter your email to reset your password</p>
              </div>

              <div v-if="!otpSent">
                <form @submit.prevent="sendOtp">
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

                  <div class="d-grid mb-4">
                    <button type="submit" class="btn btn-primary btn-lg rounded-pill" :disabled="loading">
                      <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                      <span v-if="loading">Sending OTP...</span>
                      <span v-else>Send OTP</span>
                    </button>
                  </div>
                </form>
              </div>

              <div v-else>
                <form @submit.prevent="resetPassword">
                  <div class="mb-4">
                    <label for="otp" class="form-label fw-semibold">OTP</label>
                    <div class="input-group">
                      <span class="input-group-text bg-light border-end-0">
                        <i class="bi bi-shield-lock"></i>
                      </span>
                      <input
                        id="otp"
                        name="otp"
                        type="text"
                        class="form-control border-start-0"
                        v-model="otp"
                        placeholder="Enter 6-digit OTP"
                      />
                    </div>
                  </div>

                  <div class="mb-4">
                    <label for="newPassword" class="form-label fw-semibold">New Password</label>
                    <div class="input-group">
                      <span class="input-group-text bg-light border-end-0">
                        <i class="bi bi-lock"></i>
                      </span>
                      <input
                        id="newPassword"
                        name="newPassword"
                        type="password"
                        class="form-control border-start-0"
                        v-model="newPassword"
                        placeholder="Create a new password"
                      />
                    </div>
                  </div>

                  <div class="d-grid mb-4">
                    <button type="submit" class="btn btn-primary btn-lg rounded-pill" :disabled="loading">
                      <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                      <span v-if="loading">Resetting Password...</span>
                      <span v-else>Reset Password</span>
                    </button>
                  </div>
                </form>
              </div>

              <div class="text-center">
                <router-link to="/login" class="text-decoration-none fw-semibold">
                  <i class="bi bi-arrow-left me-1"></i> Back to Login
                </router-link>
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
  name: 'ForgotPassword',
  setup() {
    const router = useRouter()

    const otpSent = ref(false)
    const loading = ref(false)
    const email = ref('')
    const otp = ref('')
    const newPassword = ref('')

    const sendOtp = async () => {
      if (!email.value || !/\S+@\S+\.\S+/.test(email.value)) {
        alert('Email address is invalid')
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
        const response = await authService.sendOTP(email.value)

        if (response.success) {
          otpSent.value = true
          // Show success toast
          if (window.showToast) {
            window.showToast('OTP sent to your email', 'success');
          } else {
            alert('OTP sent to your email')
          }
        } else {
          // Show error toast
          if (window.showToast) {
            window.showToast(response.message || 'Failed to send OTP', 'danger');
          } else {
            alert(response.message || 'Failed to send OTP')
          }
        }
      } catch (error) {
        console.error('Send OTP error:', error);
        // Show toast message instead of alert
        if (window.showToast) {
          window.showToast(`Failed to send OTP: ${error.message || 'An error occurred while sending OTP'}`, 'danger');
        } else {
          alert(`Failed to send OTP: ${error.message || 'An error occurred while sending OTP'}`);
        }
      } finally {
        loading.value = false;
      }
    }

    const resetPassword = async () => {
      if (!otp.value || !/^\d{6}$/.test(otp.value)) {
        alert('OTP must be 6 digits')
        return
      }

      if (!newPassword.value || newPassword.value.length < 6) {
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
        const response = await authService.verifyOTP(email.value, otp.value)

        if (response.success) {
          const resetResponse = await authService.resetPassword(email.value, newPassword.value)

          if (resetResponse.success) {
            // Show success toast
            if (window.showToast) {
              window.showToast('Password reset successful', 'success');
            } else {
              alert('Password reset successful')
            }
            nextTick(() => {
              router.push('/login')
            });
          } else {
            // Show error toast
            if (window.showToast) {
              window.showToast(resetResponse.message || 'Failed to reset password', 'danger');
            } else {
              alert(resetResponse.message || 'Failed to reset password')
            }
          }
        } else {
          alert(response.message || 'Invalid OTP')
        }
      } catch (error) {
        console.error('Password reset error:', error);
        // Show toast message instead of alert
        if (window.showToast) {
          window.showToast(`Password reset failed: ${error.message || 'An error occurred during password reset'}`, 'danger');
        } else {
          alert(`Password reset failed: ${error.message || 'An error occurred during password reset'}`);
        }
      } finally {
        loading.value = false;
      }
    }

    return {
      otpSent,
      loading,
      email,
      otp,
      newPassword,
      sendOtp,
      resetPassword
    }
  }
}
</script>

<style scoped>
.forgot-password-container {
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
