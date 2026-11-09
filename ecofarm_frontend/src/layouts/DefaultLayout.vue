<template>
  <div class="default-layout">
    <aside class="sidebar">
      <h1 class="brand">EcoFarm</h1>
      <nav class="nav">
        <RouterLink to="/dashboard">Tổng quan</RouterLink>
        <RouterLink to="/farms">Trang trại</RouterLink>
        <RouterLink to="/greenhouses">Nhà màng</RouterLink>
      </nav>
    </aside>
    <main class="main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.default-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
  padding: 1.5rem 1rem;
  background: #1f3d2c;
  color: #fff;
}

.brand {
  margin: 0 0 1.5rem;
  font-size: 1.25rem;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.nav a {
  padding: 0.6rem 0.75rem;
  border-radius: 6px;
  color: #cfe3d6;
  text-decoration: none;
}

.nav a:hover {
  background: #2c5440;
}

.nav a.router-link-active {
  background: #2f7d4f;
  color: #fff;
}

.main {
  flex: 1;
  padding: 2rem;
}
</style>
