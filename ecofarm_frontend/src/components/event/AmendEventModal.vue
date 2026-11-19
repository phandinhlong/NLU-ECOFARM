<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useAmendEventMutation, useEventQuery } from '@/composables/useEventReview'
import { AMEND_REASON_LABELS, EVENT_FIELDS, type AmendReason } from '@/api/types/event'

const props = defineProps<{
  modelValue: boolean
  eventId: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const toLocalInput = (iso: string | null | undefined) => {
  const d = iso ? new Date(iso) : new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const eventIdRef = computed(() => (props.modelValue ? props.eventId : null))
const { data: event } = useEventQuery(eventIdRef)
const mutation = useAmendEventMutation()

const occurredAt = ref('')
const data = ref<Record<string, unknown>>({})
const reasonCode = ref<AmendReason>('WRONG_QUANTITY')
const note = ref('')

const fields = computed(() => (event.value ? EVENT_FIELDS[event.value.eventType] ?? [] : []))
const currentEntries = computed(() =>
  fields.value.map((f) => ({
    key: f.key,
    label: f.label,
    value: event.value?.data?.[f.key] ?? '—',
  })),
)
const formData = computed(() => data.value as Record<string, any>)
const reasonOptions = Object.entries(AMEND_REASON_LABELS) as [AmendReason, string][]
const noteRequired = computed(() => reasonCode.value === 'OTHER')
const canSubmit = computed(
  () =>
    !!occurredAt.value &&
    (!noteRequired.value || note.value.trim().length > 0) &&
    !mutation.isPending.value,
)

watch(
  () => props.modelValue,
  (open) => {
    if (!open) return
    mutation.reset()
    occurredAt.value = ''
    data.value = {}
    reasonCode.value = 'WRONG_QUANTITY'
    note.value = ''
  },
)

watch(event, (value) => {
  if (!value || !props.modelValue) return
  occurredAt.value = toLocalInput(value.occurredAt)
  data.value = { ...(value.data ?? {}) }
})

const onSubmit = () => {
  if (!canSubmit.value) return
  const cleaned: Record<string, unknown> = {}
  for (const f of fields.value) {
    const value = data.value[f.key]
    if (value === '' || value === undefined || value === null) continue
    cleaned[f.key] = f.type === 'number' ? Number(value) : value
  }
  mutation.mutate(
    {
      id: props.eventId,
      input: {
        clientEventId: crypto.randomUUID(),
        occurredAt: new Date(occurredAt.value).toISOString(),
        data: cleaned,
        reasonCode: reasonCode.value,
        note: note.value.trim() || undefined,
      },
    },
    { onSuccess: () => emit('update:modelValue', false) },
  )
}
</script>

<template>
  <BModal
    :model-value="modelValue"
    title="Đính chính sự kiện"
    ok-title="Gửi đính chính"
    cancel-title="Hủy"
    :ok-loading="mutation.isPending.value"
    :ok-disabled="!canSubmit"
    @ok.prevent="onSubmit"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="d-flex flex-column gap-3">
      <div v-if="event" class="border rounded-3 p-2 bg-body-secondary small">
        <div class="fw-semibold mb-1">Giá trị hiện tại</div>
        <div v-for="entry in currentEntries" :key="entry.key" class="d-flex gap-2">
          <span class="text-body-secondary flex-shrink-0" style="min-width: 120px">{{ entry.label }}</span>
          <span class="text-body">{{ String(entry.value) }}</span>
        </div>
        <div class="d-flex gap-2 mt-1">
          <span class="text-body-secondary flex-shrink-0" style="min-width: 120px">Người ghi</span>
          <span class="text-body">{{ event.recordedByName ?? '—' }}</span>
        </div>
      </div>

      <div>
        <label for="amend-occurred-at" class="form-label small fw-semibold mb-1">Thời điểm</label>
        <BFormInput id="amend-occurred-at" v-model="occurredAt" type="datetime-local" />
      </div>

      <div v-for="f in fields" :key="f.key">
        <label :for="`amend-field-${f.key}`" class="form-label small fw-semibold mb-1">{{ f.label }}</label>
        <BFormSelect v-if="f.type === 'select'" :id="`amend-field-${f.key}`" v-model="formData[f.key]">
          <option value="">Chọn</option>
          <option v-for="opt in f.options" :key="opt" :value="opt">{{ opt }}</option>
        </BFormSelect>
        <BFormInput
          v-else
          :id="`amend-field-${f.key}`"
          v-model="formData[f.key]"
          :type="f.type"
          :step="f.type === 'number' ? 'any' : undefined"
        />
      </div>

      <div>
        <label for="amend-reason" class="form-label small fw-semibold mb-1">Lý do</label>
        <BFormSelect id="amend-reason" v-model="reasonCode">
          <option v-for="[value, label] in reasonOptions" :key="value" :value="value">{{ label }}</option>
        </BFormSelect>
      </div>

      <div>
        <label for="amend-note" class="form-label small fw-semibold mb-1">
          Ghi chú<span v-if="noteRequired" class="text-danger"> *</span>
        </label>
        <textarea id="amend-note" v-model="note" class="form-control" rows="2" />
      </div>

      <div v-if="mutation.isError.value" class="alert alert-danger p-2 small mb-0">
        {{ mutation.error.value?.message }}
      </div>
    </div>
  </BModal>
</template>
