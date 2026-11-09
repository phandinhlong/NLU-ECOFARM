<template>
  <div class="not-found">
    <h1>404</h1>
    <p>Không tìm thấy trang bạn yêu cầu.</p>
    <div class="links">
      <RouterLink to="/dashboard">Về trang chủ</RouterLink>
      <RouterLink to="/login">Đăng nhập</RouterLink>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  min-height: 100vh;
  padding: 1.5rem;
  text-align: center;
}

.not-found h1 {
  margin: 0;
  font-size: 4rem;
  color: #2f7d4f;
}

.not-found p {
  margin: 0;
  color: #5c6b62;
}

.links {
  display: flex;
  gap: 1rem;
}

.links a {
  color: #2f7d4f;
}
</style>
