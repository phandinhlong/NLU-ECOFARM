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
      component: () => import('@/views/auth/ActivationSuccess.vue'),
      meta: { title: 'Kích hoạt thành công' },
    },
    {
      path: '/activation/failure',
      name: 'activation-failure',
      component: () => import('@/views/auth/ActivationFailure.vue'),
      meta: { title: 'Kích hoạt thất bại' },
    },
    {
      path: '/forgot-password',
      name: 'forgot-password',
      component: () => import('@/views/auth/ForgotPasswordView.vue'),
      meta: { guestOnly: true, title: 'Quên mật khẩu' },
    },
    {
      path: '/reset-password',
      name: 'reset-password',
      component: () => import('@/views/auth/ResetPasswordView.vue'),
      meta: { guestOnly: true, title: 'Đặt lại mật khẩu' },
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
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: 'Tổng quan' },
        },
        {
          path: 'batches',
          name: 'batches',
          component: () => import('@/views/batches/BatchListView.vue'),
          meta: { title: 'Quản lý lô dưa' },
        },
        {
          path: 'batches/:id',
          name: 'batch-detail',
          component: () => import('@/views/batches/BatchDetailView.vue'),
          meta: { title: 'Chi tiết lô' },
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
        {
          path: 'varieties',
          name: 'varieties',
          component: () => import('@/views/catalog/VarietyView.vue'),
          meta: { title: 'Giống dưa' },
        },
        {
          path: 'certificates',
          name: 'certificates',
          component: () => import('@/views/catalog/CertificateView.vue'),
          meta: { title: 'Chứng nhận' },
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
