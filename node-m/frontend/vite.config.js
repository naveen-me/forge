import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:3001',
        changeOrigin: true,
        secure: false,
      }
    }
  },
  // Base path for production builds to work in Electron
  base: './', // Use relative paths for Electron compatibility
  build: {
    outDir: 'dist', // Ensure build output goes to dist folder
    assetsDir: 'assets', // Place assets in a subfolder
    rollupOptions: {
      output: {
        // Ensure consistent file naming for Electron
        entryFileNames: `assets/[name].[hash].js`,
        chunkFileNames: `assets/[name].[hash].js`,
        assetFileNames: `assets/[name].[hash].[ext]`
      }
    }
  }
})
