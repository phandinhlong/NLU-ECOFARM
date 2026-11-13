<script setup lang="ts">
withDefaults(
  defineProps<{
    label: string
    variant?: 'success' | 'warning' | 'danger' | 'info' | 'muted'
  }>(),
  { variant: 'muted' },
)
</script>

<template>
  <span class="ef-badge-soft" :class="`ef-badge-${variant}`">{{ label }}</span>
</template>
