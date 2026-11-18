<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SoftBadge from '@/components/common/SoftBadge.vue'
import TaskList from '@/components/batch/TaskList.vue'
import EventTimeline from '@/components/batch/EventTimeline.vue'
import { useBatchQuery } from '@/composables/useBatches'
import { useUserStore } from '@/stores'
import { STATUS_META, formatDate, type Problem } from '@/api/types/batch'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const id = computed(() => Number(route.params.id) || 0)
const { data, isLoading, isError, error, refetch } = useBatchQuery(id)

const roles = computed(() => userStore.user?.role ?? [])
const isFarmer = computed(() => roles.value.includes('FARMER'))

const problem = computed(() => error.value as (Error & { status?: number } & Partial<Problem>) | null)
const errorStatus = computed(() => problem.value?.status ?? 0)
const locked = computed(() => data.value?.locked ?? false)
const statusMeta = computed(() => (data.value ? STATUS_META[data.value.status] : null))
</script>

<template>
  <div>
    <RouterLink :to="{ name: 'farmer-home' }" class="small text-decoration-none d-inline-flex align-items-center gap-1">
      <FontAwesomeIcon icon="arrow-left" aria-hidden="true" /> Lô của tôi
    </RouterLink>

    <div v-if="isLoading" class="d-flex justify-content-center py-5">
      <BSpinner />
    </div>

    <template v-else-if="isError">
      <div v-if="errorStatus === 403" class="text-center py-5">
        <p class="fw-semibold mb-3">Bạn không có quyền xem lô này</p>
        <RouterLink :to="{ name: 'farmer-home' }" class="small d-inline-flex align-items-center gap-1">
          <FontAwesomeIcon icon="arrow-left" aria-hidden="true" /> Lô của tôi
        </RouterLink>
      </div>
      <div v-else-if="errorStatus === 404" class="text-center py-5">
        <p class="fw-semibold mb-3">Không tìm thấy lô</p>
        <RouterLink :to="{ name: 'farmer-home' }" class="small d-inline-flex align-items-center gap-1">
          <FontAwesomeIcon icon="arrow-left" aria-hidden="true" /> Lô của tôi
        </RouterLink>
      </div>
      <BAlert v-else variant="danger" class="d-flex gap-2 align-items-center mt-3">
        <FontAwesomeIcon icon="circle-exclamation" aria-hidden="true" />
        <span>Không tải được lô: {{ problem?.message }}</span>
        <BButton size="sm" variant="outline-danger" @click="refetch()">Thử lại</BButton>
      </BAlert>
    </template>

    <template v-else-if="data">
      <div class="d-flex flex-wrap gap-3 align-items-center mb-3 mt-2">
        <h1 class="h4 fw-bold mb-0">{{ data.batchCode }}</h1>
        <SoftBadge v-if="statusMeta" :variant="statusMeta.tone" :label="statusMeta.label" />
      </div>

      <BAlert v-if="locked" variant="warning">
        <FontAwesomeIcon icon="lock" aria-hidden="true" /> Lô đã khóa dữ liệu. Không thể thêm hoặc sửa nhật ký canh tác.
      </BAlert>

      <section class="border rounded-3 bg-body p-3 mb-3">
        <div class="row g-3">
          <div class="col-12">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="warehouse" aria-hidden="true" /> Nhà màng</div>
            <div class="fw-semibold">{{ data.greenhouseCode }} · {{ data.greenhouseName }}</div>
          </div>
          <div class="col-12">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="seedling" aria-hidden="true" /> Giống</div>
            <div class="fw-semibold">{{ data.varietyName }}</div>
          </div>
          <div class="col-6">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="calendar-days" aria-hidden="true" /> Ngày xuống giống</div>
            <div>{{ formatDate(data.sowDate) }}</div>
          </div>
          <div class="col-6">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="calendar-days" aria-hidden="true" /> Ngày thụ phấn</div>
            <div>{{ formatDate(data.pollinationDate) }}</div>
          </div>
          <div class="col-6">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="hourglass-half" aria-hidden="true" /> Thu hoạch dự kiến</div>
            <div>{{ formatDate(data.expectedHarvestDate) }}</div>
          </div>
          <div class="col-6">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="box-open" aria-hidden="true" /> Ngày thu hoạch thực tế</div>
            <div>{{ formatDate(data.harvestDate) }}</div>
          </div>
          <div class="col-6">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="list-check" aria-hidden="true" /> Số sự kiện</div>
            <div>{{ data.eventCount }}</div>
          </div>
        </div>
      </section>

      <section class="border rounded-3 bg-body p-3">
        <div class="d-flex flex-wrap gap-2 justify-content-between align-items-center mb-2">
          <p class="fw-semibold mb-0"><FontAwesomeIcon icon="clock-rotate-left" aria-hidden="true" /> Nhật ký canh tác</p>
          <BButton
            v-if="isFarmer && !locked"
            size="sm"
            variant="outline-primary"
            @click="router.push({ name: 'batch-log', params: { id } })"
          >
            <FontAwesomeIcon icon="plus" aria-hidden="true" /> Ghi nhật ký
          </BButton>
        </div>
        <EventTimeline :batch-id="id" />
        <p class="fw-semibold mt-3 mb-2">Công việc</p>
        <TaskList :batch-id="id" :is-engineer="false" :is-farmer="isFarmer" :locked="locked" :workers="[]" />
      </section>
    </template>
  </div>
</template>
