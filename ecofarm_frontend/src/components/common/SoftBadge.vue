<script setup lang="ts">
withDefaults(
  defineProps<{
    label: string
    variant?: 'success' | 'warning' | 'danger' | 'info' | 'muted'
  }>(),
  { variant: 'muted' },
)

const variantClasses: Record<string, string> = {
  success: 'bg-success-subtle text-success-emphasis border border-success-subtle',
  warning: 'bg-warning-subtle text-warning-emphasis border border-warning-subtle',
  danger: 'bg-danger-subtle text-danger-emphasis border border-danger-subtle',
  info: 'bg-primary-subtle text-primary-emphasis border border-primary-subtle',
  muted: 'bg-secondary-subtle text-secondary-emphasis border border-secondary-subtle',
}
</script>

<template>
  <span class="badge fw-semibold" :class="variantClasses[variant]">{{ label }}</span>
</template>
