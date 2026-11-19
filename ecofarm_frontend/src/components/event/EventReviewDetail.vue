<script setup lang="ts">
import { computed, ref } from 'vue'
import SoftBadge from '@/components/common/SoftBadge.vue'
import RejectEventModal from '@/components/event/RejectEventModal.vue'
import AmendEventModal from '@/components/event/AmendEventModal.vue'
import ReassignApproverModal from '@/components/event/ReassignApproverModal.vue'
import { useApproveEventMutation, useEventQuery } from '@/composables/useEventReview'
import { useEventReviewStore, useUserStore } from '@/stores'
import {
  AMEND_REASON_LABELS,
  CHAIN_STATUS_LABELS,
  CHAIN_STATUS_TONES,
  EVENT_FIELDS,
  EVENT_TYPE_LABELS,
  REVIEW_STATUS_LABELS,
  REVIEW_STATUS_TONES,
  formatEventTime,
  type EventType,
} from '@/api/types/event'

const props = defineProps<{ eventId: number }>()

const store = useEventReviewStore()
const userStore = useUserStore()
const isAdmin = computed(() => (userStore.user?.role ?? []).includes('ADMIN'))
const eventIdRef = computed(() => props.eventId)
const { data: event, isLoading, isError, error } = useEventQuery(eventIdRef)
const approveMutation = useApproveEventMutation()

const typeLabel = (type: string) => EVENT_TYPE_LABELS[type as EventType] ?? type

const fields = computed(() => (event.value ? EVENT_FIELDS[event.value.eventType] ?? [] : []))

const entries = computed(() => {
  const data = event.value?.data ?? {}
  return Object.entries(data).map(([key, value]) => ({
    key,
    label: fields.value.find((f) => f.key === key)?.label ?? key,
    value: value === null || value === undefined ? '—' : String(value),
  }))
})

const amendLabel = computed(() => {
  const reason = event.value?.amendReason
  return reason ? AMEND_REASON_LABELS[reason as keyof typeof AMEND_REASON_LABELS] ?? reason : null
})

const showApproveConfirm = ref(false)
const approved = ref(false)
const showTech = ref(false)

const confirmApprove = () => {
  approved.value = false
  approveMutation.mutate(props.eventId, {
    onSuccess: () => {
      showApproveConfirm.value = false
      approved.value = true
    },
  })
}

const rejectOpen = computed({
  get: () => store.rejectTargetId === props.eventId,
  set: (open) => (open ? store.openReject(props.eventId) : store.closeReject()),
})

const amendOpen = computed({
  get: () => store.amendTargetId === props.eventId,
  set: (open) => (open ? store.openAmend(props.eventId) : store.closeAmend()),
})

const reassignOpen = ref(false)
const reassigned = ref(false)
</script>

