import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

import Components from 'unplugin-vue-components/vite'
import { BootstrapVueNextResolver } from 'bootstrap-vue-next/resolvers'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const backend = new URL(env.VITE_BACKEND || 'http://localhost:8080/ecofarm_backend-1.0-SNAPSHOT')
  const backendContextPath = backend.pathname.replace(/\/+$/, '')

  return {
    plugins: [
      vue(),
      vueDevTools(),
      Components({ resolvers: [BootstrapVueNextResolver()] })
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      port: 5174,
      // Dev gọi /api qua chính Vite rồi proxy sang backend, nên không phụ thuộc
      // origin/port của dev server và không bị CORS chặn (backend chỉ cho 5173).
      proxy: {
        '/api': {
          target: backend.origin,
          changeOrigin: false,
          rewrite: (path) => `${backendContextPath}${path}`,
        },
      },
    },
  }
})
