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
const detailTarget = computed(() =>
  route.params.id ? { name: 'farmer-batch-detail', params: { id: route.params.id } } : null,
)
</script>

<template>
  <div class="ef-farmer min-vh-100 bg-body-tertiary d-flex flex-column">
    <header class="d-lg-none ef-farmer-header sticky-top bg-body border-bottom px-3 py-2">
      <div class="d-flex align-items-center gap-2 mx-auto" style="max-width: 640px">
        <span class="ef-brand-icon d-flex align-items-center justify-content-center rounded-3 bg-primary-subtle text-primary">
          <FontAwesomeIcon icon="leaf" aria-hidden="true" />
        </span>
        <span class="fw-bold">EcoFarm</span>
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

    <header class="d-none d-lg-flex ef-farmer-topbar sticky-top bg-body border-bottom px-4 py-2 align-items-center gap-3">
      <div class="d-flex align-items-center gap-2">
        <span class="ef-brand-icon d-flex align-items-center justify-content-center rounded-3 bg-primary-subtle text-primary">
          <FontAwesomeIcon icon="leaf" aria-hidden="true" />
        </span>
        <span class="fw-bold">EcoFarm</span>
      </div>
      <nav class="d-flex align-items-center gap-3 ms-4">
        <RouterLink :to="{ name: 'farmer-home' }" class="small fw-semibold text-decoration-none">
          Lô của tôi
        </RouterLink>
        <RouterLink
          v-if="detailTarget"
          :to="detailTarget"
          class="small fw-semibold text-decoration-none text-body-secondary"
        >
          Chi tiết lô
        </RouterLink>
      </nav>
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

    <main class="flex-grow-1 px-3 py-3 px-lg-4 py-lg-4 mx-auto w-100 ef-farmer-main">
      <RouterView />
    </main>

    <nav class="d-lg-none ef-farmer-nav bg-body border-top">
      <div class="d-flex mx-auto" style="max-width: 640px">
        <RouterLink
          :to="{ name: 'farmer-home' }"
          class="ef-farmer-nav-item flex-fill text-center text-primary fw-semibold text-decoration-none py-2"
        >
          <FontAwesomeIcon icon="seedling" aria-hidden="true" />
          <span class="d-block small">Lô của tôi</span>
        </RouterLink>
      </div>
    </nav>
  </div>
</template>

<style scoped>
.ef-farmer-main {
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

.ef-farmer-nav-item {
  min-height: 48px;
}

@media (max-width: 991.98px) {
  .ef-farmer-main {
    padding-bottom: 5rem;
  }

  .ef-farmer-nav {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 1030;
  }
}

@media (min-width: 992px) {
  .ef-farmer-main {
    max-width: 1100px;
  }
}
</style>
