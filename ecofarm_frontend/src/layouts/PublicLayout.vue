<template>
  <div class="public-layout">
    <div class="shell">
      <h1 class="brand">EcoFarm</h1>
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
.public-layout {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 1.5rem;
  background: #f2f7f3;
}

.shell {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.brand {
  margin: 0 0 1.5rem;
  font-size: 1.5rem;
  color: #2f7d4f;
  text-align: center;
}
</style>