<template>
  <div>
    <div v-if="isLoading" class="d-flex justify-content-center py-4">
      <BSpinner small />
    </div>

    <BAlert v-else-if="isError" variant="danger" class="small mb-0">
      Không tải được sự kiện: {{ error?.message }}
    </BAlert>

    <div v-else-if="event" class="d-flex flex-column gap-3">
      <div>
        <div class="d-flex flex-wrap gap-2 align-items-center mb-1">
          <span class="font-monospace fw-bold">{{ event.batchCode ?? `#${event.batchId}` }}</span>
          <SoftBadge
            :label="REVIEW_STATUS_LABELS[event.reviewStatus]"
            :variant="REVIEW_STATUS_TONES[event.reviewStatus]"
          />
          <SoftBadge
            v-if="event.status"
            :label="CHAIN_STATUS_LABELS[event.status]"
            :variant="CHAIN_STATUS_TONES[event.status]"
          />
        </div>
        <div class="fw-semibold">{{ typeLabel(event.eventType) }}</div>
        <div v-if="event.greenhouseName" class="small text-body-secondary">{{ event.greenhouseName }}</div>
      </div>

      <div class="row g-2 small">
        <div class="col-12 col-sm-6">
          <span class="text-body-secondary">Người ghi: </span>
          <span class="fw-semibold">{{ event.recordedByName ?? '—' }}</span>
        </div>
        <div class="col-12 col-sm-6">
          <span class="text-body-secondary">Ghi lúc: </span>
          <span>{{ formatEventTime(event.recordedAt) }}</span>
        </div>
        <div class="col-12 col-sm-6">
          <span class="text-body-secondary">Thời điểm: </span>
          <span>{{ formatEventTime(event.occurredAt) }}</span>
        </div>
      </div>

      <div class="border rounded-3">
        <div class="px-3 py-2 border-bottom small fw-semibold">Dữ liệu</div>
        <div v-if="entries.length === 0" class="px-3 py-2 small text-body-secondary">Không có dữ liệu</div>
        <div v-for="entry in entries" :key="entry.key" class="d-flex gap-2 px-3 py-2 border-bottom small">
          <span class="text-body-secondary flex-shrink-0" style="min-width: 120px">{{ entry.label }}</span>
          <span class="text-body text-break">{{ entry.value }}</span>
        </div>
      </div>

      <div class="small">
        <BButton size="sm" variant="outline-secondary" @click="showTech = !showTech">
          {{ showTech ? 'Ẩn thông tin kỹ thuật' : 'Thông tin kỹ thuật' }}
        </BButton>
        <div v-if="showTech" class="border rounded-3 p-2 mt-2 d-flex flex-column gap-1">
          <div>
            <span class="text-body-secondary">Hash: </span>
            <span
              v-if="event.eventHash"
              class="font-monospace text-truncate d-inline-block align-bottom"
              style="max-width: 100%"
              :title="event.eventHash"
            >{{ event.eventHash }}</span>
            <span v-else>—</span>
          </div>
          <div>
            <span class="text-body-secondary">Chuỗi: </span>
            <span>{{ event.status ? CHAIN_STATUS_LABELS[event.status] : '—' }}</span>
          </div>
          <div>
            <span class="text-body-secondary">Duyệt: </span>
            <span>{{ REVIEW_STATUS_LABELS[event.reviewStatus] }}</span>
          </div>
          <div>
            <span class="text-body-secondary">Ghi lúc: </span>
            <span>{{ formatEventTime(event.recordedAt) }}</span>
          </div>
          <div>
            <span class="text-body-secondary">Client event ID: </span>
            <span
              class="font-monospace text-truncate d-inline-block align-bottom"
              style="max-width: 100%"
              :title="event.clientEventId ?? undefined"
            >{{ event.clientEventId ?? '—' }}</span>
          </div>
        </div>
      </div>

      <div v-if="event.pendingApproverId" class="small text-body-secondary">
        Người duyệt được chỉ định: #{{ event.pendingApproverId }}
      </div>

      <BAlert v-if="approved" variant="success" class="small mb-0">Đã duyệt sự kiện.</BAlert>
      <BAlert v-if="reassigned" variant="success" class="small mb-0">Đã chuyển người duyệt.</BAlert>
      <BAlert v-if="approveMutation.isError.value" variant="danger" class="small mb-0">
        Duyệt thất bại: {{ approveMutation.error.value?.message }}
      </BAlert>

      <BAlert v-if="event.reviewStatus === 'REJECTED' && event.rejectReason" variant="danger" class="small mb-0">
        Lý do từ chối: {{ event.rejectReason }}
      </BAlert>

      <BAlert v-if="amendLabel" variant="warning" class="small mb-0">
        Đính chính: {{ amendLabel }}<span v-if="event.amendNote"> — {{ event.amendNote }}</span>
      </BAlert>

      <div v-if="event.reviewStatus === 'UNREVIEWED'" class="d-flex flex-wrap gap-2">
        <BButton variant="primary" @click="showApproveConfirm = true">
          <FontAwesomeIcon icon="check" aria-hidden="true" /> Duyệt
        </BButton>
        <BButton variant="danger" @click="rejectOpen = true">
          <FontAwesomeIcon icon="xmark" aria-hidden="true" /> Từ chối
        </BButton>
        <BButton variant="secondary" @click="amendOpen = true">
          <FontAwesomeIcon icon="pen-to-square" aria-hidden="true" /> Đính chính
        </BButton>
        <BButton
          v-if="isAdmin && (event.assignerId === null || event.assignerActive === false)"
          variant="outline-primary"
          @click="reassignOpen = true"
        >
          <FontAwesomeIcon icon="user-group" aria-hidden="true" /> Chuyển người duyệt
        </BButton>
      </div>
    </div>

    <BModal v-model="showApproveConfirm" title="Duyệt sự kiện">
      <p class="mb-0 small">Xác nhận duyệt sự kiện này?</p>
      <template #footer>
        <BButton variant="outline-secondary" @click="showApproveConfirm = false">Hủy</BButton>
        <BButton variant="primary" :disabled="approveMutation.isPending.value" @click="confirmApprove">
          {{ approveMutation.isPending.value ? 'Đang duyệt...' : 'Duyệt' }}
        </BButton>
      </template>
    </BModal>

    <RejectEventModal v-model="rejectOpen" :event-id="eventId" />
    <AmendEventModal v-model="amendOpen" :event-id="eventId" />
    <ReassignApproverModal
      v-model="reassignOpen"
      :event-id="eventId"
      @success="reassigned = true"
    />
  </div>
</template>
