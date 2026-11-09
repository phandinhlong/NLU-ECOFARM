<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
const title = (route.meta.title as string | undefined) ?? String(route.name ?? '')
</script>

<template>
  <BContainer class="d-flex align-items-center justify-content-center vh-100 p-4">
    <BCard>
      <template #header>Placeholder: {{ title }}</template>
      <div>
        <p>Đang xây dựng: {{ title }} ({{ route.path }})</p>
      </div>
    </BCard>
  </BContainer>
</template>
