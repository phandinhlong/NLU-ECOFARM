import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import { createBootstrap } from 'bootstrap-vue-next'
import './styles/index.css'
import '@fortawesome/fontawesome-svg-core/styles.css'
import '@/plugins/fontawesome'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { VueQueryPlugin } from '@tanstack/vue-query'
import { config } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import App from './App.vue'
import router from './router/router.ts'
import { queryClient } from './plugins/vue-query'

config.autoAddCss = false

const app = createApp(App)
app.component('FontAwesomeIcon', FontAwesomeIcon)
app.use(createPinia())
app.use(router)
app.use(createBootstrap())
app.use(VueQueryPlugin, { queryClient })
app.mount('#app')
