<script setup lang="ts">
import { computed } from 'vue'
import type { ColorVariant } from 'bootstrap-vue-next'
import { logService } from '@/api/services/log.service'
import type { EventStatus, EventView } from '@/api/types/log'

const props = defineProps<{
  batchId: number
  events: EventView[]
  canReview: boolean
}>()

const emit = defineEmits<{
  approve: [eventId: number]
  reject: [eventId: number]
  amend: [event: EventView]
}>()

const ordered = computed(() => [...props.events].sort((a, b) => b.seq - a.seq))

const statusVariant = (status: EventStatus): ColorVariant => {
  switch (status) {
    case 'APPROVED':
      return 'success'
    case 'REJECTED':
      return 'danger'
    case 'SUPERSEDED':
      return 'secondary'
    case 'CONFIRMED':
      return 'info'
    default:
      return 'warning'
  }
}

const formatTime = (ms: number | null): string => (ms ? new Date(ms).toLocaleString() : '—')
</script>

<template>
  <div class="d-flex flex-column gap-3">
    <BCard
      v-for="event in ordered"
      :key="event.id"
      :class="{ 'opacity-75': event.status === 'SUPERSEDED' }"
    >
      <div class="d-flex justify-content-between align-items-start gap-2">
        <div>
          <div class="d-flex align-items-center gap-2">
            <strong>{{ event.eventType }}</strong>
            <BBadge :variant="statusVariant(event.status)">{{ event.status }}</BBadge>
            <span class="text-muted small">#{{ event.seq }}</span>
          </div>
          <div class="text-muted small">
            Xảy ra: {{ formatTime(event.occurredAt) }} · Ghi: {{ formatTime(event.recordedAt) }}
          </div>
        </div>
        <div v-if="canReview && event.status === 'CONFIRMED'" class="d-flex gap-2">
          <BButton size="sm" variant="success" @click="emit('approve', event.id)">Duyệt</BButton>
          <BButton size="sm" variant="outline-danger" @click="emit('reject', event.id)">Từ chối</BButton>
          <BButton size="sm" variant="outline-secondary" @click="emit('amend', event)">Đính chính</BButton>
        </div>
      </div>

      <pre class="mt-2 mb-2 small bg-body-tertiary p-2 rounded">{{ JSON.stringify(event.payload, null, 2) }}</pre>

      <div v-if="event.attachments.length" class="d-flex flex-wrap gap-2">
        <a
          v-for="attachment in event.attachments"
          :key="attachment.id"
          :href="logService.attachmentUrl(batchId, attachment.fileKey)"
          target="_blank"
          rel="noopener"
          class="small"
        >
          {{ attachment.mimeType }} ({{ attachment.sha256.slice(0, 8) }}…)
        </a>
      </div>

      <div v-if="event.amendsEventId" class="text-muted small mt-1">
        Thay thế sự kiện #{{ event.amendsEventId }}
      </div>
    </BCard>

    <BAlert v-if="!ordered.length" :model-value="true" variant="light">Chưa có sự kiện nào.</BAlert>
  </div>
</template>
