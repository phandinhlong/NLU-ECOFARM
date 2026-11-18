<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores'
import { useLogoutMutation } from '@/composables/useAuth'

const route = useRoute()
const userStore = useUserStore()
const { mutate: logout, isPending: isLoggingOut } = useLogoutMutation()

const displayName = computed(() => userStore.user?.fullName || userStore.user?.email || 'Người dùng')
const initials = computed(() =>
  displayName.value
    .split(' ')
    .filter(Boolean)
    .slice(-2)
    .map((part) => part.charAt(0).toUpperCase())
    .join(''),
)
const isFarmer = computed(() => {
  const roles = userStore.user?.role ?? []
  return roles.includes('FARMER') && !roles.includes('ENGINEER')
})
const detailTarget = computed(() =>
  isFarmer.value
    ? { name: 'farmer-batch-detail', params: { id: route.params.id } }
    : { name: 'batch-detail', params: { id: route.params.id } },
)
</script>

<template>
  <div class="ef-log min-vh-100 bg-body-tertiary d-flex flex-column">
    <header class="d-lg-none ef-log-header sticky-top bg-body border-bottom px-3 py-2">
      <div class="d-flex align-items-center gap-2 mx-auto" style="max-width: 640px">
        <RouterLink
          :to="detailTarget"
          class="btn btn-sm btn-outline-secondary"
          aria-label="Quay lại"
        >
          <FontAwesomeIcon icon="arrow-left" aria-hidden="true" />
        </RouterLink>
        <span class="fw-bold small text-truncate">Ghi nhật ký canh tác</span>
        <span
          class="ef-avatar ms-auto d-flex align-items-center justify-content-center rounded-circle bg-primary-subtle text-primary fw-bold"
          :title="displayName"
        >
          {{ initials }}
        </span>
        <BButton
          size="sm"
          variant="outline-secondary"
          :disabled="isLoggingOut"
          title="Đăng xuất"
          aria-label="Đăng xuất"
          @click="logout()"
        >
          <FontAwesomeIcon icon="right-from-bracket" aria-hidden="true" />
        </BButton>
      </div>
    </header>

    <header class="d-none d-lg-flex ef-log-topbar sticky-top bg-body border-bottom px-4 py-2 align-items-center gap-3">
      <RouterLink
        :to="detailTarget"
        class="btn btn-sm btn-outline-secondary"
        aria-label="Quay lại"
      >
        <FontAwesomeIcon icon="arrow-left" aria-hidden="true" />
      </RouterLink>
      <div class="d-flex align-items-center gap-2">
        <span class="ef-brand-icon d-flex align-items-center justify-content-center rounded-3 bg-primary-subtle text-primary">
          <FontAwesomeIcon icon="leaf" aria-hidden="true" />
        </span>
        <div class="lh-1">
          <div class="fw-bold">EcoFarm</div>
          <div class="small text-body-secondary">Ghi nhật ký canh tác</div>
        </div>
      </div>
      <div class="ms-auto d-flex align-items-center gap-2">
        <span class="small fw-semibold text-truncate">{{ displayName }}</span>
        <span
          class="ef-avatar d-flex align-items-center justify-content-center rounded-circle bg-primary-subtle text-primary fw-bold"
          :title="displayName"
        >
          {{ initials }}
        </span>
        <BButton size="sm" variant="outline-secondary" :disabled="isLoggingOut" @click="logout()">
          Đăng xuất
        </BButton>
      </div>
    </header>

    <main class="flex-grow-1 px-3 py-3 px-lg-4 py-lg-4 mx-auto w-100 ef-log-main">
      <RouterView />
    </main>

    <nav class="d-lg-none ef-log-nav bg-body border-top">
      <div class="d-flex mx-auto" style="max-width: 640px">
        <RouterLink
          :to="{ name: 'batch-log', params: { id: route.params.id } }"
          class="ef-log-nav-item flex-fill text-center text-primary fw-semibold text-decoration-none py-2"
        >
          <FontAwesomeIcon icon="clock-rotate-left" aria-hidden="true" />
          <span class="d-block small">Nhật ký</span>
        </RouterLink>
        <RouterLink
          :to="detailTarget"
          class="ef-log-nav-item flex-fill text-center text-body-secondary text-decoration-none py-2"
        >
          <FontAwesomeIcon icon="list-check" aria-hidden="true" />
          <span class="d-block small">Chi tiết lô</span>
        </RouterLink>
      </div>
    </nav>
  </div>
</template>

<style scoped>
.ef-log-main {
  max-width: 640px;
}

.ef-brand-icon {
  width: 36px;
  height: 36px;
}

.ef-avatar {
  width: 32px;
  height: 32px;
  font-size: 0.72rem;
}

.ef-log-nav-item {
  min-height: 48px;
}

@media (max-width: 991.98px) {
  .ef-log-main {
    padding-bottom: 5rem;
  }

  .ef-log-nav {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 1030;
  }
}

@media (min-width: 992px) {
  .ef-log-main {
    max-width: 1200px;
  }
}
</style>
