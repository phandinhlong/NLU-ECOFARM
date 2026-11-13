<script setup lang="ts">
import BrandHeader from '@/components/common/BrandHeader.vue'
</script>

<template>
  <BContainer class="d-flex align-items-center justify-content-center min-vh-100 p-4">
    <BCard class="w-100 p-4 p-sm-5 border-0 shadow" style="max-width: 420px">
      <BrandHeader title="Kích hoạt thất bại" subtitle="Hệ thống truy xuất nguồn gốc dưa lưới" />

      <div class="d-flex align-items-center justify-content-center rounded-circle bg-danger-subtle text-danger mx-auto my-4" style="width: 3.5rem; height: 3.5rem">
        <svg width="32" height="32" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24">
          <path d="M18 6 6 18"></path>
          <path d="m6 6 12 12"></path>
        </svg>
      </div>

      <p class="text-center small text-body-secondary mb-4">
        Kích hoạt tài khoản thất bại. Token không hợp lệ hoặc đã hết hạn.
      </p>

      <BButton to="/login" variant="primary" class="w-100 small shadow-sm">Về trang đăng nhập</BButton>
      <p class="text-center fs-xs text-body-secondary mt-3 mb-0">
        Cần hỗ trợ?
        <a class="fw-medium link-primary link-underline-opacity-0 link-underline-opacity-100-hover" href="#">Liên hệ quản trị viên</a>
      </p>
    </BCard>
  </BContainer>
</template>
