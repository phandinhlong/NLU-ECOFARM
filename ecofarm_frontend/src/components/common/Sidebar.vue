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
    ],
  },
]
</script>

<template>
  <aside class="ef-sidebar bg-body border-end d-flex flex-column">
    <RouterLink
      to="/dashboard"
      class="ef-brand d-flex align-items-center gap-2 px-3 py-3 border-bottom text-decoration-none"
    >
      <span class="ef-brand-mark rounded-2 bg-primary text-white d-grid fw-bold">EF</span>
      <span>
        <span class="d-block fw-bold small text-body lh-sm">EcoFarm</span>
        <span class="ef-brand-sub d-block text-uppercase text-body-secondary">Truy xuất nguồn gốc</span>
      </span>
    </RouterLink>

    <nav class="ef-nav px-2 py-2 overflow-auto">
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
</template>

<style scoped>
.ef-sidebar {
  width: 248px;
  flex: 0 0 248px;
}

.ef-brand-mark {
  width: 34px;
  height: 34px;
  font-size: 0.95rem;
  flex-shrink: 0;
  place-items: center;
}

.ef-brand-sub {
  font-size: 0.58rem;
  letter-spacing: 0.14em;
  font-weight: 600;
}

.ef-nav-title {
  padding: 0.9rem 0.55rem 0.35rem;
  font-size: 0.65rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  font-weight: 700;
  color: var(--bs-secondary-color);
}

.ef-nav-link {
  display: flex;
  align-items: center;
  padding: 0.5rem 0.6rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  line-height: 1.3;
  color: var(--bs-body-color);
  text-decoration: none;
}

.ef-nav-link:hover {
  background: var(--ef-bg-secondary);
  color: var(--ef-primary-700);
}

.ef-nav-link.active {
  background: var(--ef-bg-secondary);
  color: var(--ef-primary-700);
  font-weight: 600;
}

@media (max-width: 991.98px) {
  .ef-sidebar {
    width: 100%;
    flex: 0 0 auto;
    border-bottom: 1px solid var(--bs-border-color);
  }
}
</style>
