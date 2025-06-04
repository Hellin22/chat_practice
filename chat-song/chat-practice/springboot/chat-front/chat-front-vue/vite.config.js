import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      // SockJS를 위한 별칭 추가
      'sockjs-client': 'sockjs-client/dist/sockjs.min.js',
    }
  },
  optimizeDeps: {
    include: ['sockjs-client'],
  },
})