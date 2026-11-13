<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
  breadcrumb?: string[]
}>()
</script>

<template>
  <div class="d-flex flex-wrap gap-3 align-items-start justify-content-between mb-3">
    <div>
      <p v-if="breadcrumb?.length" class="ef-breadcrumb text-uppercase fw-bold text-body-secondary mb-1">
        <template v-for="(item, index) in breadcrumb" :key="item">
          <span>{{ item }}</span>
          <span v-if="index < breadcrumb.length - 1" class="mx-1 text-body-tertiary">/</span>
        </template>
      </p>
      <h1 class="h4 fw-bold text-body mb-1">{{ title }}</h1>
      <p v-if="subtitle" class="small text-body-secondary mb-0">{{ subtitle }}</p>
    </div>
    <div class="d-flex flex-wrap gap-2">
      <slot name="actions" />
    </div>
  </div>
</template>

<style scoped>
.ef-breadcrumb {
  font-size: 0.7rem;
  letter-spacing: 0.08em;
}
</style>
