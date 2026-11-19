<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRejectEventMutation } from '@/composables/useEventReview'

const props = defineProps<{
  modelValue: boolean
  eventId: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const mutation = useRejectEventMutation()
const reason = ref('')

const canConfirm = computed(() => reason.value.trim().length > 0)

watch(
  () => props.modelValue,
  (open) => {
    if (open) reason.value = ''
  },
)

const onConfirm = () => {
  if (!canConfirm.value) return
  mutation.mutate(
    { id: props.eventId, input: { reason: reason.value.trim() } },
    { onSuccess: () => emit('update:modelValue', false) },
  )
}
</script>

<template>
  <BModal
    :model-value="modelValue"
    title="Từ chối sự kiện"
    ok-title="Từ chối"
    cancel-title="Hủy"
    :ok-loading="mutation.isPending.value"
    :ok-disabled="!canConfirm"
    @ok.prevent="onConfirm"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="d-flex flex-column gap-3">
      <div>
        <label for="reject-reason" class="form-label small fw-semibold mb-1">Lý do từ chối</label>
        <textarea id="reject-reason" v-model="reason" class="form-control" rows="3" />
      </div>
      <div v-if="mutation.isError.value" class="alert alert-danger p-2 small mb-0">
        {{ mutation.error.value?.message }}
      </div>
    </div>
  </BModal>
</template>
