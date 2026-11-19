<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { batchService } from '@/api/services/batch.service'
import { attachmentService } from '@/api/services/attachment.service'
import type { Batch } from '@/api/types/batch'
import type { Problem } from '@/api/types/batch'
import {
  useHarvestMutation,
  useLatestQcQuery,
  useRecordQcMutation,
  useReviewQcMutation,
} from '@/composables/useQc'
import { useUserStore } from '@/stores'

interface SampleRow {
  brix: number | null
  weight: number | null
  morphology: string
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const roles = computed(() => userStore.user?.role ?? [])
const isAdmin = computed(() => roles.value.includes('ADMIN'))
const canReviewQc = computed(() => roles.value.includes('ADMIN') || roles.value.includes('ENGINEER') || roles.value.includes('QUALITY_CONTROL'))

// Batch selection
const selectedBatchId = ref<number | null>(null)
const batchList = ref<Batch[]>([])
const isLoadingBatches = ref(false)

// Tab: 'qc' | 'harvest'
const activeTab = ref<'qc' | 'harvest'>('qc')

// Batch details & QC Query
const {
  data: latestQc,
  isLoading: isLoadingQc,
  refetch: refetchQc,
} = useLatestQcQuery(selectedBatchId)

// Selected batch object
const currentBatch = computed(() =>
  batchList.value.find((b) => b.id === selectedBatchId.value) ?? null,
)

// 5 Sample inputs
const sampleRows = ref<SampleRow[]>([
  { brix: null, weight: null, morphology: 'Lưới nổi đều >90%, vân gân sâu' },
  { brix: null, weight: null, morphology: 'Lưới kín cuống, cuống khô đạt chuẩn' },
  { brix: null, weight: null, morphology: 'Lưới mịn, nền vỏ ngả vàng cam đều' },
  { brix: null, weight: null, morphology: 'Hương thơm đặc trưng, vỏ căng chắc' },
  { brix: null, weight: null, morphology: 'Thịt quả giòn chặt, ruột cam đậm màu' },
])

// Engineer decision form
const engineerPassed = ref(true)
const harvestDate = ref('')
const qcNote = ref('')
const photoFiles = ref<File[]>([])
const photoHashes = ref<string[]>([])
const isHashingPhotos = ref(false)

// Toast / alert feedback
const submitSuccessMessage = ref('')
const submitErrorMessage = ref('')

// Admin Review state
const adminDecision = ref<'APPROVED' | 'REJECTED'>('APPROVED')
const adminNote = ref('')
const adminReviewError = ref('')

// Harvest Form state
const harvestDateTime = ref(new Date().toISOString().slice(0, 16))
const harvestQuantity = ref<number | null>(150)
const harvestArea = ref('Khu A - nhà màng 3')
const harvestNote = ref('')
const harvestSuccess = ref<any>(null)
const harvestError = ref('')

// Mutations
const recordQcMutation = useRecordQcMutation()
const reviewQcMutation = useReviewQcMutation()
const harvestMutation = useHarvestMutation()

// Load batches for dropdown
const loadBatches = async () => {
  isLoadingBatches.value = true
  try {
    const growingRes = await batchService.listBatches({ status: 'GROWING' })
    const passedRes = await batchService.listBatches({ status: 'QC_PASSED' })
    const combined = [...(growingRes?.content ?? []), ...(passedRes?.content ?? [])]
    const map = new Map<number, Batch>()
    combined.forEach((b) => map.set(b.id, b))
    batchList.value = Array.from(map.values())
  } catch (err) {
    console.error('Failed to load batch list', err)
  } finally {
    isLoadingBatches.value = false
  }
}

// Initial route param handling
onMounted(async () => {
  await loadBatches()
  const routeBatchId = route.params.id ? Number(route.params.id) : null
  if (routeBatchId) {
    selectedBatchId.value = routeBatchId
    if (!batchList.value.some((b) => b.id === routeBatchId)) {
      try {
        const direct = await batchService.getBatch(routeBatchId)
        if (direct) batchList.value.push(direct)
      } catch (err) {
        console.error('Failed to load direct batch', err)
      }
    }
  } else if (batchList.value.length > 0) {
    selectedBatchId.value = batchList.value[0]?.id ?? null
  }
})

watch(
  () => route.params.id,
  (newId) => {
    if (newId) selectedBatchId.value = Number(newId)
  },
)

// Pre-fill form when latest QC loads
watch(
  latestQc,
  (qc) => {
    if (qc) {
      if (qc.brixSamples && qc.brixSamples.length === 5) {
        qc.brixSamples.forEach((val, idx) => {
          if (sampleRows.value[idx]) sampleRows.value[idx].brix = val
        })
      }
      if (qc.weightSamples && qc.weightSamples.length === 5) {
        qc.weightSamples.forEach((val, idx) => {
          if (sampleRows.value[idx]) sampleRows.value[idx].weight = val
        })
      }
      if (qc.passed !== undefined && qc.passed !== null) {
        engineerPassed.value = qc.passed
      }
      if (qc.expectedHarvestDate) {
        harvestDate.value = qc.expectedHarvestDate
      }
      if (qc.adminNote) {
        adminNote.value = qc.adminNote
      }
    }
  },
  { immediate: true },
)

// Compute stats
const filledBrixCount = computed(
  () =>
    sampleRows.value.filter(
      (r) => typeof r.brix === 'number' && !isNaN(r.brix) && r.brix >= 0 && r.brix <= 30,
    ).length,
)

const filledWeightCount = computed(
  () =>
    sampleRows.value.filter(
      (r) => typeof r.weight === 'number' && !isNaN(r.weight) && r.weight > 0 && r.weight <= 20,
    ).length,
)

const filledSampleCount = computed(() => Math.min(filledBrixCount.value, filledWeightCount.value))

const avgBrix = computed(() => {
  if (filledBrixCount.value !== 5) return null
  const sum = sampleRows.value.reduce((acc, r) => acc + (r.brix ?? 0), 0)
  return Math.round((sum / 5) * 10) / 10
})

const avgWeight = computed(() => {
  if (filledWeightCount.value !== 5) return null
  const sum = sampleRows.value.reduce((acc, r) => acc + (r.weight ?? 0), 0)
  return Math.round((sum / 5) * 100) / 100
})

// Evaluation standard checks: Brix 13.0 - 15.0
const brixCriteriaPassed = computed(() => {
  if (avgBrix.value === null) return null
  return avgBrix.value >= 13.0 && avgBrix.value <= 15.0
})

// Weight 1.2 - 1.8 kg
const weightCriteriaPassed = computed(() => {
  if (avgWeight.value === null) return null
  return avgWeight.value >= 1.2 && avgWeight.value <= 1.8
})

// Quarantine PHI
const quarantineDays = computed(() => {
  if (latestQc.value?.quarantineDaysActual) return latestQc.value.quarantineDaysActual
  return 18
})

const quarantinePassed = computed(() => {
  if (latestQc.value?.quarantineDaysDeficit !== undefined && latestQc.value?.quarantineDaysDeficit !== null) {
    return latestQc.value.quarantineDaysDeficit <= 0
  }
  return true
})

// System suggestion
const systemSuggestedPassed = computed(() => {
  if (brixCriteriaPassed.value === null || weightCriteriaPassed.value === null) return null
  return brixCriteriaPassed.value && weightCriteriaPassed.value && quarantinePassed.value
})

const isFormReady = computed(() => {
  return (
    selectedBatchId.value !== null &&
    filledBrixCount.value === 5 &&
    filledWeightCount.value === 5 &&
    !recordQcMutation.isPending.value
  )
})

// Photo upload & SHA-256 hash
const handlePhotoUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return

