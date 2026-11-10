<script setup lang="ts">
defineProps<{ title?: string; subtitle?: string }>()
</script>

<template>
  <div class="text-center">
    <div class="d-flex align-items-center justify-content-center gap-2">
      <div
          class="d-flex align-items-center justify-content-center rounded-2 bg-primary-subtle text-primary"
          style="width: 2.25rem; height: 2.25rem"
      >
        <svg
            width="20"
            height="20"
            fill="none"
            stroke="currentColor"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            viewBox="0 0 24 24"
        >
          <path d="M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10Z"></path>
          <path d="M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12"></path>
        </svg>
      </div>
      <span class="fs-5 fw-bold text-primary">EcoFarm</span>
    </div>

    <h1 v-if="title" class="fs-4 fw-semibold mt-3 mb-1">{{ title }}</h1>
    <p v-if="subtitle" class="small mb-0">{{ subtitle }}</p>
  </div>
</template>