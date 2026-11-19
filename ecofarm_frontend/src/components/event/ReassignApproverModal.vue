<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useEngineerSearchQuery } from '@/composables/useUsers'
import { useReassignApproverMutation } from '@/composables/useEventReview'

const props = defineProps<{
  modelValue: boolean
  eventId: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const mutation = useReassignApproverMutation()

const keyword = ref('')
const debouncedKeyword = ref('')
const engineerId = ref<number | null>(null)

let timer: ReturnType<typeof setTimeout> | undefined

watch(keyword, (value) => {
  clearTimeout(timer)
  timer = setTimeout(() => {
    debouncedKeyword.value = value
  }, 300)
})

const params = computed(() => ({ keyword: debouncedKeyword.value }))
const { data, isLoading, hasNextPage, isFetchingNextPage, fetchNextPage } =
  useEngineerSearchQuery(params)

const engineers = computed(() => data.value?.pages.flatMap((page) => page.content) ?? [])

const canSubmit = computed(() => engineerId.value !== null && !mutation.isPending.value)

watch(
  () => props.modelValue,
  (open) => {
    if (!open) return
    mutation.reset()
    keyword.value = ''
    debouncedKeyword.value = ''
    engineerId.value = null
  },
)

const onSubmit = () => {
  if (!canSubmit.value) return
  mutation.mutate(
    { id: props.eventId, input: { engineerId: engineerId.value as number } },
    {
      onSuccess: () => {
        emit('success')
        emit('update:modelValue', false)
      },
    },
  )
}
</script>

<template>
  <BModal
    :model-value="modelValue"
    title="Chuyển người duyệt"
    ok-title="Chuyển"
    cancel-title="Hủy"
    :ok-loading="mutation.isPending.value"
    :ok-disabled="!canSubmit"
    @ok.prevent="onSubmit"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="d-flex flex-column gap-3">
      <BFormInput v-model="keyword" placeholder="Tìm theo họ tên hoặc email" />

      <div v-if="isLoading" class="d-flex justify-content-center py-3">
        <BSpinner small />
      </div>

      <div v-else-if="engineers.length === 0" class="text-body-secondary small text-center py-3">
        Không tìm thấy kỹ sư
      </div>

      <div v-else class="d-flex flex-column gap-2" style="max-height: 320px; overflow-y: auto">
        <button
          v-for="engineer in engineers"
          :key="engineer.id"
          type="button"
          class="btn text-start border rounded-3 p-2"
          :class="engineerId === engineer.id ? 'btn-primary' : 'btn-light'"
          @click="engineerId = engineer.id"
        >
          <div class="fw-semibold small">{{ engineer.fullName }}</div>
          <div class="small text-truncate">{{ engineer.email }}</div>
        </button>

        <BButton
          v-if="hasNextPage"
          variant="outline-secondary"
          size="sm"
          :disabled="isFetchingNextPage"
          @click="fetchNextPage()"
        >
          {{ isFetchingNextPage ? 'Đang tải...' : 'Tải thêm' }}
        </BButton>
      </div>

      <BAlert v-if="mutation.isError.value" variant="danger" class="small mb-0">
        Chuyển người duyệt thất bại: {{ mutation.error.value?.message }}
      </BAlert>
    </div>
  </BModal>
</template>
