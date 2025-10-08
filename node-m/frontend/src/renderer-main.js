import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'bootstrap-icons/font/bootstrap-icons.css'
import authService from './services/authService'

// Session flag to track PIN validation
window.pinValidatedThisSession = false;

const app = createApp(App)

// Register router
app.use(router)

// Handle PIN check from main process
if (window.electronAPI) {
  window.electronAPI.receive('check-pin-required', async () => {
    const pinRequired = await authService.isPinRequired();
    if (pinRequired) {
      // Navigate to PIN login page
      router.push('/pin-login');
    } else {
      // Show main window
      window.electronAPI.send('show-main-window');
    }
  });

  // Handle PIN validation from PIN login page
  window.electronAPI.receive('pin-validated', () => {
    // Set session flag when PIN is validated
    window.pinValidatedThisSession = true;
  });
} else {
  // Fallback behavior when not in Electron environment
  console.log("Running in browser environment - Electron API not available");
}

// Mount the app
app.mount('#app')