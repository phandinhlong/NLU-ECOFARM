import { createRouter, createWebHistory } from 'vue-router'
import { authService } from '@/api/services'
import { useUserStore } from '@/stores'
import type { User } from '@/api/types/auth'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    guestOnly?: boolean
    roles?: string[]
    title?: string
  }
}

/** Các role được phép vào khu vực quản trị */
const ADMIN_ROLES = ['ADMIN', 'ENGINEER']
/** Trang mặc định cho tài khoản không có quyền quản trị */
const NON_ADMIN_HOME = 'batch-activation'

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
          meta: { title: 'Tổng quan', roles: ADMIN_ROLES },
        },
        {
          path: 'batch-activation',
          name: 'batch-activation',
          component: () => import('@/views/batch/BatchActivationView.vue'),
          meta: { title: 'Kích hoạt lô & In tem' },
        },
        {
          path: 'batches/:id/activation',
          name: 'batch-activation-detail',
          component: () => import('@/views/batch/BatchActivationView.vue'),
          meta: { title: 'Kích hoạt lô & In tem' },
        },
        {
          path: 'farms',
          name: 'farms',
          component: () => import('@/views/PlaceholderView.vue'),
          meta: { title: 'Trang trại', roles: ADMIN_ROLES },
        },
        {
          path: 'greenhouses',
          name: 'greenhouses',
          component: () => import('@/views/catalog/GreenhouseView.vue'),
          meta: { title: 'Nhà màng', roles: ADMIN_ROLES },
        },
        {
          path: 'varieties',
          name: 'varieties',
          component: () => import('@/views/catalog/VarietyView.vue'),
          meta: { title: 'Giống dưa', roles: ADMIN_ROLES },
        },
        {
          path: 'certificates',
          name: 'certificates',
          component: () => import('@/views/catalog/CertificateView.vue'),
          meta: { title: 'Chứng nhận', roles: ADMIN_ROLES },
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

const hasRequiredRole = (user: User, roles?: string[]) =>
  !roles?.length || user.role.some((role) => roles.includes(role))

let profileRequest: Promise<User | null> | null = null

/** Lấy hồ sơ người dùng hiện tại (dùng lại session cookie khi tải lại trang) */
const resolveUser = async (): Promise<User | null> => {
  const userStore = useUserStore()
  if (userStore.user) return userStore.user

  profileRequest ??= authService
    .getProfile()
    .then((response) => {
      userStore.setUser(response.data.data)
      return response.data.data
    })
    .catch(() => {
      userStore.clearUser()
      return null
    })
    .finally(() => {
      profileRequest = null
    })

  return profileRequest
}

router.beforeEach(async (to) => {
  const requiresAuth = to.matched.some((route) => route.meta.requiresAuth)
  const guestOnly = to.matched.some((route) => route.meta.guestOnly)

  if (!requiresAuth && !guestOnly) return true

  const user = await resolveUser()

  if (requiresAuth && !user) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (guestOnly && user) {
    return { name: hasRequiredRole(user, ADMIN_ROLES) ? 'dashboard' : NON_ADMIN_HOME }
  }

  const requiredRoles = to.matched.flatMap((route) => route.meta.roles ?? [])
  if (user && !hasRequiredRole(user, requiredRoles)) {
    return { name: NON_ADMIN_HOME }
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} | EcoFarm` : 'EcoFarm'
})

export default router
