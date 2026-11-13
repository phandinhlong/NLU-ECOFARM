<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores'
import { useLogoutMutation } from '@/composables/useAuth'

const route = useRoute()
const userStore = useUserStore()
const { mutate: logout, isPending: isLoggingOut } = useLogoutMutation()

const roles = computed(() => userStore.user?.role ?? [])
const canManageCatalog = computed(() =>
  roles.value.some((role) => role === 'ADMIN' || role === 'ENGINEER'),
)
const displayName = computed(() => userStore.user?.fullName || userStore.user?.email || 'Người dùng')
const initials = computed(() =>
  displayName.value
    .split(' ')
    .filter(Boolean)
    .slice(-2)
    .map((part) => part.charAt(0).toUpperCase())
    .join(''),
)
const currentLabel = computed(() => (route.meta.title as string | undefined) ?? 'Tổng quan')

const navSections = computed(() => [
  {
    title: 'Tổng quan',
    items: [{ label: 'Tổng quan', to: '/dashboard', name: 'dashboard' }],
  },
  {
    title: 'Quản lý danh mục',
    items: [
      { label: 'Nhà màng', to: '/greenhouses', name: 'greenhouses' },
      { label: 'Giống dưa', to: '/varieties', name: 'varieties' },
      { label: 'Chứng nhận VietGAP', to: '/certificates', name: 'certificates' },
    ],
  },
])
</script>

<template>
  <div class="ef-admin">
    <aside class="ef-sidebar">
      <RouterLink to="/dashboard" class="ef-brand">
        <span class="ef-brand-mark">EF</span>
        <span>
          <span class="ef-brand-name d-block">EcoFarm</span>
          <span class="ef-brand-sub">Truy xuất nguồn gốc</span>
        </span>
      </RouterLink>

      <nav class="ef-nav">
        <template v-for="section in navSections" :key="section.title">
          <p class="ef-nav-title mb-0">{{ section.title }}</p>
          <RouterLink
            v-for="item in section.items"
            :key="item.to"
            :to="item.to"
            class="ef-nav-link"
            active-class="active"
          >
            {{ item.label }}
          </RouterLink>
        </template>

        <template v-if="canManageCatalog">
          <p class="ef-nav-title mb-0">Quản trị</p>
          <span class="ef-nav-link text-body-tertiary">Nhật ký kiểm toán</span>
        </template>
      </nav>
    </aside>

    <div class="ef-main">
      <header class="ef-topbar">
        <span class="ef-topbar-brand">EcoFarm LEDGER</span>
        <span class="ef-topbar-sep">›</span>
        <span class="ef-topbar-current">{{ currentLabel }}</span>

        <div class="ef-user">
          <span class="ef-avatar">{{ initials }}</span>
          <span class="d-none d-sm-block">
            <span class="ef-user-name d-block">{{ displayName }}</span>
            <span class="ef-user-role d-block">{{ roles.join(' · ') || 'Người dùng' }}</span>
          </span>
          <BButton
            size="sm"
            variant="outline-secondary"
            :disabled="isLoggingOut"
            @click="logout()"
          >
            Đăng xuất
          </BButton>
        </div>
      </header>

      <main class="ef-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>
