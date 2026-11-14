<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores'

const userStore = useUserStore()

const roles = computed(() => userStore.user?.role ?? [])
const canManageCatalog = computed(() =>
  roles.value.some((role) => role === 'ADMIN' || role === 'ENGINEER'),
)

const navSections = [
  {
    title: 'Tổng quan',
    items: [{ label: 'Tổng quan', to: '/dashboard' }],
  },
  {
    title: 'Quản lý danh mục',
    items: [
      { label: 'Nhà màng', to: '/greenhouses' },
      { label: 'Giống dưa', to: '/varieties' },
      { label: 'Chứng nhận VietGAP', to: '/certificates' },
      { label: 'Kích hoạt lô & In tem', to: '/batch-activation' },
    ],
  },
]
</script>

<template>
  <aside class="ef-sidebar flex-shrink-0 bg-body border-end d-flex flex-column">
    <RouterLink
      to="/dashboard"
      class="d-flex align-items-center gap-2 px-3 py-3 border-bottom text-decoration-none"
    >
      <span
        class="ef-brand-mark d-flex align-items-center justify-content-center flex-shrink-0 rounded-2 bg-primary text-white fw-bold"
      >
        EF
      </span>
      <span>
        <span class="d-block fw-bold small text-body lh-sm">EcoFarm</span>
        <span class="ef-brand-sub d-block text-uppercase text-body-secondary">
          Truy xuất nguồn gốc
        </span>
      </span>
    </RouterLink>

    <nav class="ef-nav px-2 py-2 overflow-auto">
      <template v-for="section in navSections" :key="section.title">
        <p class="ef-nav-title mb-0 px-2 pt-3 pb-1 text-uppercase fw-bold text-body-secondary">
          {{ section.title }}
        </p>
        <RouterLink
          v-for="item in section.items"
          :key="item.to"
          :to="item.to"
          class="ef-nav-item d-flex align-items-center px-2 py-2 rounded-2 text-decoration-none"
          active-class="active"
        >
          {{ item.label }}
        </RouterLink>
      </template>

      <template v-if="canManageCatalog">
        <p class="ef-nav-title mb-0 px-2 pt-3 pb-1 text-uppercase fw-bold text-body-secondary">
          Quản trị
        </p>
        <span class="ef-nav-item d-flex align-items-center px-2 py-2 text-body-tertiary">
          Nhật ký kiểm toán
        </span>
      </template>
    </nav>
  </aside>
</template>

<style scoped>
.ef-sidebar {
  width: 248px;
}

.ef-brand-mark {
  width: 34px;
  height: 34px;
  font-size: 0.95rem;
}

.ef-brand-sub {
  font-size: 0.58rem;
  letter-spacing: 0.14em;
}

.ef-nav-title {
  font-size: 0.65rem;
  letter-spacing: 0.12em;
}

.ef-nav-item {
  font-size: 0.875rem;
  line-height: 1.3;
  color: var(--bs-body-color);
}

.ef-nav-item:hover {
  background: var(--ef-bg-secondary);
  color: var(--ef-primary-700);
}

.ef-nav-item.active {
  background: var(--ef-bg-secondary);
  color: var(--ef-primary-700);
  font-weight: 600;
}

@media (max-width: 991.98px) {
  .ef-sidebar {
    width: 100%;
  }
}
</style>
