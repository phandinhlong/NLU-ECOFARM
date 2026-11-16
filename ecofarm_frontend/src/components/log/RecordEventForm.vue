<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RECORDABLE_EVENT_TYPES } from '@/api/types/log'
import type { CreateEventRequest, EventTypeCode } from '@/api/types/log'

const props = defineProps<{ submitting: boolean }>()
const emit = defineEmits<{ submit: [body: CreateEventRequest, files: File[]] }>()

// Local draft only; nothing is sent until the user saves (draft-then-commit).
const draft = reactive({
  eventType: 'GHI_CHU' as EventTypeCode,
  occurredAt: new Date().toISOString().slice(0, 16),
  quantity: 1,
  sampleCount: 1,
  brix: 12,
  substance: '',
  note: '',
})
const files = ref<File[]>([])

// Stable per draft so a retry after a failed save reuses the same idempotency key.
const clientEventId = ref(crypto.randomUUID())

const buildPayload = (): Record<string, unknown> => {
  const base: Record<string, unknown> = { schemaVersion: 1 }
  switch (draft.eventType) {
    case 'XUONG_GIONG':
      return { ...base, quantity: draft.quantity }
    case 'THU_PHAN':
      return { ...base, sampleCount: draft.sampleCount }
    case 'DO_BRIX':
      return { ...base, brix: draft.brix }
    case 'BON_PHAN':
    case 'PHUN_VI_LUONG':
      return { ...base, substance: draft.substance }
    default:
      return { ...base, note: draft.note }
  }
}

const onFiles = (event: Event) => {
  const input = event.target as HTMLInputElement
  files.value = input.files ? Array.from(input.files) : []
}

const onSubmit = () => {
  if (props.submitting) return
  emit(
    'submit',
    {
      eventType: draft.eventType,
      occurredAt: new Date(draft.occurredAt).getTime(),
      clientEventId: clientEventId.value,
      payload: buildPayload(),
    },
    files.value,
  )
}

// Called by the parent after a successful save so the next entry starts fresh.
const resetDraft = () => {
  clientEventId.value = crypto.randomUUID()
  files.value = []
  draft.note = ''
  draft.substance = ''
}
defineExpose({ resetDraft })
</script>

<template>
  <BForm class="d-flex flex-column gap-3" @submit.prevent="onSubmit">
    <BFormGroup label="Loại sự kiện">
      <BFormSelect v-model="draft.eventType" :options="RECORDABLE_EVENT_TYPES" />
    </BFormGroup>

    <BFormGroup label="Thời điểm xảy ra">
      <BFormInput v-model="draft.occurredAt" type="datetime-local" />
    </BFormGroup>

    <BFormGroup v-if="draft.eventType === 'XUONG_GIONG'" label="Số lượng">
      <BFormInput v-model.number="draft.quantity" type="number" min="1" />
    </BFormGroup>
    <BFormGroup v-else-if="draft.eventType === 'THU_PHAN'" label="Số mẫu">
      <BFormInput v-model.number="draft.sampleCount" type="number" min="1" />
    </BFormGroup>
    <BFormGroup v-else-if="draft.eventType === 'DO_BRIX'" label="Chỉ số Brix">
      <BFormInput v-model.number="draft.brix" type="number" step="0.1" min="0" max="40" />
    </BFormGroup>
    <BFormGroup
      v-else-if="draft.eventType === 'BON_PHAN' || draft.eventType === 'PHUN_VI_LUONG'"
      label="Loại phân / vi lượng"
    >
      <BFormInput v-model="draft.substance" type="text" />
    </BFormGroup>
    <BFormGroup v-else label="Ghi chú">
      <BFormTextarea v-model="draft.note" rows="2" />
    </BFormGroup>

    <BFormGroup label="Tệp đính kèm (tối đa 5, ảnh hoặc PDF)">
      <input class="form-control" type="file" multiple accept="image/*,application/pdf" @change="onFiles" />
    </BFormGroup>

    <div>
      <BButton type="submit" variant="primary" :disabled="submitting">
        {{ submitting ? 'Đang lưu…' : 'Ghi sự kiện' }}
      </BButton>
    </div>
  </BForm>
</template>
