import { createRouter, createWebHistory } from 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    guestOnly?: boolean
    title?: string
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/PublicLayout.vue'),
      meta: { guestOnly: true },
      redirect: '/login',
      children: [
        {
          path: 'login',
          name: 'login',
          component: () => import('@/views/PlaceholderView.vue'),
          meta: { title: 'Đăng nhập' },
        },
        {
          path: 'register',
          name: 'register',
          component: () => import('@/views/PlaceholderView.vue'),
          meta: { title: 'Đăng ký' },
        },
      ],
    },
    {
      path: '/',
      component: () => import('@/layouts/DefaultLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: { name: 'dashboard' },
        },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/PlaceholderView.vue'),
          meta: { title: 'Tổng quan' },
        },
        {
          path: 'farms',
          name: 'farms',
          component: () => import('@/views/PlaceholderView.vue'),
          meta: { title: 'Trang trại' },
        },
        {
          path: 'greenhouses',
          name: 'greenhouses',
          component: () => import('@/views/PlaceholderView.vue'),
          meta: { title: 'Nhà màng' },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
    },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('accessToken')

  if (to.meta.requiresAuth && !token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.guestOnly && token) {
    return { name: 'dashboard' }
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} | EcoFarm` : 'EcoFarm'
})

export default router
