<template>
  <div class="d-flex min-vh-100">
    <aside class="bg-dark text-white p-3 flex-shrink-0" style="width: 220px;">
      <h1 class="mb-4 fs-5">EcoFarm</h1>
      <BNav vertical class="gap-1">
        <BNavItem to="/dashboard" link-class="text-white" active-class="bg-primary">Tổng quan</BNavItem>
        <BNavItem to="/farms" link-class="text-white" active-class="bg-primary">Trang trại</BNavItem>
        <BNavItem to="/greenhouses" link-class="text-white" active-class="bg-primary">Nhà màng</BNavItem>
      </BNav>
    </aside>

    <main class="flex-grow-1 p-4">
      <RouterView />
    </main>
  </div>
</template>