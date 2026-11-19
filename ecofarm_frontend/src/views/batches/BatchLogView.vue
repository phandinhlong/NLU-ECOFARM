<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SoftBadge from '@/components/common/SoftBadge.vue'
import EventTimeline from '@/components/batch/EventTimeline.vue'
import { useBatchQuery } from '@/composables/useBatches'
import { useCreateEventMutation } from '@/composables/useEvents'
import { useEventFormStore, useUserStore } from '@/stores'
import {
  EVENT_FIELDS,
  EVENT_TYPE_ICONS,
  EVENT_TYPE_LABELS,
  LOG_EVENT_TYPES,
  type EventType,
} from '@/api/types/event'
import { STATUS_META, formatDate } from '@/api/types/batch'

const route = useRoute()
const router = useRouter()
const form = useEventFormStore()
const userStore = useUserStore()

const isFarmerOnly = computed(() => {
  const roles = userStore.user?.role ?? []
  return roles.includes('FARMER') && !roles.includes('ENGINEER')
})

const id = computed(() => Number(route.params.id) || 0)
const { data: batch, isLoading, isError, error } = useBatchQuery(id)
const mutation = useCreateEventMutation(id)

const saved = ref(false)
const data = computed(() => form.data as Record<string, any>)

const locked = computed(() => batch.value?.locked ?? false)
const statusMeta = computed(() => (batch.value ? STATUS_META[batch.value.status] : null))
const fields = computed(() => (form.eventType ? EVENT_FIELDS[form.eventType] ?? [] : []))
const canSave = computed(() => !!form.eventType && !locked.value && !mutation.isPending.value)

const pad = (n: number) => String(n).padStart(2, '0')
const toLocalInput = (d: Date) =>
  `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`

const occurredLabel = computed(() =>
  form.occurredAt ? new Date(form.occurredAt).toLocaleString('vi-VN') : '—',
)

const chipIcon = (type: string) => EVENT_TYPE_ICONS[type] ?? 'seedling'
const chipLabel = (type: string) => EVENT_TYPE_LABELS[type as EventType] ?? type

const selectType = (type: EventType) => {
  saved.value = false
  form.setEventType(type)
}

const adjustFlowerCount = (delta: number) => {
  const current = Number(form.data.flowerCount) || 0
  form.data.flowerCount = Math.max(0, current + delta)
}

const save = () => {
  if (!canSave.value || !form.eventType) return
  saved.value = false
  const cleaned: Record<string, unknown> = {}
  for (const f of fields.value) {
    const value = form.data[f.key]
    if (value === '' || value === undefined || value === null) continue
    cleaned[f.key] = f.type === 'number' ? Number(value) : value
  }
  mutation.mutate(
    {
      clientEventId: form.clientEventId,
      eventType: form.eventType,
      schemaVersion: 1,
      occurredAt: new Date(form.occurredAt).toISOString(),
      data: cleaned,
    },
    {
      onSuccess: () => {
        saved.value = true
        router.push(
          isFarmerOnly.value
            ? { name: 'farmer-batch-detail', params: { id: id.value } }
            : { name: 'batch-detail', params: { id: id.value } },
        )
      },
    },
  )
}

onMounted(() => {
  if (!form.clientEventId) form.reset()
  if (!form.occurredAt) form.occurredAt = toLocalInput(new Date())
})
</script>

