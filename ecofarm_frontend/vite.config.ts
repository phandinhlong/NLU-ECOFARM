import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

import Components from 'unplugin-vue-components/vite'
import { BootstrapVueNextResolver } from 'bootstrap-vue-next/resolvers'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    Components({ resolvers: [BootstrapVueNextResolver()] }),
    VitePWA({
      registerType: 'autoUpdate',
      manifest: {
        name: 'EcoFarm',
        short_name: 'EcoFarm',
        start_url: '.',
        display: 'standalone',
      },
      workbox: {
        // Cache only the built app shell. Never cache authenticated API responses or
        // private images into a shared cache; those stay network-only.
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
        navigateFallbackDenylist: [/^\/api\//, /\/ecofarm_backend-/],
        runtimeCaching: [],
      },
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
})
