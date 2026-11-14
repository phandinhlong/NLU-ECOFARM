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
  <footer
    class="d-flex flex-wrap gap-2 justify-content-between align-items-center px-3 py-2 border-top small text-body-secondary"
  >
    <span>
      Hiển thị <strong class="text-body">{{ start }} – {{ end }}</strong> trong tổng số
      <strong class="text-body">{{ totalElements }}</strong> {{ unit }}
    </span>

    <nav aria-label="Phân trang">
      <ul class="pagination pagination-sm mb-0">
        <li class="page-item" :class="{ disabled: page === 0 }">
          <button class="page-link" type="button" :disabled="page === 0" @click="goTo(page - 1)">
            &lt;
          </button>
        </li>
        <li
          v-for="item in pageNumbers"
          :key="item"
          class="page-item"
          :class="{ active: item === page }"
        >
          <button class="page-link" type="button" @click="goTo(item)">{{ item + 1 }}</button>
        </li>
        <li class="page-item" :class="{ disabled: page + 1 >= lastPage }">
          <button
            class="page-link"
            type="button"
            :disabled="page + 1 >= lastPage"
            @click="goTo(page + 1)"
          >
            &gt;
          </button>
        </li>
      </ul>
    </nav>
  </footer>
</template>
