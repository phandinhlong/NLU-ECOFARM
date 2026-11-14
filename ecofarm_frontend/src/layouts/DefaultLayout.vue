<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from '@/components/common/Sidebar.vue'
import { useUserStore } from '@/stores'
import { useLogoutMutation } from '@/composables/useAuth'

const route = useRoute()
const userStore = useUserStore()
const { mutate: logout, isPending: isLoggingOut } = useLogoutMutation()

const roles = computed(() => userStore.user?.role ?? [])
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
</script>

<template>
  <div class="ef-admin d-flex vh-100 overflow-hidden bg-body-tertiary">
    <Sidebar class="h-100 overflow-y-auto" />

    <div class="ef-main flex-grow-1 d-flex flex-column h-100 overflow-hidden">
      <header
        class="ef-topbar flex-shrink-0 bg-body border-bottom d-flex flex-wrap align-items-center gap-2 gap-sm-3 px-3 px-lg-4 py-2"
      >
        <span class="fw-bold small text-body">EcoFarm LEDGER</span>
        <span class="text-body-tertiary">›</span>
        <span class="small text-body-secondary">{{ currentLabel }}</span>

        <div class="d-flex align-items-center gap-2 ms-auto">
          <span
            class="ef-avatar d-flex align-items-center justify-content-center flex-shrink-0 rounded-circle bg-primary-subtle text-primary fw-bold"
          >
            {{ initials }}
          </span>
          <span class="d-none d-sm-block lh-sm">
            <span class="ef-user-name d-block text-body">{{ displayName }}</span>
            <span class="ef-user-role d-block text-body-secondary">
              {{ roles.join(' · ') || 'Người dùng' }}
            </span>
          </span>
          <BButton size="sm" variant="outline-secondary" :disabled="isLoggingOut" @click="logout()">
            Đăng xuất
          </BButton>
        </div>
      </header>

      <main class="ef-content flex-grow-1 p-3 p-lg-4 overflow-y-auto">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.ef-main,
.ef-content {
  min-width: 0;
}

.ef-topbar {
  min-height: 60px;
}

.ef-avatar {
  width: 34px;
  height: 34px;
  font-size: 0.78rem;
}

.ef-user-name {
  font-size: 0.82rem;
  font-weight: 600;
}

.ef-user-role {
  font-size: 0.7rem;
}
</style>
