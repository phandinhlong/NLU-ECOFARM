<template>
  <div
      class="position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center bg-white bg-opacity-90"
      style="z-index: 9999"
      role="status"
      aria-live="polite"
  >
    <div class="d-flex flex-column align-items-center gap-3">
      <BSpinner variant="primary" style="width: 3rem; height: 3rem" />
      <p class="mb-0 small text-primary-emphasis">Đang tải EcoFarm...</p>
    </div>
  </div>
</template>