  isHashingPhotos.value = true
  try {
    const files = Array.from(input.files)
    photoFiles.value = files
    const hashes: string[] = []

    for (const file of files) {
      const buffer = await file.arrayBuffer()
      const hashBuffer = await crypto.subtle.digest('SHA-256', buffer)
      const hashArray = Array.from(new Uint8Array(hashBuffer))
      const hashHex = hashArray.map((b) => b.toString(16).padStart(2, '0')).join('')
      hashes.push(`sha256:${hashHex}`)
    }
    photoHashes.value = hashes
  } catch (err) {
    console.error('Error generating photo hash', err)
  } finally {
    isHashingPhotos.value = false
  }
}

// Submit QC Record
const handleSubmitQc = async () => {
  if (!selectedBatchId.value || !isFormReady.value) return

  submitSuccessMessage.value = ''
  submitErrorMessage.value = ''

  const clientEventId = crypto.randomUUID()
  const brixSamples = sampleRows.value.map((r) => r.brix!)
  const weightSamples = sampleRows.value.map((r) => r.weight!)

  try {
    const result = await recordQcMutation.mutateAsync({
      batchId: selectedBatchId.value,
      data: {
        clientEventId,
        brixSamples,
        weightSamples,
        passed: engineerPassed.value,
        harvestDate: harvestDate.value || null,
        note: qcNote.value || null,
        photoHashes: photoHashes.value.length > 0 ? photoHashes.value : undefined,
        occurredAt: Date.now(),
      },
    })

    const uploadedCount = photoFiles.value.length
    if (uploadedCount > 0 && result.eventId) {
      for (const file of photoFiles.value) {
        try {
          await attachmentService.createAttachment({
            eventId: result.eventId,
            file,
            capturedAt: Date.now(),
          })
        } catch (uploadErr) {
          console.error('Lỗi tải ảnh minh chứng qua module attachment:', uploadErr)
        }
      }
    }

    submitSuccessMessage.value = `Ghi nhận QC lần ${result.attemptNumber || 1} thành công! Trạng thái: ${result.status}${uploadedCount > 0 ? ` (Đã tải ${uploadedCount} ảnh qua module attachment)` : ''}`
    photoFiles.value = []
    photoHashes.value = []
    await refetchQc()
    await loadBatches()
  } catch (err: any) {
    const problem = err?.response?.data as Problem | undefined
    submitErrorMessage.value =
      problem?.message || err?.message || 'Có lỗi xảy ra khi lưu kết quả QC. Vui lòng thử lại.'
  }
}

