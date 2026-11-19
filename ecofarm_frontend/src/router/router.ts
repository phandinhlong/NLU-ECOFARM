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
    adminArea?: boolean
    farmerArea?: boolean
  }
}

/** Các role được phép vào khu vực quản trị */
const ADMIN_ROLES = ['ADMIN']
const FARMER_HOME = 'farmer-home'
const NON_ADMIN_HOME = 'batch-activation'

const isFarmerOnly = (user: User) =>
  user.role.includes('FARMER') && !user.role.includes('ENGINEER') && !user.role.includes('ADMIN')

const homeFor = (user: User) =>
  isFarmerOnly(user) ? FARMER_HOME : user.role.includes('ADMIN') ? 'dashboard' : NON_ADMIN_HOME

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
      path: '/trace/:code?',
      name: 'customer-trace',
      component: () => import('@/views/trace/CustomerTraceView.vue'),
      meta: { title: 'Truy xuất nguồn gốc' },
    },
    {
      path: '/t/:code',
      name: 'customer-trace-short',
      component: () => import('@/views/trace/CustomerTraceView.vue'),
      meta: { title: 'Truy xuất nguồn gốc' },
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
      meta: { requiresAuth: true, adminArea: true },
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
          path: 'qc-harvest',
          name: 'qc-harvest',
          component: () => import('@/views/qc/QcHarvestView.vue'),
          meta: { title: 'Kiểm định chất lượng & Thu hoạch' },
        },
        {
          path: 'batches/:id/qc',
          name: 'batch-qc',
          component: () => import('@/views/qc/QcHarvestView.vue'),
          meta: { title: 'Kiểm định chất lượng & Thu hoạch' },
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
      path: '/farmer',
      component: () => import('@/layouts/FarmerLayout.vue'),
      meta: { requiresAuth: true, farmerArea: true },
      children: [
        {
          path: '',
          name: 'farmer-home',
          component: () => import('@/views/farmer/FarmerDashboardView.vue'),
          meta: { title: 'Lô của tôi' },
        },
        {
          path: 'batches/:id',
          name: 'farmer-batch-detail',
          component: () => import('@/views/farmer/FarmerBatchDetailView.vue'),
          meta: { title: 'Chi tiết lô' },
        },
      ],
    },
    {
      path: '/batches/:id/log',
      component: () => import('@/layouts/LogLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'batch-log',
          component: () => import('@/views/batches/BatchLogView.vue'),
          meta: { title: 'Ghi nhật ký canh tác' },
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
    return { name: homeFor(user) }
  }

  const inAdminArea = to.matched.some((route) => route.meta.adminArea)
  const inFarmerArea = to.matched.some((route) => route.meta.farmerArea)

  if (user && isFarmerOnly(user) && inAdminArea) {
    return { name: FARMER_HOME }
  }

  if (user && !isFarmerOnly(user) && inFarmerArea) {
    return { name: homeFor(user) }
  }

  const requiredRoles = to.matched.flatMap((route) => route.meta.roles ?? [])
  if (user && !hasRequiredRole(user, requiredRoles)) {
    return { name: homeFor(user) }
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} | EcoFarm` : 'EcoFarm'
})

export default router
