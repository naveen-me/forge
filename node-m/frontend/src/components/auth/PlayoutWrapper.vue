<template>
  <div class="min-vh-100 d-flex">
    <!-- Menu Bar -->
    <MenuBar />

    <!-- Main Content Area -->
    <div class="flex-grow-1 d-flex flex-column" style="margin-left: 60px;">
      <!-- Header -->
      <div class="bg-white shadow-sm">
        <div class="d-flex align-items-center justify-content-between px-4 py-3">
          <h2 class="m-0 h5 fw-medium">Tarva Engine Playout System</h2>
          <div class="d-flex align-items-center">
            <button class="btn btn-outline-primary me-2 rounded-pill" @click="goToDashboard">
              <i class="bi bi-speedometer2 me-1"></i> Dashboard
            </button>
            <button class="btn btn-outline-danger rounded-pill" @click="logout">
              <i class="bi bi-box-arrow-right me-1"></i> Logout
            </button>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div class="flex-grow-1 overflow-auto">
        <MainContent />
      </div>
    </div>
  </div>
</template>

<script>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MainContent from '../MainContent.vue'
import MenuBar from '../MenuBar.vue'
import authService from '../../services/authService'

export default {
  name: 'PlayoutWrapper',
  components: {
    MainContent,
    MenuBar
  },
  setup() {
    const router = useRouter()

    onMounted(() => {
      if (!authService.isAuthenticated()) {
        router.push('/login');
      }
    });

    const goToDashboard = () => {
      router.push('/dashboard')
    }

    const logout = () => {
      authService.logout()
      router.push('/login')
    }

    return {
      goToDashboard,
      logout
    }
  }
}
</script>