// Admin Review Action
const handleAdminReview = async () => {
  if (!selectedBatchId.value || !adminNote.value.trim()) {
    adminReviewError.value = 'Vui lòng nhập ghi chú duyệt'
    return
  }

  adminReviewError.value = ''
  try {
    await reviewQcMutation.mutateAsync({
      batchId: selectedBatchId.value,
      data: {
        decision: adminDecision.value,
        note: adminNote.value.trim(),
      },
    })
    await refetchQc()
    await loadBatches()
  } catch (err: any) {
    const problem = err?.response?.data as Problem | undefined
    adminReviewError.value =
      problem?.message || err?.message || 'Có lỗi khi duyệt kết quả QC. Vui lòng kiểm tra lại.'
  }
}

// Harvest submission
const handleHarvest = async () => {
  if (!selectedBatchId.value) return

  harvestError.value = ''
  harvestSuccess.value = null

  try {
    const result = await harvestMutation.mutateAsync({
      batchId: selectedBatchId.value,
      data: {
        clientEventId: crypto.randomUUID(),
        harvestedAt: harvestDateTime.value ? new Date(harvestDateTime.value).getTime() : Date.now(),
        estimatedQuantity: harvestQuantity.value || null,
        area: harvestArea.value || null,
        note: harvestNote.value || null,
      },
    })
    harvestSuccess.value = result
    await loadBatches()
  } catch (err: any) {
    const problem = err?.response?.data as Problem | undefined
    harvestError.value =
      problem?.message || err?.message || 'Không thể thực hiện thu hoạch. Vui lòng thử lại.'
  }
}

const selectBatch = (id: number) => {
  selectedBatchId.value = id
  router.replace({ query: { ...route.query, batchId: id } })
}
</script>

