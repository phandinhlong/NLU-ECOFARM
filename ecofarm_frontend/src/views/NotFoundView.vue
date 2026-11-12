<template>
  <BContainer class="d-flex align-items-center justify-content-center vh-100 p-4">
    <BCard>
      <template #header>404 - Not Found</template>
      <div>
        <p>Không tìm thấy trang bạn yêu cầu.</p>
        <BButton to="/dashboard" variant="primary">Về trang chủ</BButton>
        <BButton to="/login" variant="outline-secondary" class="ms-2">Đăng nhập</BButton>
      </div>
    </BCard>
  </BContainer>
</template>
