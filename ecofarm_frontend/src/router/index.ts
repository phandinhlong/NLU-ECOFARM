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
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { guestOnly: true, title: 'Đăng nhập' },
    },
    {
      path: '/activation/success',
      name: 'activation-success',
      component: () => import('@/views/ActivationSuccess.vue'),
      meta: { title: 'Kích hoạt thành công' },
    },
    {
      path: '/activation/failure',
      name: 'activation-failure',
      component: () => import('@/views/ActivationFailure.vue'),
      meta: { title: 'Kích hoạt thất bại' },
    },
    {
      path: '/',
      component: () => import('@/layouts/PublicLayout.vue'),
      meta: { guestOnly: true },
      children: [
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
          component: () => import('@/views/catalog/GreenhouseView.vue'),
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

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} | EcoFarm` : 'EcoFarm'
})

export default router
