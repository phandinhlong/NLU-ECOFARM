<script setup lang="ts">
import { computed } from 'vue'
import SoftBadge from '@/components/common/SoftBadge.vue'
import { useEventsQuery } from '@/composables/useEvents'
import {
  CHAIN_STATUS_LABELS,
  CHAIN_STATUS_TONES,
  EVENT_TYPE_LABELS,
  REVIEW_STATUS_LABELS,
  REVIEW_STATUS_TONES,
  formatEventTime,
  type CultivationEvent,
} from '@/api/types/event'

const props = defineProps<{ batchId: number }>()

const batchIdRef = computed(() => props.batchId)
const { data, isLoading, isError, error } = useEventsQuery(batchIdRef)

const events = computed(() => data.value?.content ?? [])

const typeLabel = (type: string) => EVENT_TYPE_LABELS[type as keyof typeof EVENT_TYPE_LABELS] ?? type

const summary = (event: CultivationEvent) =>
  Object.entries(event.data ?? {}).filter(([, value]) => value !== null && typeof value !== 'object')
</script>

<template>
  <div>
    <div v-if="isLoading" class="d-flex justify-content-center py-4">
      <BSpinner small />
    </div>

    <BAlert v-else-if="isError" variant="danger" class="small mb-0">
      Không tải được sự kiện: {{ error?.message }}
    </BAlert>

    <p v-else-if="events.length === 0" class="small text-body-secondary mb-0">Chưa có sự kiện nào</p>

    <div v-else class="d-flex flex-column gap-2">
      <div
        v-for="event in events"
        :key="event.id"
        class="border rounded-3 p-2 p-md-3"
        :class="{ 'opacity-50': event.status === 'SUPERSEDED' }"
      >
        <div class="d-flex flex-wrap gap-2 align-items-center mb-1">
          <span class="fw-semibold">{{ typeLabel(event.eventType) }}</span>
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
        <div class="small text-body-secondary mb-1">
          {{ formatEventTime(event.occurredAt ?? event.recordedAt) }} · {{ event.recordedByName ?? '—' }}
        </div>
        <div v-if="summary(event).length" class="d-flex flex-wrap gap-2 small">
          <span v-for="[key, value] in summary(event)" :key="key" class="text-body-secondary">
            {{ key }}: <span class="text-body">{{ value }}</span>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
