<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
  breadcrumb?: string[]
}>()
</script>

<template>
  <div class="ef-page-head">
    <div>
      <p v-if="breadcrumb?.length" class="ef-page-sub mb-1 text-uppercase" style="letter-spacing: 0.08em; font-size: 0.7rem; font-weight: 700">
        <template v-for="(item, index) in breadcrumb" :key="item">
          <span>{{ item }}</span>
          <span v-if="index < breadcrumb.length - 1" class="mx-1 text-body-tertiary">/</span>
        </template>
      </p>
      <h1 class="ef-page-title">{{ title }}</h1>
      <p v-if="subtitle" class="ef-page-sub">{{ subtitle }}</p>
    </div>
    <div class="d-flex flex-wrap gap-2">
      <slot name="actions" />
    </div>
  </div>
</template>
