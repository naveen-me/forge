<template>
  <div class="toast-container position-fixed bottom-0 end-0 p-3" style="z-index: 11">
    <div
      v-for="toast in toasts"
      :key="toast.id"
      class="toast"
      :class="`bg-${toast.type} text-white`"
      role="alert"
      aria-live="assertive"
      aria-atomic="true"
    >
      <div class="d-flex">
        <div class="toast-body">
          {{ toast.message }}
        </div>
        <button
          type="button"
          class="btn-close btn-close-white me-2 m-auto"
          @click="removeToast(toast.id)"
        ></button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'

export default {
  name: 'Toast',
  setup() {
    const toasts = ref([])
    let toastId = 0

    const addToast = (message, type = 'info') => {
      const id = toastId++
      toasts.value.push({ id, message, type })

      // Auto remove toast after 5 seconds
      setTimeout(() => {
        removeToast(id)
      }, 5000)
    }

    const removeToast = (id) => {
      const index = toasts.value.findIndex(toast => toast.id === id)
      if (index !== -1) {
        toasts.value.splice(index, 1)
      }
    }

    // Make toast functions globally available
    onMounted(() => {
      window.showToast = addToast
    })

    return {
      toasts,
      removeToast
    }
  }
}
</script>

<style scoped>
.toast-container {
  z-index: 1050;
}

.bg-success {
  background-color: #28a745 !important;
}

.bg-danger {
  background-color: #dc3545 !important;
}

.bg-warning {
  background-color: #ffc107 !important;
  color: #212529 !important;
}

.bg-info {
  background-color: #17a2b8 !important;
}
</style>