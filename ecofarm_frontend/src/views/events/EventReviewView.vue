<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import SoftBadge from '@/components/common/SoftBadge.vue'
import EventReviewDetail from '@/components/event/EventReviewDetail.vue'
import { useBatchLookupQuery } from '@/composables/useBatches'
import { useReviewCountQuery, useReviewQueueQuery, type ReviewQueueParams } from '@/composables/useEventReview'
import { useEventReviewStore } from '@/stores'
import {
  EVENT_TYPE_LABELS,
  REVIEW_STATUS_LABELS,
  REVIEW_STATUS_TONES,
  formatEventTime,
  type EventReviewStatus,
  type EventType,
} from '@/api/types/event'

const store = useEventReviewStore()

const statusFilter = ref<EventReviewStatus | ''>(store.filterStatus ?? 'UNREVIEWED')
const greenhouseFilter = ref<number | ''>(store.filterGreenhouseId ?? '')

const params = computed<ReviewQueueParams>(() => ({
  reviewStatus: statusFilter.value || null,
  greenhouseId: greenhouseFilter.value || null,
}))

const { data, isLoading, isError, error, fetchNextPage, hasNextPage, isFetchingNextPage } =
  useReviewQueueQuery(params)

const { data: reviewCount } = useReviewCountQuery(params)

const lookups = useBatchLookupQuery()
const greenhouseOptions = computed(() => lookups.data.value?.greenhouses ?? [])

const events = computed(() => data.value?.pages.flatMap((page) => page.content) ?? [])
const selectedId = computed(() => store.selectedEventId)

const typeLabel = (type: string) => EVENT_TYPE_LABELS[type as EventType] ?? type
const statusOptions = Object.entries(REVIEW_STATUS_LABELS) as [EventReviewStatus, string][]

watch(events, (list) => {
  if (!list.length) {
    store.select(null)
    return
  }
  if (!list.some((event) => event.id === store.selectedEventId)) {
    store.select(list[0]?.id ?? null)
  }
})

watch(statusFilter, (value) => store.setStatusFilter(value || null))
watch(greenhouseFilter, (value) => store.setGreenhouseFilter(value || null))

onMounted(() => store.reset())
</script>

<template>
  <div>
    <PageHeader title="Duyệt nhật ký"/>

    <section class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
      <div class="row g-3 align-items-end">
        <div class="col-12 col-md-4">
          <label for="review-status" class="form-label small fw-semibold text-body-secondary mb-1">
            Trạng thái
          </label>
          <BFormSelect id="review-status" v-model="statusFilter">
            <option value="">Tất cả trạng thái</option>
            <option v-for="[value, label] in statusOptions" :key="value" :value="value">{{ label }}</option>
          </BFormSelect>
        </div>
        <div class="col-12 col-md-4">
          <label for="review-greenhouse" class="form-label small fw-semibold text-body-secondary mb-1">
            Nhà màng
          </label>
          <BFormSelect id="review-greenhouse" v-model="greenhouseFilter">
            <option value="">Tất cả nhà màng</option>
            <option v-for="gh in greenhouseOptions" :key="gh.id" :value="gh.id">{{ gh.code }}</option>
          </BFormSelect>
        </div>
      </div>
    </section>

    <div v-if="isLoading" class="d-flex flex-column gap-2 py-3">
      <span class="placeholder placeholder-glow col-8 rounded" style="height: 20px"></span>
      <span class="placeholder placeholder-glow col-12 rounded" style="height: 14px"></span>
      <span class="placeholder placeholder-glow col-10 rounded" style="height: 14px"></span>
      <span class="placeholder placeholder-glow col-6 rounded" style="height: 14px"></span>
    </div>

    <BAlert v-else-if="isError" variant="danger" class="small">
      Không tải được danh sách duyệt: {{ error?.message }}
    </BAlert>

    <div v-else class="row g-3">
      <div class="col-12 col-lg-5">
        <div class="border rounded-3 bg-body">
          <div class="p-3 border-bottom d-flex justify-content-between align-items-center">
            <span class="fw-semibold small">
              {{ statusFilter === 'UNREVIEWED' ? 'Chờ duyệt' : 'Sự kiện' }}
              <template v-if="reviewCount !== undefined"> ({{ reviewCount }})</template>
            </span>
            <SoftBadge variant="info" :label="`${events.length} sự kiện`" />
          </div>

          <p v-if="events.length === 0" class="small text-body-secondary p-3 mb-0">
            Không có sự kiện nào cần duyệt.
          </p>

          <div v-else class="review-scroll d-flex flex-column">
            <button
              v-for="event in events"
              :key="event.id"
              type="button"
              class="btn btn-link text-start text-decoration-none rounded-0 border-bottom p-3"
              :class="{ 'bg-body-secondary': event.id === selectedId }"
              @click="store.select(event.id)"
            >
              <div class="d-flex flex-wrap gap-2 align-items-center mb-1">
                <span class="font-monospace fw-semibold small">{{ event.batchCode ?? `#${event.batchId}` }}</span>
                <SoftBadge
                  :label="REVIEW_STATUS_LABELS[event.reviewStatus]"
                  :variant="REVIEW_STATUS_TONES[event.reviewStatus]"
                />
              </div>
              <div class="small fw-semibold text-body">{{ typeLabel(event.eventType) }}</div>
              <div class="small text-body-secondary">
                {{ event.recordedByName ?? '—' }} · {{ formatEventTime(event.occurredAt ?? event.recordedAt) }}
              </div>
              <div v-if="event.greenhouseName" class="small text-body-secondary">{{ event.greenhouseName }}</div>
            </button>
          </div>

          <div v-if="hasNextPage" class="p-3">
            <BButton
              size="sm"
              variant="outline-secondary"
              class="w-100"
              :disabled="isFetchingNextPage"
              @click="fetchNextPage()"
            >
              {{ isFetchingNextPage ? 'Đang tải...' : 'Tải thêm' }}
            </BButton>
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-7">
        <div class="border rounded-3 bg-body p-3 p-lg-4">
          <EventReviewDetail v-if="selectedId" :key="selectedId" :event-id="selectedId" />
          <p v-else class="small text-body-secondary mb-0">Chọn một sự kiện để xem chi tiết.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@media (min-width: 992px) {
  .review-scroll {
    max-height: 65vh;
    overflow-y: auto;
  }
}
</style>
