<script setup lang="ts">
import { computed, ref, toRef } from 'vue'
import { useHarvestMutation, useQcListQuery, useRecordQcMutation } from '@/composables/useQuality'
import type { QcView } from '@/api/types/quality'

const props = defineProps<{ batchId: number; canQc: boolean; batchStatus: string }>()
const batchId = toRef(props, 'batchId')

const { data, isLoading, isError, error, refetch } = useQcListQuery(batchId)
const harvestMutation = useHarvestMutation(batchId)
const qcMutation = useRecordQcMutation(batchId)

const records = computed<QcView[]>(() => data.value ?? [])
const harvested = computed(
  () => ['HARVESTED', 'QC_PASSED', 'REJECTED'].includes(props.batchStatus),
)

// --- Harvest form ---
const harvestError = ref('')
const estimatedQuantity = ref<number | null>(null)
const area = ref('')
const submitHarvest = async () => {
  harvestError.value = ''
  if (!estimatedQuantity.value || estimatedQuantity.value < 1) {
    harvestError.value = 'Số quả ước tính phải là số dương.'
    return
  }
  try {
    await harvestMutation.mutateAsync({
      clientEventId: crypto.randomUUID(),
      harvestedAt: Date.now(),
      estimatedQuantity: estimatedQuantity.value,
      area: area.value.trim() || undefined,
    })
    estimatedQuantity.value = null
    area.value = ''
  } catch (e) {
    harvestError.value = (e as Error).message || 'Không ghi được thu hoạch.'
  }
}

// --- QC form ---
const qcError = ref('')
const brix = ref<number | null>(null)
const avgWeightKg = ref<number | null>(null)
const sizeDescription = ref('')
const note = ref('')
const confirmPassed = ref(false)
const submitQc = async () => {
  qcError.value = ''
  if (brix.value == null || avgWeightKg.value == null) {
    qcError.value = 'Brix và khối lượng trung bình là bắt buộc.'
    return
  }
  try {
    await qcMutation.mutateAsync({
      brix: brix.value,
      avgWeightKg: avgWeightKg.value,
      confirmPassed: confirmPassed.value,
      sizeDescription: sizeDescription.value.trim() || undefined,
      note: note.value.trim() || undefined,
    })
    brix.value = null
    avgWeightKg.value = null
    sizeDescription.value = ''
    note.value = ''
    confirmPassed.value = false
  } catch (e) {
    // The server returns 422 with an application/problem+json body for isolation/threshold rejects.
    const err = e as Error & { response?: { data?: { code?: string; title?: string } } }
    qcError.value = err.response?.data?.title || err.message || 'Không ghi được QC.'
  }
}

const fmtTime = (ms: number | null) => (ms == null ? '—' : new Date(ms).toLocaleString('vi-VN'))
</script>

<template>
  <BCard class="mt-4">
    <h5 class="mb-3"><FontAwesomeIcon icon="flask-vial" aria-hidden="true" /> Thu hoạch &amp; Kiểm định chất lượng</h5>

    <!-- Harvest -->
    <section v-if="canQc" class="mb-4">
      <p class="fw-semibold mb-2">Ghi nhận thu hoạch</p>
      <BAlert :model-value="!!harvestError" variant="danger" dismissible @close="harvestError = ''">
        {{ harvestError }}
      </BAlert>
      <div v-if="harvested" class="small text-body-secondary">Lô đã được thu hoạch.</div>
      <div v-else class="row g-2 align-items-end">
        <div class="col-6 col-md-4">
          <BFormGroup label="Số quả ước tính">
            <BFormInput v-model.number="estimatedQuantity" type="number" min="1" />
          </BFormGroup>
        </div>
        <div class="col-6 col-md-4">
          <BFormGroup label="Khu vực">
            <BFormInput v-model="area" />
          </BFormGroup>
        </div>
        <div class="col-12 col-md-4">
          <BButton variant="primary" :disabled="harvestMutation.isPending.value" @click="submitHarvest">
            {{ harvestMutation.isPending.value ? 'Đang ghi...' : 'Thu hoạch' }}
          </BButton>
        </div>
      </div>
    </section>

    <!-- QC form -->
    <section v-if="canQc && harvested" class="mb-4">
      <hr />
      <p class="fw-semibold mb-2">Nhập kiểm định (server tự tính đạt theo ngưỡng giống)</p>
      <BAlert :model-value="!!qcError" variant="danger" dismissible @close="qcError = ''">{{ qcError }}</BAlert>
      <div class="row g-2 align-items-end">
        <div class="col-6 col-md-3">
          <BFormGroup label="Brix">
            <BFormInput v-model.number="brix" type="number" step="0.1" min="0" max="30" />
          </BFormGroup>
        </div>
        <div class="col-6 col-md-3">
          <BFormGroup label="KL trung bình (kg)">
            <BFormInput v-model.number="avgWeightKg" type="number" step="0.01" min="0.1" max="20" />
          </BFormGroup>
        </div>
        <div class="col-12 col-md-3">
          <BFormGroup label="Mô tả kích thước">
            <BFormInput v-model="sizeDescription" />
          </BFormGroup>
        </div>
        <div class="col-12 col-md-3">
          <BFormGroup label="Ghi chú">
            <BFormInput v-model="note" />
          </BFormGroup>
        </div>
        <div class="col-12 d-flex gap-3 align-items-center">
          <BFormCheckbox v-model="confirmPassed">Kỹ sư xác nhận đạt</BFormCheckbox>
          <BButton variant="primary" :disabled="qcMutation.isPending.value" @click="submitQc">
            {{ qcMutation.isPending.value ? 'Đang ghi...' : 'Ghi kiểm định' }}
          </BButton>
        </div>
      </div>
    </section>

    <hr />

    <!-- QC history -->
    <p class="fw-semibold mb-2">Lịch sử kiểm định</p>
    <div v-if="isLoading" class="d-flex justify-content-center py-3"><BSpinner /></div>
    <BAlert v-else-if="isError" :model-value="true" variant="danger" class="d-flex gap-2 align-items-center">
      <span>Không tải được QC: {{ (error as Error)?.message }}</span>
      <BButton size="sm" variant="outline-danger" @click="refetch()">Thử lại</BButton>
    </BAlert>
    <p v-else-if="records.length === 0" class="small text-body-secondary mb-0">Chưa có kết quả kiểm định.</p>
    <div v-else class="table-responsive">
      <table class="table table-sm align-middle">
        <thead>
          <tr>
            <th>Thời điểm</th><th>Brix</th><th>KL (kg)</th><th>Kết quả</th><th>Ngưỡng Brix</th><th>Đủ điều kiện từ</th><th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in records" :key="r.id" :class="{ 'text-body-secondary': r.superseded }">
            <td>{{ fmtTime(r.inspectedAt) }}</td>
            <td>{{ r.brix }}</td>
            <td>{{ r.avgWeightKg }}</td>
            <td>
              <span v-if="r.status === 'QC_PASSED'" class="badge bg-success-subtle text-success-emphasis">Đạt</span>
              <span v-else class="badge bg-danger-subtle text-danger-emphasis">Không đạt</span>
            </td>
            <td>{{ r.thresholdBrixUsed ?? '—' }}</td>
            <td>{{ fmtTime(r.isolationEligibleAt) }}</td>
            <td>
              <span v-if="r.superseded" class="badge bg-warning-subtle text-warning-emphasis">Lỗi thời</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </BCard>
</template>
