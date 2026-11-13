<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    page: number
    totalPages: number
    totalElements: number
    size: number
    unit: string
  }>(),
  { unit: 'bản ghi' },
)

const emit = defineEmits<{ (event: 'update:page', value: number): void }>()

const start = computed(() => (props.totalElements === 0 ? 0 : props.page * props.size + 1))
const end = computed(() => Math.min((props.page + 1) * props.size, props.totalElements))
const lastPage = computed(() => Math.max(props.totalPages, 1))

const pageNumbers = computed<number[]>(() => {
  const total = lastPage.value
  const current = props.page
  if (total <= 7) return Array.from({ length: total }, (_, index) => index)
  const from = Math.max(0, Math.min(current - 2, total - 5))
  return Array.from({ length: 5 }, (_, index) => from + index)
})

const goTo = (value: number) => {
  if (value < 0 || value >= lastPage.value || value === props.page) return
  emit('update:page', value)
}
</script>

<template>
  <div class="ef-footer-bar">
    <span>
      Hiển thị <strong>{{ start }} – {{ end }}</strong> trong tổng số
      <strong>{{ totalElements }}</strong> {{ unit }}
    </span>
    <div class="ef-pager">
      <button class="ef-pager-btn" type="button" :disabled="page === 0" aria-label="Trang trước" @click="goTo(page - 1)">
        &lt;
      </button>
      <button
        v-for="item in pageNumbers"
        :key="item"
        class="ef-pager-btn"
        :class="{ active: item === page }"
        type="button"
        @click="goTo(item)"
      >
        {{ item + 1 }}
      </button>
      <button
        class="ef-pager-btn"
        type="button"
        :disabled="page + 1 >= lastPage"
        aria-label="Trang sau"
        @click="goTo(page + 1)"
      >
        &gt;
      </button>
    </div>
  </div>
</template>
