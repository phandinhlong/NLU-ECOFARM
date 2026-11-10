<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppLoadingSkeleton from '@/components/common/AppLoadingSkeleton.vue'

const isInitializing = ref(true)

onMounted(() => {
  setTimeout(() => {
    isInitializing.value = false
  }, 500)
})
</script>

<template>
  <AppLoadingSkeleton v-if="isInitializing" />
  <router-view v-else />
</template>
