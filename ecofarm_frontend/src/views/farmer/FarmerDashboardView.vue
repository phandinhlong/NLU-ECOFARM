<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import SoftBadge from '@/components/common/SoftBadge.vue'
import { useMyBatchesQuery } from '@/composables/useBatches'
import { STATUS_META, formatDate, type Batch } from '@/api/types/batch'

const router = useRouter()
const { data, isLoading, isError, error, refetch } = useMyBatchesQuery()

const batches = computed<Batch[]>(() => data.value?.content ?? [])
const statusOf = (batch: Batch) => STATUS_META[batch.status]

const openBatch = (batch: Batch) => {
  router.push({ name: 'farmer-batch-detail', params: { id: batch.id } })
}
</script>

<template>
  <div>
    <div class="d-flex flex-wrap gap-2 justify-content-between align-items-center mb-3">
      <h1 class="h4 fw-bold mb-0">Lô của tôi</h1>
      <SoftBadge variant="info" :label="`${batches.length} lô`" />
    </div>

    <div v-if="isLoading" class="d-flex justify-content-center py-5">
      <BSpinner />
    </div>

    <BAlert v-else-if="isError" variant="danger" class="d-flex gap-2 align-items-center">
      <span>Không tải được danh sách lô: {{ error?.message }}</span>
      <BButton size="sm" variant="outline-danger" @click="refetch()">Thử lại</BButton>
    </BAlert>

    <div v-else-if="batches.length === 0" class="text-center text-body-secondary py-5">
      <FontAwesomeIcon icon="inbox" aria-hidden="true" />
      <p class="mb-0 mt-2">Bạn chưa được phân công lô nào</p>
    </div>

    <div v-else class="row g-3">
      <div v-for="batch in batches" :key="batch.id" class="col-12 col-md-6 col-lg-4">
        <div
          class="border rounded-3 bg-body p-3 h-100 d-flex flex-column gap-2 cursor-pointer"
          role="button"
          tabindex="0"
          @click="openBatch(batch)"
          @keyup.enter="openBatch(batch)"
        >
          <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between">
            <span class="font-monospace fw-bold">{{ batch.batchCode }}</span>
            <SoftBadge :variant="statusOf(batch).tone" :label="statusOf(batch).label" />
          </div>
          <div class="small text-body-secondary">
            <FontAwesomeIcon icon="warehouse" aria-hidden="true" />
            {{ batch.greenhouseCode }} · {{ batch.greenhouseName }}
          </div>
          <div class="small">
            <FontAwesomeIcon icon="seedling" aria-hidden="true" />
            {{ batch.varietyName }}
          </div>
          <div class="small text-body-secondary">
            <FontAwesomeIcon icon="calendar-days" aria-hidden="true" />
            Xuống giống: {{ formatDate(batch.sowDate) }}
          </div>
          <div v-if="batch.expectedHarvestDate" class="small text-body-secondary">
            <FontAwesomeIcon icon="hourglass-half" aria-hidden="true" />
            Thu hoạch dự kiến: {{ formatDate(batch.expectedHarvestDate) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}
</style>
