<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
const title = (route.meta.title as string | undefined) ?? String(route.name ?? '')
</script>

<template>
  <section class="placeholder">
    <h2>{{ title }}</h2>
    <p>Trang này đang được xây dựng, vui lòng quay lại sau.</p>
  </section>
</template>

<style scoped>
.placeholder {
  max-width: 640px;
}

.placeholder h2 {
  margin: 0 0 0.5rem;
  color: #1f3d2c;
}

.placeholder p {
  margin: 0;
  color: #5c6b62;
}
</style>
