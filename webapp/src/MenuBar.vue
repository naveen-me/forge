<template>
  <div class="d-flex flex-column align-items-center justify-content-between menu-bar vh-100" style="width: 60px;" ref="menuBar">
    <div class="d-flex flex-column">
      <!-- Dashboard -->
      <button 
        class="btn btn-link menu-icon m-0 p-0 py-3" 
        :class="{ 'active': $route.name === 'Dashboard' }"
        @click="$router.push('/')"
        data-bs-toggle="tooltip" data-bs-placement="right" data-bs-title="Dashboard"
      >
        <span class="material-symbols-outlined">{{ $route.name === 'Dashboard' ? 'dashboard' : 'dashboard' }}</span>
      </button>

      <!-- Media Library -->
      <button 
        class="btn btn-link menu-icon m-0 p-0 py-3" 
        :class="{ 'active': $route.name === 'MediaLibrary' }"
        @click="$router.push('/media-library')"
        data-bs-toggle="tooltip" data-bs-placement="right" data-bs-title="Media Library"
      >
        <span class="material-symbols-outlined">{{ $route.name === 'MediaLibrary' ? 'folder_open' : 'folder_open' }}</span>
      </button>

      <!-- Overlays -->
      <button 
        class="btn btn-link menu-icon m-0 p-0 py-3" 
        :class="{ 'active': $route.name === 'Overlays' }"
        @click="$router.push('/overlays')"
        data-bs-toggle="tooltip" data-bs-placement="right" data-bs-title="Overlays"
      >
        <span class="material-symbols-outlined">{{ $route.name === 'Overlays' ? 'layers' : 'layers' }}</span>
      </button>

      <!-- Ads -->
      <button 
        class="btn btn-link menu-icon m-0 p-0 py-3" 
        :class="{ 'active': $route.name === 'Ads' }"
        @click="$router.push('/ads')"
        data-bs-toggle="tooltip" data-bs-placement="right" data-bs-title="Ads"
      >
        <span class="material-symbols-outlined">{{ $route.name === 'Ads' ? 'lightbulb' : 'lightbulb' }}</span>
      </button>

      <!-- Status -->
      <button 
        class="btn btn-link menu-icon m-0 p-0 py-3" 
        :class="{ 'active': $route.name === 'Live' }"
        @click="$router.push('/live')"
        data-bs-toggle="tooltip" data-bs-placement="right" data-bs-title="Live Preview"
      >
        <span class="material-symbols-outlined">{{ $route.name === 'Live' ? 'sensors' : 'sensors' }}</span>
      </button>
      
      <!-- Calendar -->
      <button 
        class="btn btn-link menu-icon m-0 p-0 py-3" 
        :class="{ 'active': $route.name === 'Scheduler' }"
        @click="$router.push('/scheduler')"
        data-bs-toggle="tooltip" data-bs-placement="right" data-bs-title="Scheduler"
      >
        <span class="material-symbols-outlined">{{ $route.name === 'Scheduler' ? 'calendar_today' : 'calendar_today' }}</span>
      </button>
      
    </div>
    <div class="d-flex flex-column">
      <!-- User -->
      <button 
        class="btn btn-link menu-icon m-0 p-0 py-3" 
        :class="{ 'active': $route.name === 'UserProfile' }"
        @click="$router.push('/profile')"
        data-bs-toggle="tooltip" data-bs-placement="right" data-bs-title="Profile"
      >
        <span class="material-symbols-outlined">{{ $route.name === 'UserProfile' ? 'person' : 'person' }}</span>
      </button>

      <!-- Push Settings to Bottom -->
      <div class="mt-auto mb-3">
        <button 
          class="btn btn-link menu-icon" 
          :class="{ 'active': $route.name === 'Settings' }"
          @click="$router.push('/settings')"
          data-bs-toggle="tooltip" data-bs-placement="right" data-bs-title="Settings"
        >
          <span class="material-symbols-outlined">{{ $route.name === 'Settings' ? 'settings' : 'settings' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { onMounted, ref, inject } from 'vue'
import { useRoute } from 'vue-router'
import { Tooltip } from 'bootstrap'

export default {
  name: 'MenuBar',
  setup() {
    const menuBar = ref(null)
    const route = useRoute()
    const openModal = inject('openModal')
    
    onMounted(() => {
      // Initialize tooltips
      const tooltipTriggerList = menuBar.value.querySelectorAll('[data-bs-toggle="tooltip"]')
      const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new Tooltip(tooltipTriggerEl, {
        trigger: 'hover',
        delay: { show: 500, hide: 100 },
        customClass: 'custom-tooltip'
      }))
    })
    
    return {
      menuBar,
      route,
      openModal
    }
  }
}
</script>

<style>
/* Custom Tooltip Styles */
.custom-tooltip .tooltip-inner {
  background-color: #6c757d;
  color: #fff;
  border-radius: 0.25rem;
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
  font-weight: 600;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.bs-tooltip-end .tooltip-arrow::before,
.bs-tooltip-auto[data-popper-placement^="right"] .tooltip-arrow::before {
    border-right-color: #6c757d !important;
}
</style>

<style scoped>
.menu-bar {
  background-color: #f0f0f0; /* Windows default background color */
  overflow-y: hidden; /* Prevent scrolling */
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 1000;
}

/* For dark theme, we would invert the colors */
@media (prefers-color-scheme: dark) {
  .menu-bar {
    background-color: #2d2d2d; /* Dark theme background */
  }
}

.menu-icon {
  color: #5C5F62; /* Default icon color */
  transition: all 0.2s ease-in-out;
}

.menu-icon:hover {
  color: #000; /* Black on hover */
  filter: brightness(0);
}

/* In dark mode, icons should be light */
@media (prefers-color-scheme: dark) {
  .menu-icon {
    color: #BABEC3;
  }
  
  .menu-icon:hover {
    color: #fff;
    filter: brightness(1.5);
  }
}

/* Ensure all SVG icons are the same size */
.menu-icon i {
  font-size: 24px;
  width: 20px;
  height: 20px;
}

/* Active state */
.menu-icon:active,
.menu-icon.active {
  color: #000;
  filter: brightness(0);
}

@media (prefers-color-scheme: dark) {
  .menu-icon:active,
  .menu-icon.active {
    color: #fff;
    filter: brightness(1.5);
  }
}
</style>
