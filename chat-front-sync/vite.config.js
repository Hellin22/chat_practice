import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
   server: {
    proxy: {
      '/health': {
        target: 'http://localhost', // nginx가 80포트에서 리버스 프록시
        changeOrigin: true
      }
    }
  },
  plugins: [
    vue(),
    vueDevTools(),
  ],define: {
    global: 'window'  // global을 window로 대체하여 해결
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
