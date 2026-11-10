import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Frontend chạy ở http://localhost:5173 và chuyển mọi yêu cầu /api sang backend (cổng 8080),
// nên không bị lỗi CORS khi phát triển.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
});
