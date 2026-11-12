<script setup lang="ts">
import BrandHeader from '@/components/common/BrandHeader.vue'
</script>

<template>
  <BContainer class="d-flex align-items-center justify-content-center min-vh-100 p-4">
    <BCard class="w-100 p-4 p-sm-5 border-0 shadow" style="max-width: 420px">
      <BrandHeader title="Kích hoạt thành công" subtitle="Hệ thống truy xuất nguồn gốc dưa lưới" />

      <div class="d-flex align-items-center justify-content-center rounded-circle bg-success-subtle text-success mx-auto my-4" style="width: 3.5rem; height: 3.5rem">
        <svg width="32" height="32" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24">
          <path d="M20 6 9 17l-5-5"></path>
        </svg>
      </div>

      <p class="text-center fw-medium mb-4">Kích hoạt tài khoản thành công!</p>

      <BButton to="/login" variant="primary" class="w-100 small shadow-sm">Đăng nhập ngay</BButton>
    </BCard>
  </BContainer>
</template>
