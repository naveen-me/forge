import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:3001',
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // Ensure authorization header is preserved
            if (req.headers.authorization) {
              proxyReq.setHeader('authorization', req.headers.authorization);
            }
          });
          return proxy;
        }
      },
      '/auth': {
        target: 'http://localhost:3001',
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // Ensure authorization header is preserved
            if (req.headers.authorization) {
              proxyReq.setHeader('authorization', req.headers.authorization);
            }
          });
          return proxy;
        }
      },
    },
  },
});