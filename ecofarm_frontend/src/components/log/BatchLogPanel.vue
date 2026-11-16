<script setup lang="ts">
import { computed, ref, toRef } from 'vue'
import CultivationEventTimeline from '@/components/log/CultivationEventTimeline.vue'
import RecordEventForm from '@/components/log/RecordEventForm.vue'
import {
  useAmendEventMutation,
  useApproveEventMutation,
  useCreateEventMutation,
  useCultivationEventsQuery,
  useRejectEventMutation,
  useUploadAttachmentsMutation,
} from '@/composables/useCultivationEvents'
import type { CreateEventRequest, EventListParams, EventView } from '@/api/types/log'

const props = defineProps<{ batchId: number; canReview: boolean }>()
const batchId = toRef(props, 'batchId')

const params = ref<EventListParams>({ page: 0, size: 50, includeSuperseded: true })
const { data, isLoading, isError, error, refetch } = useCultivationEventsQuery(batchId, params)

const createMutation = useCreateEventMutation(batchId)
const uploadMutation = useUploadAttachmentsMutation(batchId)
const approveMutation = useApproveEventMutation(batchId)
const rejectMutation = useRejectEventMutation(batchId)
const amendMutation = useAmendEventMutation(batchId)

const formRef = ref<InstanceType<typeof RecordEventForm> | null>(null)
const formError = ref('')
const submitting = computed(() => createMutation.isPending.value || uploadMutation.isPending.value)

const events = computed<EventView[]>(() => data.value?.content ?? [])

const onSubmit = async (body: CreateEventRequest, files: File[]) => {
  formError.value = ''
  try {
    // Upload attachments first so the create request references stable file keys.
    const fileKeys = files.length ? (await uploadMutation.mutateAsync(files)).map((a) => a.fileKey) : []
    await createMutation.mutateAsync({ ...body, attachmentFileKeys: fileKeys })
    formRef.value?.resetDraft()
  } catch (e) {
    // Keep the draft intact on failure; the backend owns duplicate protection on retry.
    formError.value = (e as Error).message || 'Không ghi được sự kiện. Vui lòng thử lại.'
  }
}

const rejectReason = ref('')
const rejectTarget = ref<number | null>(null)
const showReject = ref(false)
const openReject = (eventId: number) => {
  rejectTarget.value = eventId
  rejectReason.value = ''
  showReject.value = true
}
const confirmReject = () => {
  if (rejectTarget.value == null || !rejectReason.value.trim()) return
  rejectMutation.mutate(
    { eventId: rejectTarget.value, body: { reason: rejectReason.value.trim() } },
    { onSuccess: () => (showReject.value = false) },
  )
}

const amendPayload = ref('')
const amendTarget = ref<EventView | null>(null)
const amendError = ref('')
const showAmend = ref(false)
const openAmend = (event: EventView) => {
  amendTarget.value = event
  amendPayload.value = JSON.stringify(event.payload ?? { schemaVersion: 1 }, null, 2)
  amendError.value = ''
  showAmend.value = true
}
const confirmAmend = () => {
  if (!amendTarget.value) return
  let payload: Record<string, unknown>
  try {
    payload = JSON.parse(amendPayload.value)
  } catch {
    amendError.value = 'Payload không phải JSON hợp lệ.'
    return
  }
  amendMutation.mutate(
    {
      eventId: amendTarget.value.id,
      body: {
        clientEventId: crypto.randomUUID(),
        occurredAt: amendTarget.value.occurredAt ?? Date.now(),
        payload,
        reason: 'Đính chính từ giao diện',
      },
    },
    {
      onSuccess: () => (showAmend.value = false),
      onError: (e) => (amendError.value = (e as Error).message),
    },
  )
}
</script>

<template>
  <BCard class="mt-4">
    <h5 class="mb-3">Nhật ký canh tác</h5>

    <BAlert :model-value="!!formError" variant="danger" dismissible @close="formError = ''">
      {{ formError }}
    </BAlert>

    <RecordEventForm ref="formRef" :submitting="submitting" @submit="onSubmit" />

    <hr />

    <div v-if="isLoading" class="d-flex justify-content-center py-4"><BSpinner /></div>
    <BAlert v-else-if="isError" :model-value="true" variant="danger" class="d-flex gap-2 align-items-center">
      <span>Không tải được nhật ký: {{ (error as Error)?.message }}</span>
      <BButton size="sm" variant="outline-danger" @click="refetch()">Thử lại</BButton>
    </BAlert>
    <CultivationEventTimeline
      v-else
      :batch-id="batchId"
      :events="events"
      :can-review="canReview"
      @approve="(id) => approveMutation.mutate(id)"
      @reject="openReject"
      @amend="openAmend"
    />

    <BModal v-model="showReject" title="Từ chối sự kiện" @ok="confirmReject">
      <BFormGroup label="Lý do từ chối">
        <BFormTextarea v-model="rejectReason" rows="3" />
      </BFormGroup>
    </BModal>

    <BModal v-model="showAmend" title="Đính chính sự kiện" @ok="confirmAmend">
      <BAlert :model-value="!!amendError" variant="danger">{{ amendError }}</BAlert>
      <BFormGroup label="Payload mới (JSON)">
        <BFormTextarea v-model="amendPayload" rows="8" />
      </BFormGroup>
    </BModal>
  </BCard>
</template>