<template>
  <div class="qc-harvest-container">
    <!-- Top Header -->
    <div class="card p-3 p-lg-4 mb-4 bg-body border shadow-sm rounded-3">
      <div class="d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
          <h1 class="h4 fw-bold text-body mb-1">Kiểm định chất lượng & Thu hoạch</h1>
        </div>

        <!-- Batch Selector -->
        <div class="d-flex align-items-center gap-2">
          <label for="batchSelect" class="small fw-semibold text-body-secondary text-nowrap mb-0">
            Lô canh tác:
          </label>
          <select
            id="batchSelect"
            :value="selectedBatchId"
            class="form-select form-select-sm"
            style="min-width: 280px"
            :disabled="isLoadingBatches"
            @change="(e) => selectBatch(Number((e.target as HTMLSelectElement).value))"
          >
            <option v-if="isLoadingBatches" :value="null">Đang tải danh sách lô...</option>
            <option v-else-if="batchList.length === 0" :value="null">Không có lô nào</option>
            <option v-for="b in batchList" :key="b.id" :value="b.id">
              {{ b.batchCode }} — {{ b.varietyName }} ({{ b.greenhouseCode }}) [{{ b.status }}]
            </option>
          </select>
        </div>
      </div>

      <!-- Navigation Tabs: Chữ màu trắng, khi hover / active mới màu xanh -->
      <div class="d-flex gap-2 mt-4 pt-3 border-top">
        <button
          type="button"
          class="qc-nav-tab"
          :class="{ active: activeTab === 'qc' }"
          @click="activeTab = 'qc'"
        >
          1. Ghi nhận QC
        </button>
        <button
          type="button"
          class="qc-nav-tab"
          :class="{ active: activeTab === 'harvest' }"
          @click="activeTab = 'harvest'"
        >
          2. Thu hoạch
        </button>
      </div>
    </div>

    <!-- Alert if no batch selected -->
    <div v-if="!selectedBatchId" class="alert alert-info py-2 px-3" role="alert">
      <span>Vui lòng chọn lô canh tác từ danh sách phía trên để bắt đầu quy trình.</span>
    </div>

    <!-- Loading State for QC -->
    <div v-else-if="isLoadingQc" class="d-flex justify-content-center py-5">
      <BSpinner variant="primary" />
    </div>

    <!-- TAB 1: GHI NHẬN QC -->
    <div v-else-if="activeTab === 'qc'">
      <!-- QC Status Banner if exists -->
      <div v-if="latestQc" class="card p-3 mb-4 border bg-body shadow-sm">
        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <div class="d-flex align-items-center gap-3">
            <span
              v-if="latestQc.status === 'QC_PASSED'"
              class="badge bg-success-subtle text-success fs-6 px-3 py-2 border border-success-subtle"
            >
              ĐÃ ĐẠT QC (QC_PASSED)
            </span>
            <span
              v-else-if="latestQc.status === 'PENDING_REVIEW'"
              class="badge bg-warning-subtle text-warning fs-6 px-3 py-2 border border-warning-subtle"
            >
              CHỜ ADMIN DUYỆT (PENDING_REVIEW)
            </span>
            <span
              v-else-if="latestQc.status === 'FAILED'"
              class="badge bg-danger-subtle text-danger fs-6 px-3 py-2 border border-danger-subtle"
            >
              CHƯA ĐẠT (FAILED)
            </span>
            <span
              v-else-if="latestQc.status === 'REJECTED'"
              class="badge bg-danger text-white fs-6 px-3 py-2"
            >
              BỊ LOẠI (REJECTED)
            </span>
            <span v-else class="badge bg-secondary-subtle text-secondary fs-6 px-3 py-2">
              {{ latestQc.status }}
            </span>

            <span class="badge bg-primary-subtle text-primary border border-primary-subtle">
              Lần kiểm tra: #{{ latestQc.attemptNumber }}/3
            </span>
          </div>

          <div class="small text-body-secondary">
            Cập nhật lần cuối:
            <span class="fw-semibold">{{
              latestQc.inspectedAt ? new Date(latestQc.inspectedAt).toLocaleString('vi-VN') : '—'
            }}</span>
          </div>
        </div>

        <div v-if="latestQc.failureReason" class="text-danger small mt-2">
          <strong>Lý do chưa đạt:</strong> {{ latestQc.failureReason }}
        </div>

        <!-- Admin Review Form for PENDING_REVIEW -->
        <div
          v-if="latestQc.status === 'PENDING_REVIEW' && canReviewQc"
          class="mt-3 pt-3 border-top bg-light-subtle p-3 rounded-2"
        >
          <h6 class="fw-bold text-primary mb-2">
            Phê duyệt QC
          </h6>
          <p class="small text-body-secondary mb-3">
            Lô dưa thiếu sự kiện ghi nhận cách ly hoặc cần xác nhận thủ công từ Quản trị viên.
          </p>

          <div class="row g-2 align-items-center">
            <div class="col-auto">
              <div class="btn-group" role="group">
                <input
                  id="decisionApprove"
                  v-model="adminDecision"
                  type="radio"
                  class="btn-check"
                  value="APPROVED"
                />
                <label class="btn btn-sm btn-outline-success" for="decisionApprove">
                  Duyệt đạt (APPROVED)
                </label>

                <input
                  id="decisionReject"
                  v-model="adminDecision"
                  type="radio"
                  class="btn-check"
                  value="REJECTED"
                />
                <label class="btn btn-sm btn-outline-danger" for="decisionReject">
                  Từ chối (REJECTED)
                </label>
              </div>
            </div>

            <div class="col">
              <input
                v-model="adminNote"
                type="text"
                class="form-control form-control-sm"
                placeholder="Nhập lý do duyệt / ghi chú (bắt buộc, 3–1000 ký tự)..."
              />
            </div>

            <div class="col-auto">
              <button
                class="btn btn-sm btn-primary"
                :disabled="reviewQcMutation.isPending.value || !adminNote.trim()"
                @click="handleAdminReview"
              >
                {{ reviewQcMutation.isPending.value ? 'Đang duyệt...' : 'Xác nhận duyệt' }}
              </button>
            </div>
          </div>
          <div v-if="adminReviewError" class="text-danger small mt-1">
            {{ adminReviewError }}
          </div>
        </div>
      </div>

      <!-- Feedback alerts -->
      <div v-if="submitSuccessMessage" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ submitSuccessMessage }}
        <button type="button" class="btn-close" @click="submitSuccessMessage = ''"></button>
      </div>

      <div v-if="submitErrorMessage" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ submitErrorMessage }}
        <button type="button" class="btn-close" @click="submitErrorMessage = ''"></button>
      </div>

      <!-- 3 Top Stat Cards -->
      <div class="row g-3 mb-4">
        <!-- Stat 1: Brix -->
        <div class="col-12 col-md-4">
          <div class="card p-3 border bg-body shadow-sm h-100">
            <div>
              <span class="text-uppercase fw-bold text-body-secondary small" style="font-size: 0.72rem">
                ĐỘ NGỌT TB (BRIX)
              </span>
              <div class="h3 fw-bold text-body mt-2 mb-1">
                {{ avgBrix !== null ? avgBrix.toFixed(1) : '—' }}
                <span class="fs-6 fw-normal text-body-secondary">°Bx</span>
              </div>
              <div class="small">
                <span v-if="brixCriteriaPassed === true" class="text-success fw-semibold">
                  Đạt chuẩn (13.0 – 15.0 °Bx)
                </span>
                <span v-else-if="brixCriteriaPassed === false" class="text-danger fw-semibold">
                  Ngoài chuẩn (13.0 – 15.0 °Bx)
                </span>
                <span v-else class="text-body-secondary">
                  Nhập đủ 5 mẫu (đã nhập {{ filledBrixCount }}/5)
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Stat 2: Weight -->
        <div class="col-12 col-md-4">
          <div class="card p-3 border bg-body shadow-sm h-100">
            <div>
              <span class="text-uppercase fw-bold text-body-secondary small" style="font-size: 0.72rem">
                TRỌNG LƯỢNG TB
              </span>
              <div class="h3 fw-bold text-body mt-2 mb-1">
                {{ avgWeight !== null ? avgWeight.toFixed(2) : '—' }}
                <span class="fs-6 fw-normal text-body-secondary">kg / quả</span>
              </div>
              <div class="small">
                <span v-if="weightCriteriaPassed === true" class="text-success fw-semibold">
                  Chuẩn hạng A (1.2 – 1.8 kg)
                </span>
                <span v-else-if="weightCriteriaPassed === false" class="text-danger fw-semibold">
                  Ngoài chuẩn (1.2 – 1.8 kg)
                </span>
                <span v-else class="text-body-secondary">
                  Nhập đủ 5 mẫu (đã nhập {{ filledWeightCount }}/5)
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Stat 3: PHI -->
        <div class="col-12 col-md-4">
          <div class="card p-3 border bg-body shadow-sm h-100">
            <div>
              <span class="text-uppercase fw-bold text-body-secondary small" style="font-size: 0.72rem">
                CÁCH LY PHÂN THUỐC (PHI)
              </span>
              <div class="h3 fw-bold text-body mt-2 mb-1">
                {{ quarantineDays }}
                <span class="fs-6 fw-normal text-body-secondary">ngày</span>
              </div>
              <div class="small">
                <span v-if="quarantinePassed" class="text-success fw-semibold">
                  An toàn xuất vườn (≥ 15 ngày)
                </span>
                <span v-else class="text-danger fw-semibold">
                  Chưa đủ điều kiện (≥ 15 ngày)
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Layout: 2 Columns -->
      <div class="row g-4">
        <!-- Left: 5 Samples Table -->
        <div class="col-12 col-lg-8">
          <div class="card border bg-body shadow-sm h-100 overflow-hidden">
            <div class="card-header bg-body border-bottom p-3 d-flex justify-content-between align-items-center">
              <div>
                <span class="text-uppercase fw-bold text-body-secondary small" style="font-size: 0.7rem">
                  SỐ LIỆU ĐO ĐẠC THỰC ĐỊA
                </span>
                <h6 class="fw-bold mb-0 text-body">Kiểm tra ngẫu nhiên 5 mẫu quả</h6>
              </div>
              <span class="badge bg-primary-subtle text-primary border border-primary-subtle">
                {{ filledSampleCount }}/5 MẪU ĐÃ ĐO
              </span>
            </div>

            <div class="table-responsive">
              <table class="table table-hover align-middle mb-0">
                <thead class="table-light text-uppercase small text-body-secondary" style="font-size: 0.72rem">
                  <tr>
                    <th style="width: 70px" class="ps-3">MẪU</th>
                    <th style="width: 140px">ĐỘ NGỌT (°BX)</th>
                    <th style="width: 140px">KHỐI LƯỢNG (KG)</th>
                    <th class="pe-3">ĐẶC ĐIỂM VỎ LƯỚI & HÌNH THÁI</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(row, idx) in sampleRows" :key="idx">
                    <td class="ps-3 fw-bold text-primary">#0{{ idx + 1 }}</td>
                    <td>
                      <div class="input-group input-group-sm">
                        <input
                          v-model.number="row.brix"
                          type="number"
                          step="0.1"
                          min="0"
                          max="30"
                          class="form-control fw-semibold"
                          :class="{
                            'is-valid': row.brix !== null && row.brix >= 13.0 && row.brix <= 15.0,
                            'is-invalid': row.brix !== null && (row.brix < 13.0 || row.brix > 15.0),
                          }"
                          placeholder="14.0"
                        />
                      </div>
                    </td>
                    <td>
                      <div class="input-group input-group-sm">
                        <input
                          v-model.number="row.weight"
                          type="number"
                          step="0.01"
                          min="0.1"
                          max="20"
                          class="form-control fw-semibold"
                          :class="{
                            'is-valid': row.weight !== null && row.weight >= 1.2 && row.weight <= 1.8,
                            'is-invalid': row.weight !== null && (row.weight < 1.2 || row.weight > 1.8),
                          }"
                          placeholder="1.6"
                        />
                      </div>
                    </td>
                    <td class="pe-3">
                      <input
                        v-model="row.morphology"
                        type="text"
                        class="form-control form-control-sm text-body-secondary"
                        placeholder="Mô tả vỏ lưới, cuống..."
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="card-footer bg-light-subtle p-3 d-flex justify-content-between align-items-center">
              <span class="small text-body-secondary">
                Tự động tính toán giá trị trung bình theo thời gian thực
              </span>
              <span class="small fw-semibold text-primary">
                {{ filledSampleCount }}/5 ĐÃ NHẬP
              </span>
            </div>
          </div>
        </div>

        <!-- Right: Assessment & Decision -->
        <div class="col-12 col-lg-4">
          <div class="d-flex flex-column gap-3">
            <!-- System Suggestion Card -->
            <div class="card border bg-body shadow-sm p-3">
              <span class="text-uppercase fw-bold text-body-secondary small" style="font-size: 0.7rem">
                ĐÁNH GIÁ TỰ ĐỘNG
              </span>
              <h6 class="fw-bold mb-3 text-body">Gợi ý hệ thống</h6>

              <div
                v-if="systemSuggestedPassed === true"
                class="alert alert-success mb-3 py-2 px-3"
              >
                <div class="fw-bold">Gợi ý: ĐẠT</div>
                <div class="small" style="font-size: 0.78rem">Đủ 3 điều kiện Brix, Cân nặng, Cách ly</div>
              </div>
              <div
                v-else-if="systemSuggestedPassed === false"
                class="alert alert-danger mb-3 py-2 px-3"
              >
                <div class="fw-bold">Gợi ý: CHƯA ĐẠT</div>
                <div class="small" style="font-size: 0.78rem">Một hoặc nhiều tiêu chí không thỏa mãn</div>
              </div>
              <div v-else class="alert alert-secondary mb-3 py-2 px-3">
                <span class="small">Nhập đủ 5 mẫu đo để nhận gợi ý</span>
              </div>

              <!-- Criteria list -->
              <ul class="list-group list-group-flush small">
                <li class="list-group-item px-0 d-flex justify-content-between align-items-center">
                  <span>Độ Brix (13–15 °Bx)</span>
                  <span v-if="brixCriteriaPassed === true" class="badge bg-success-subtle text-success">
                    {{ avgBrix }} °Bx (Đạt)
                  </span>
                  <span v-else-if="brixCriteriaPassed === false" class="badge bg-danger-subtle text-danger">
                    {{ avgBrix }} °Bx (Không đạt)
                  </span>
                  <span v-else class="text-body-secondary">Chưa đủ mẫu</span>
                </li>

                <li class="list-group-item px-0 d-flex justify-content-between align-items-center">
                  <span>Cân nặng (1,2–1,8 kg)</span>
                  <span v-if="weightCriteriaPassed === true" class="badge bg-success-subtle text-success">
                    {{ avgWeight }} kg (Đạt)
                  </span>
                  <span v-else-if="weightCriteriaPassed === false" class="badge bg-danger-subtle text-danger">
                    {{ avgWeight }} kg (Không đạt)
                  </span>
                  <span v-else class="text-body-secondary">Chưa đủ mẫu</span>
                </li>

                <li class="list-group-item px-0 d-flex justify-content-between align-items-center">
                  <span>Cách ly (≥ 15 ngày)</span>
                  <span v-if="quarantinePassed" class="badge bg-success-subtle text-success">
                    {{ quarantineDays }} ngày (Đạt)
                  </span>
                  <span v-else class="badge bg-danger-subtle text-danger">
                    {{ quarantineDays }} ngày (Thiếu)
                  </span>
                </li>
              </ul>
              <div class="text-body-secondary small fst-italic mt-2" style="font-size: 0.75rem">
                * Lưu ý: Gợi ý chỉ mang tính tham khảo. Quyết định cuối cùng thuộc về kỹ sư phụ trách.
              </div>
            </div>

            <!-- Engineer Decision Form -->
            <div class="card border bg-body shadow-sm p-3">
              <span class="text-uppercase fw-bold text-body-secondary small" style="font-size: 0.7rem">
                THẨM ĐỊNH KỸ THUẬT
              </span>
              <h6 class="fw-bold mb-3 text-body">Kết luận của Kỹ sư Nông học</h6>

              <!-- Toggle Confirm -->
              <div class="form-check form-switch p-3 bg-light-subtle rounded-3 mb-3 border">
                <input
                  id="passedSwitch"
                  v-model="engineerPassed"
                  class="form-check-input ms-0 me-3"
                  type="checkbox"
                  role="switch"
                  style="width: 2.5em; height: 1.3em"
                />
                <label class="form-check-label" for="passedSwitch">
                  <div class="fw-bold small text-body">Xác nhận nghiệm thu lô</div>
                  <div class="text-body-secondary small" style="font-size: 0.78rem">
                    {{
                      engineerPassed
                        ? 'Đạt quy chuẩn kỹ thuật cho phép thu hoạch'
                        : 'Chưa đạt — không cho phép thu hoạch'
                    }}
                  </div>
                </label>
              </div>

              <!-- Harvest Date -->
              <div class="mb-3">
                <label for="harvestDateInput" class="form-label small fw-semibold text-body-secondary">
                  NGÀY THU HOẠCH DỰ KIẾN
                </label>
                <input
                  id="harvestDateInput"
                  v-model="harvestDate"
                  type="date"
                  class="form-control form-control-sm"
                />
              </div>

              <!-- Proof Photo Upload -->
              <div class="mb-3">
                <label class="form-label small fw-semibold text-body-secondary">
                  ẢNH MINH CHỨNG ĐO ĐẠC
                </label>
                <div class="d-grid">
                  <label
                    class="btn btn-sm btn-outline-primary d-flex align-items-center justify-content-center"
                  >
                    <span>{{
                      photoFiles.length > 0 ? `Đã chọn ${photoFiles.length} ảnh` : 'Đính kèm ảnh khúc xạ kế & cân điện tử'
                    }}</span>
                    <input
                      type="file"
                      multiple
                      accept="image/*"
                      class="d-none"
                      @change="handlePhotoUpload"
                    />
                  </label>
                </div>
                <div v-if="photoFiles.length > 0" class="mt-2 small text-body-secondary">
                  <div v-for="f in photoFiles" :key="f.name" class="text-truncate">
                    {{ f.name }} ({{ Math.round(f.size / 1024) }} KB)
                  </div>
                </div>
              </div>

              <!-- Note -->
              <div class="mb-3">
                <label for="qcNoteInput" class="form-label small fw-semibold text-body-secondary">
                  GHI CHÚ BỔ SUNG
                </label>
                <textarea
                  id="qcNoteInput"
                  v-model="qcNote"
                  rows="3"
                  class="form-control form-control-sm"
                  placeholder="Mẫu đồng đều, thịt giòn ngọt đậm đà, độ đường ổn định, không vết nấm cuống..."
                ></textarea>
              </div>

              <!-- Submit Button -->
              <button
                class="btn btn-primary w-100 py-2 fw-semibold d-flex align-items-center justify-content-center"
                :disabled="!isFormReady"
                @click="handleSubmitQc"
              >
                <BSpinner v-if="recordQcMutation.isPending.value" small />
                <span v-else>Lưu kết quả QC & Cho phép Thu hoạch</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- TAB 2: THU HOẠCH -->
    <div v-else-if="activeTab === 'harvest'">
      <div v-if="currentBatch?.status !== 'QC_PASSED' && currentBatch?.status !== 'HARVESTED'" class="card p-4 border bg-body shadow-sm">
        <div class="alert alert-warning mb-0" role="alert">
          <div>
            <h6 class="fw-bold mb-1">Chưa đủ điều kiện thu hoạch</h6>
            <p class="small mb-2">
              Lô dưa cần vượt qua kiểm tra chất lượng (trạng thái
              <strong>QC_PASSED</strong>) trước khi có thể thực hiện thu hoạch.
            </p>
            <button class="btn btn-sm btn-outline-warning" @click="activeTab = 'qc'">
              Quay lại Tab Ghi nhận QC
            </button>
          </div>
        </div>
      </div>

      <div v-else class="card p-4 border bg-body shadow-sm">
        <div class="mb-3">
          <h5 class="fw-bold mb-0">Xác nhận Thu hoạch Lô</h5>
        </div>

        <div v-if="harvestSuccess" class="alert alert-success" role="alert">
          <h6 class="fw-bold mb-2">
            Thu hoạch thành công! Dữ liệu đã được neo lên blockchain.
          </h6>
          <div class="small">
            <div>Mã lô: <strong>{{ harvestSuccess.batchCode }}</strong></div>
            <div>Trạng thái: <strong>{{ harvestSuccess.status }}</strong></div>
            <div v-if="harvestSuccess.besuTxHash" class="mt-2">
              <span>Tx Hash: </span>
              <code class="text-break">{{ harvestSuccess.besuTxHash }}</code>
            </div>
            <div v-if="harvestSuccess.explorerUrl" class="mt-1">
              <a :href="harvestSuccess.explorerUrl" target="_blank" rel="noopener noreferrer" class="fw-semibold">
                Xem trên Blockchain Explorer ↗
              </a>
            </div>
          </div>
        </div>

        <div v-if="harvestError" class="alert alert-danger" role="alert">
          {{ harvestError }}
        </div>

        <div v-if="!harvestSuccess" class="row g-3">
          <div class="col-12 col-md-6">
            <label class="form-label small fw-semibold text-body-secondary">THỜI GIAN THU HOẠCH</label>
            <input v-model="harvestDateTime" type="datetime-local" class="form-control" />
          </div>

          <div class="col-12 col-md-6">
            <label class="form-label small fw-semibold text-body-secondary">SẢN LƯỢNG ƯỚC TÍNH (KG/QUẢ)</label>
            <input
              v-model.number="harvestQuantity"
              type="number"
              min="1"
              class="form-control"
              placeholder="150"
            />
          </div>

          <div class="col-12">
            <label class="form-label small fw-semibold text-body-secondary">KHU VỰC THU HOẠCH</label>
            <input
              v-model="harvestArea"
              type="text"
              class="form-control"
              placeholder="Khu A - nhà màng 3"
            />
          </div>

          <div class="col-12">
            <label class="form-label small fw-semibold text-body-secondary">GHI CHÚ THU HOẠCH</label>
            <textarea
              v-model="harvestNote"
              rows="3"
              class="form-control"
              placeholder="Thu hoạch sáng sớm, độ ẩm mát, cuống quả ráo..."
            ></textarea>
          </div>

          <div class="col-12 mt-4">
            <button
              class="btn btn-success w-100 py-2 fw-semibold d-flex align-items-center justify-content-center"
              :disabled="harvestMutation.isPending.value"
              @click="handleHarvest"
            >
              <BSpinner v-if="harvestMutation.isPending.value" small />
              <span v-else>Xác nhận Thu hoạch & Neo Blockchain Besu</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.qc-harvest-container {
  max-width: 1200px;
  margin: 0 auto;
}

.table > :not(caption) > * > * {
  padding: 0.75rem 0.5rem;
}

/* Tabs: Nền màu trắng, chữ đen, khi hover / active mới màu xanh dương */
.qc-nav-tab {
  background-color: #ffffff;
  color: #1a2332 !important;
  border: 1.5px solid #cbd5e1;
  border-radius: 8px;
  padding: 8px 20px;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
}

.qc-nav-tab:hover {
  color: #1d4ed8 !important;
  border-color: #1d4ed8;
  background-color: #eff6ff;
}

.qc-nav-tab.active {
  color: #1d4ed8 !important;
  border-color: #1d4ed8;
  background-color: #eff6ff;
  box-shadow: 0 0 0 1px #1d4ed8;
}
</style>
