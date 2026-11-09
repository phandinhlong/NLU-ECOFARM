<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
const title = (route.meta.title as string | undefined) ?? String(route.name ?? '')
</script>

<template>
  <div>
    <h2 class="mb-3">{{ title }}</h2>
    <p>Đang xây dựng: {{ title }} ({{ route.path }})</p>
  </div>
</template>
