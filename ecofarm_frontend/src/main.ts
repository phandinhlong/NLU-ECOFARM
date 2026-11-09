import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import { createBootstrap } from 'bootstrap-vue-next'
import './styles/index.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { VueQueryPlugin } from '@tanstack/vue-query'
import App from './App.vue'
import router from './router'
import { queryClient } from './plugins/vue-query'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(createBootstrap())
app.use(VueQueryPlugin, { queryClient })
app.mount('#app')
