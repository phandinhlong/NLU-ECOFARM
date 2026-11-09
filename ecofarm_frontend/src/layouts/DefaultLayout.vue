<template>
  <div class="d-flex min-vh-100">
    <BSidebar
      visible
      mobile
      shadow
      bg-variant="dark"
      text-variant="white"
      class="d-flex flex-column p-3" style="width: 220px; min-height: 100vh;">
      <h1 class="mb-4 text-white" style="font-size: 1.25rem;">EcoFarm</h1>
      <BNav vertical class="flex-column gap-1">
        <BNavItem to="/dashboard" active-class="bg-primary text-white">
          Tổng quan
        </BNavItem>
        <BNavItem to="/farms" active-class="bg-primary text-white">
          Trang trại
        </BNavItem>
        <BNavItem to="/greenhouses" active-class="bg-primary text-white">
          Nhà màng
        </BNavItem>
      </BNav>
    </BSidebar>

    <main class="main p-4" style="flex: 1;">
      <RouterView />
    </main>
  </div>
</template>