<template>
  <div>
    <div v-if="isLoading" class="d-flex justify-content-center py-5">
      <BSpinner />
    </div>

    <BAlert v-else-if="isError" variant="danger" class="small">
      Không tải được lô: {{ error?.message }}
    </BAlert>

    <template v-else-if="batch">
      <div class="row gy-3 gx-0 gx-lg-3">
        <div class="d-none d-lg-block col-lg-4">
          <div class="border rounded-3 bg-body p-3 mb-3">
            <div class="d-flex flex-wrap gap-2 align-items-center mb-2">
              <span class="font-monospace fw-bold">{{ batch.batchCode }}</span>
              <SoftBadge v-if="statusMeta" :variant="statusMeta.tone" :label="statusMeta.label" />
            </div>
            <div class="row g-2 small">
              <div class="col-12">
                <span class="text-body-secondary">Nhà màng: </span>
                <span class="fw-semibold">{{ batch.greenhouseCode }} · {{ batch.greenhouseName }}</span>
              </div>
              <div class="col-12">
                <span class="text-body-secondary">Giống: </span>
                <span class="fw-semibold">{{ batch.varietyName }}</span>
              </div>
              <div class="col-12">
                <span class="text-body-secondary">Ngày xuống giống: </span>
                <span class="fw-semibold">{{ formatDate(batch.sowDate) }}</span>
              </div>
            </div>
          </div>

          <div class="border rounded-3 bg-body p-3">
            <div class="small text-uppercase text-body-secondary fw-semibold mb-2">Nhật ký gần đây</div>
            <EventTimeline :batch-id="id" />
          </div>
        </div>

        <div class="col-12 col-lg-8">
          <div class="d-lg-none border rounded-3 bg-body p-3 mb-3">
            <div class="d-flex flex-wrap gap-2 align-items-center mb-2">
              <span class="font-monospace fw-bold">{{ batch.batchCode }}</span>
              <SoftBadge v-if="statusMeta" :variant="statusMeta.tone" :label="statusMeta.label" />
            </div>
            <div class="row g-2 small">
              <div class="col-12">
                <span class="text-body-secondary">Nhà màng: </span>
                <span class="fw-semibold">{{ batch.greenhouseCode }} · {{ batch.greenhouseName }}</span>
              </div>
              <div class="col-12">
                <span class="text-body-secondary">Giống: </span>
                <span class="fw-semibold">{{ batch.varietyName }}</span>
              </div>
              <div class="col-12">
                <span class="text-body-secondary">Ngày xuống giống: </span>
                <span class="fw-semibold">{{ formatDate(batch.sowDate) }}</span>
              </div>
            </div>
          </div>

          <BAlert v-if="locked" variant="warning" class="small">
            <FontAwesomeIcon icon="lock" aria-hidden="true" /> Lô đã khóa, không thể ghi nhật ký.
          </BAlert>

          <div class="mb-3">
            <div class="d-flex justify-content-between align-items-center px-1 mb-1">
              <span class="small text-uppercase text-body-secondary fw-semibold">Loại công việc</span>
              <span class="small text-primary">Bắt buộc</span>
            </div>
            <div class="d-flex gap-2 overflow-auto pb-1 flex-lg-wrap">
              <button
                v-for="type in LOG_EVENT_TYPES"
                :key="type"
                type="button"
                class="btn d-flex align-items-center gap-2 flex-shrink-0"
                :class="form.eventType === type ? 'btn-primary' : 'btn-outline-secondary'"
                style="min-height: 48px"
                @click="selectType(type)"
              >
                <FontAwesomeIcon :icon="chipIcon(type)" aria-hidden="true" />
                <span class="text-nowrap">{{ chipLabel(type) }}</span>
              </button>
            </div>
          </div>

          <div class="border rounded-3 bg-body p-3 mb-3">
            <div class="d-flex justify-content-between align-items-center bg-body-secondary rounded-2 px-2 py-2 mb-3">
              <span class="small fw-semibold">
                <FontAwesomeIcon icon="clock-rotate-left" aria-hidden="true" /> Thời điểm ghi nhận
              </span>
              <span class="small fw-bold">{{ occurredLabel }}</span>
            </div>
            <div class="mb-3">
              <label for="event-occurred-at" class="form-label small fw-semibold mb-1">Thời điểm</label>
              <BFormInput id="event-occurred-at" v-model="form.occurredAt" type="datetime-local" />
            </div>

            <div v-for="f in fields" :key="f.key" class="mb-3">
              <label :for="`event-field-${f.key}`" class="form-label small fw-semibold mb-1">{{ f.label }}</label>
              <BFormSelect
                v-if="f.type === 'select'"
                :id="`event-field-${f.key}`"
                v-model="data[f.key]"
              >
                <option value="">Chọn</option>
                <option v-for="opt in f.options" :key="opt" :value="opt">{{ opt }}</option>
              </BFormSelect>
              <div
                v-else-if="form.eventType === 'POLLINATION' && f.key === 'flowerCount'"
                class="d-flex align-items-center gap-2"
              >
                <BButton variant="secondary" class="flex-shrink-0" @click="adjustFlowerCount(-50)">-</BButton>
                <BFormInput
                  :id="`event-field-${f.key}`"
                  v-model="data[f.key]"
                  type="number"
                  step="any"
                  min="0"
                  class="text-center"
                />
                <BButton variant="secondary" class="flex-shrink-0" @click="adjustFlowerCount(50)">+</BButton>
              </div>
              <BFormInput
                v-else
                :id="`event-field-${f.key}`"
                v-model="data[f.key]"
                :type="f.type"
                :step="f.type === 'number' ? 'any' : undefined"
              />
            </div>
          </div>

          <div class="border rounded-3 bg-body p-3 mb-3">
            <div class="d-flex align-items-center gap-2 mb-2">
              <FontAwesomeIcon icon="camera" aria-hidden="true" class="text-primary" />
              <span class="small fw-semibold">Hình ảnh thực địa</span>
            </div>
            <BButton variant="outline-secondary" size="sm" disabled>
              Đính kèm ảnh (sắp có)
            </BButton>
          </div>

          <BAlert v-if="saved" variant="success" class="small">Đã lưu nhật ký canh tác.</BAlert>
          <BAlert v-if="mutation.isError.value" variant="danger" class="small">
            Lưu thất bại: {{ mutation.error.value?.message }}
          </BAlert>

          <BButton
            variant="primary"
            class="w-100 fw-bold text-uppercase"
            style="min-height: 52px"
            :disabled="!canSave"
            @click="save"
          >
            <FontAwesomeIcon icon="floppy-disk" aria-hidden="true" />
            {{ mutation.isPending.value ? 'Đang lưu...' : 'Lưu nhật ký canh tác' }}
          </BButton>
        </div>
      </div>
    </template>
  </div>
</template>
