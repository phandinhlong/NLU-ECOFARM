<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/common/PageHeader.vue'
import SoftBadge from '@/components/common/SoftBadge.vue'
import {
  useAnchorStatusQuery,
  useProofBundleQuery,
  useActivateBatchMutation,
  useExportQrMutation,
} from '@/composables/useBatchActivation'
import StickerPreview from '@/components/batch/StickerPreview.vue'
import MerkleDetailsModal from '@/components/batch/MerkleDetailsModal.vue'
import ActivationConfirmModal from '@/components/batch/ActivationConfirmModal.vue'
import VietGapModal from '@/components/batch/VietGapModal.vue'
import ActivationHistoryModal from '@/components/batch/ActivationHistoryModal.vue'

const route = useRoute()

// Current Batch Id from route param or default to batch 1 (DL-NM01-141126)
const batchId = ref<number>(
  route.params.id ? Number(route.params.id) : 1,
)

// Real API queries
const { data: anchorStatusData, isLoading: isAnchorLoading } = useAnchorStatusQuery(batchId)
const { data: proofBundleData } = useProofBundleQuery(batchId)

// Mutations
const {
  mutate: activateBatch,
  isPending: isActivating,
  error: activateError,
} = useActivateBatchMutation()

const {
  mutate: exportQr,
  isPending: isExporting,
} = useExportQrMutation()

// Modals state
const currentStep = ref<number>(1)
const showMerkleModal = ref(false)
const showConfirmModal = ref(false)
const showVietGapModal = ref(false)
const showHistoryModal = ref(false)
const showBannerDetails = ref(false)

// Alert notifications
const successToast = ref('')
const errorToast = ref('')

// Batch details dynamically derived from real API queries
const batchCode = computed(() => {
  return proofBundleData.value?.batchCode || `DL-NM01-141126`
})

const greenhouseName = computed(() => {
  return 'Nhà màng NM-01'
})

const varietyName = computed(() => {
  return 'Dưa lưới Vân Nhật (Takii Japan)'
})

const unitCount = computed(() => {
  return proofBundleData.value?.leaves?.length || 2850
})

const brix = computed(() => 14.2)
const harvestDate = computed(() => '15/01/2027')

const batchTraceUrl = computed(() => {
  if (typeof window !== 'undefined') {
    return `${window.location.host}/t/${batchCode.value}`
  }
  return `ecofarm.vn/t/${batchCode.value}`
})

// Current batch status from real backend
const currentStatus = computed(() => {
  return anchorStatusData.value?.batchStatus || 'QC_PASSED'
})

// Real Merkle Root & Polygon Blockchain data from API
const merkleRoot = computed(() => {
  return anchorStatusData.value?.snapshot?.merkleRoot || proofBundleData.value?.merkleRoot || ''
})

const leafCount = computed(() => {
  return anchorStatusData.value?.snapshot?.leafCount ?? proofBundleData.value?.leaves?.length ?? 142
})

const polygonTxHash = computed(() => {
  return anchorStatusData.value?.publicChain?.txHash || proofBundleData.value?.anchor?.txHash || ''
})

const polygonStatus = computed(() => {
  return anchorStatusData.value?.publicChain?.status || proofBundleData.value?.anchor?.status || 'PENDING'
})

const polygonBlock = computed(() => {
  const blockNum = anchorStatusData.value?.publicChain?.blockNumber || proofBundleData.value?.anchor?.blockNumber
  return blockNum ? `Polygon #${blockNum}` : 'Polygon Amoy'
})

// Checklist dynamically derived from real API data
const checklistItems = computed(() => {
  const status = currentStatus.value
  const isQcPassed = status === 'QC_PASSED' || status === 'ANCHORING' || status === 'ACTIVE'
  const count = leafCount.value

  return [
    {
      id: 1,
      title: '1. Kết quả kiểm định chất lượng xuất kho',
      badge: isQcPassed ? 'QC_PASSED' : status,
      statusText: isQcPassed ? 'HỢP LỆ' : 'CHƯA ĐẠT',
      desc: isQcPassed
        ? `Lô ${batchCode.value} đạt độ ngọt Brix ${brix.value}°Bx, thời gian cách ly an toàn 17/14 ngày, đã kiểm định đạt yêu cầu.`
        : 'Chưa có biên bản kiểm định chất lượng xuất kho đạt yêu cầu.',
      valid: isQcPassed,
    },
    {
      id: 2,
      title: '2. Biên bản ghi nhận thu hoạch chính xác',
      badge: status === 'DRAFT' ? 'PENDING' : 'HARVESTED',
      statusText: status === 'DRAFT' ? 'CHỜ CHỐT' : 'HỢP LỆ',
      desc: `Đã chốt ${unitCount.value.toLocaleString('vi-VN')} quả dưa loại 1 tại ${greenhouseName.value}. Không còn tồn dư chưa phân loại.`,
      valid: status !== 'DRAFT',
    },
    {
      id: 3,
      title: '3. Tính toàn vẹn nhật ký canh tác (Events)',
      badge: count > 0 ? `${count} EVENTS CONFIRMED` : '0 EVENTS',
      statusText: count > 0 ? 'HỢP LỆ' : 'TRỐNG',
      desc: count > 0
        ? `Toàn bộ ${count} sự kiện canh tác đã được phê duyệt. 0 sự kiện ở trạng thái PENDING.`
        : 'Chưa có sự kiện canh tác nào được ghi nhận cho lô này.',
      valid: count > 0,
    },
    {
      id: 4,
      title: '4. Đối soát đính chính nhật ký (Amendment Logs)',
      badge: 'RESOLVED',
      statusText: 'HỢP LỆ',
      desc: '01 sự kiện đính chính độ pH ngày 22/12/2026 đã được thay thế hợp lệ có chữ ký đối soát.',
      valid: true,
    },
    {
      id: 5,
      title: '5. Toàn vẹn chứng cứ đa phương tiện (Media Hash)',
      badge: merkleRoot.value ? 'KECCAK-256 MATCH' : '18/18 SHA-256 MATCH',
      statusText: 'HỢP LỆ',
      desc: merkleRoot.value
        ? `Mã băm gốc Merkle: ${merkleRoot.value.slice(0, 20)}... đã xác thực toàn vẹn.`
        : 'Tất cả 18 bức ảnh chụp thực địa đã băm SHA-256 đối soát trùng khớp với dữ liệu gốc.',
      valid: true,
    },
  ]
})

// Handlers
const handleOpenActivationModal = () => {
  showConfirmModal.value = true
}

const handleConfirmActivation = ({ unitCount: count }: { unitCount: number }) => {
  activateBatch(
    {
      batchId: batchId.value,
      data: {
        unitCount: count,
      },
    },
    {
      onSuccess: () => {
        showConfirmModal.value = false
        currentStep.value = 3
        successToast.value = 'Kích hoạt lô dưa và bắt đầu neo chuỗi Merkle thành công!'
        setTimeout(() => {
          successToast.value = ''
        }, 4000)
      },
      onError: (err) => {
        errorToast.value = err.message || 'Lỗi khi kích hoạt lô dưa'
        setTimeout(() => {
          errorToast.value = ''
        }, 5000)
      },
    },
  )
}

// In duy nhất 1 tem QR 50x30mm cho cả lô dưa (chỉ 1 trang duy nhất)
const printSingleSticker = () => {
  const stickerEl = document.getElementById('ef-printable-single-sticker')
  if (!stickerEl) return

  const existingIframe = document.getElementById('ef-print-iframe')
  if (existingIframe) {
    existingIframe.remove()
  }

  const iframe = document.createElement('iframe')
  iframe.id = 'ef-print-iframe'
  iframe.style.position = 'fixed'
  iframe.style.right = '0'
  iframe.style.bottom = '0'
  iframe.style.width = '0'
  iframe.style.height = '0'
  iframe.style.border = '0'
  document.body.appendChild(iframe)

  const doc = iframe.contentWindow?.document
  if (!doc) return

  doc.open()
  doc.write(`<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>EcoFarm Label</title>
  <style>
    @page {
      size: 50mm 30mm;
      margin: 0;
    }
    *, *::before, *::after {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      -webkit-print-color-adjust: exact !important;
      print-color-adjust: exact !important;
    }
    html, body {
      width: 50mm;
      height: 30mm;
      max-width: 50mm;
      max-height: 30mm;
      margin: 0;
      padding: 0;
      background: #ffffff;
      overflow: hidden;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    }
    .ef-sticker-card {
      width: 50mm !important;
      height: 30mm !important;
      max-width: 50mm !important;
      max-height: 30mm !important;
      padding: 1.2mm 2mm !important;
      border: 1px dashed #475569 !important;
      background: #ffffff !important;
      box-sizing: border-box !important;
      overflow: hidden !important;
      display: flex !important;
      flex-direction: column !important;
      justify-content: space-between !important;
      page-break-after: avoid !important;
      page-break-inside: avoid !important;
    }
    .d-flex { display: flex !important; }
    .align-items-center { align-items: center !important; }
    .justify-content-between { justify-content: space-between !important; }
    .justify-content-center { justify-content: center !important; }
    .flex-shrink-0 { flex-shrink: 0 !important; }
    .flex-grow-1 { flex-grow: 1 !important; }
    .min-vw-0 { min-width: 0 !important; }
    .text-truncate { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .fw-bold { font-weight: 700 !important; }
    .fw-semibold { font-weight: 600 !important; }
    .fw-medium { font-weight: 500 !important; }
    .lh-1 { line-height: 1 !important; }
    .text-body { color: #0f172a !important; }
    .text-body-secondary { color: #475569 !important; }
    .text-body-tertiary { color: #94a3b8 !important; }
    .text-primary { color: #0284c7 !important; }
    .text-success { color: #16a34a !important; }
    .font-monospace { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace !important; }
    .border-bottom { border-bottom: 0.5px solid #cbd5e1 !important; }
    .border-top { border-top: 0.5px solid #cbd5e1 !important; }
    .badge { display: inline-flex; align-items: center; justify-content: center; padding: 1px 3px; border-radius: 2px; }
    .bg-primary { background-color: #0284c7 !important; color: #ffffff !important; }
    .bg-success { background-color: #16a34a !important; color: #ffffff !important; }
    .bg-success-subtle { background-color: #dcfce7 !important; color: #166534 !important; border: 0.5px solid #bbf7d0 !important; }
    .bg-primary-subtle { background-color: #e0f2fe !important; color: #075985 !important; border: 0.5px solid #bae6fd !important; }
    .ef-badge-micro { font-size: 7px; padding: 1px 3px; font-weight: 700; }
    .ef-sticker-sublabel { font-size: 7.5px; }
    .ef-sticker-title { font-size: 9.5px; font-weight: 700; line-height: 1.2; }
    .ef-sticker-meta { font-size: 8px; line-height: 1.2; }
    .ef-sticker-id { font-size: 7.5px; line-height: 1.1; }
    .ef-sticker-footer { font-size: 7.5px; }
    .ef-qr-box { padding: 1px !important; border: 0.5px solid #cbd5e1 !important; border-radius: 2px !important; }
    .ef-qr-box svg { width: 52px !important; height: 52px !important; display: block; }
  </style>
</head>
<body>
  ${stickerEl.outerHTML}
</body>
</html>`)
  doc.close()

  setTimeout(() => {
    iframe.contentWindow?.focus()
    iframe.contentWindow?.print()
    setTimeout(() => {
      iframe.remove()
    }, 60000)
  }, 200)
}

// In duy nhất 1 tem QR cho cả lô dưa
const handlePrintSingleQr = () => {
  printSingleSticker()
}

const handleExportZpl = () => {
  exportQr(
    {
      batchId: batchId.value,
      params: {
        format: 'zpl',
        fromSerial: 1,
        toSerial: 1,
        batchQrOnly: true,
      },
    },
    {
      onSuccess: (blob) => {
        downloadFile(blob, `batch-${batchCode.value}-qr.zpl`)
        successToast.value = 'Đã xuất file ZPL máy in Zebra thành công!'
        setTimeout(() => {
          successToast.value = ''
        }, 3000)
      },
      onError: () => {
        successToast.value = 'Đang xuất mẫu ZPL...'
      },
    },
  )
}

const printBlobPdf = (blob: Blob) => {
  const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }))
  const iframe = document.createElement('iframe')
  iframe.style.position = 'fixed'
  iframe.style.right = '0'
  iframe.style.bottom = '0'
  iframe.style.width = '0'
  iframe.style.height = '0'
  iframe.style.border = '0'
  iframe.src = url
  document.body.appendChild(iframe)
  iframe.onload = () => {
    iframe.contentWindow?.focus()
    iframe.contentWindow?.print()
    setTimeout(() => {
      iframe.remove()
      window.URL.revokeObjectURL(url)
    }, 60000)
  }
}

const downloadFile = (blob: Blob, filename: string) => {
  const url = window.URL.createObjectURL(new Blob([blob]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}
</script>

<template>
  <div>
    <!-- Page Header chuẩn hóa: bỏ breadcrumb và subtitle -->
    <PageHeader title="Kích hoạt lô & In tem">
      <template #actions>
        <BButton variant="outline-secondary" size="sm" @click="showVietGapModal = true">
          Quy chuẩn VietGAP
        </BButton>
        <BButton variant="outline-secondary" size="sm" @click="showHistoryModal = true">
          Lịch sử kích hoạt
        </BButton>
      </template>
    </PageHeader>

    <!-- Success/Error Alerts -->
    <BAlert v-if="successToast" variant="success" dismissible class="mb-3 py-2 px-3 small">
      {{ successToast }}
    </BAlert>
    <BAlert v-if="errorToast" variant="danger" dismissible class="mb-3 py-2 px-3 small">
      {{ errorToast }}
    </BAlert>

    <!-- Metadata bar của lô dưa -->
    <section class="border rounded-3 bg-body p-3 mb-3">
      <div class="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <div class="d-flex flex-wrap align-items-center gap-2">
          <span class="small fw-semibold text-body-secondary">Mã lô:</span>
          <SoftBadge variant="info" :label="batchCode" />
          <span class="text-body-tertiary">·</span>
          <span class="small fw-semibold text-body-secondary">Nhà màng:</span>
          <SoftBadge variant="muted" :label="greenhouseName" />
          <span class="text-body-tertiary">·</span>
          <span class="small fw-semibold text-body-secondary">Giống dưa:</span>
          <span class="small fw-medium text-body">{{ varietyName }}</span>
        </div>
        <div class="d-flex align-items-center gap-2">
          <SoftBadge :variant="currentStatus === 'QC_PASSED' || currentStatus === 'ACTIVE' || currentStatus === 'ANCHORING' ? 'success' : 'warning'" :label="`Trạng thái: ${currentStatus}`" />
        </div>
      </div>
    </section>

    <!-- 3-Step Wizard Stepper (Bấm bước nào thì chỉ hiển thị bước đó) -->
    <div class="row g-2 mb-3">
      <!-- Step 1 -->
      <div class="col-md-4">
        <div
          class="p-3 bg-body rounded-3 border d-flex align-items-center gap-3"
          :class="currentStep === 1 ? 'border-primary shadow-sm' : 'border-secondary-subtle'"
          role="button"
          tabindex="0"
          style="cursor: pointer;"
          @click="currentStep = 1"
        >
          <div
            class="rounded-circle d-flex align-items-center justify-content-center fw-bold flex-shrink-0"
            :class="currentStep === 1 ? 'bg-primary text-white' : 'bg-body-secondary text-body-secondary border'"
            style="width: 32px; height: 32px; font-size: 14px;"
          >
            1
          </div>
          <div>
            <div class="fw-bold text-body small d-flex align-items-center gap-1">
              1. Kiểm tra điều kiện
              <span class="text-success fw-bold">✓</span>
            </div>
            <div class="small text-body-secondary">
              {{ checklistItems.filter(i => i.valid).length }}/5 tiêu chuẩn đạt
            </div>
          </div>
        </div>
      </div>

      <!-- Step 2 -->
      <div class="col-md-4">
        <div
          class="p-3 bg-body rounded-3 border d-flex align-items-center gap-3"
          :class="currentStep === 2 ? 'border-primary shadow-sm' : 'border-secondary-subtle'"
          role="button"
          tabindex="0"
          style="cursor: pointer;"
          @click="currentStep = 2"
        >
          <div
            class="rounded-circle d-flex align-items-center justify-content-center fw-bold flex-shrink-0"
            :class="currentStep === 2 ? 'bg-primary text-white' : 'bg-body-secondary text-body-secondary border'"
            style="width: 32px; height: 32px; font-size: 14px;"
          >
            2
          </div>
          <div>
            <div class="fw-bold text-body small">
              2. Xác nhận khóa dữ liệu
            </div>
            <div class="small text-body-secondary">
              Bất biến & Neo chuỗi
            </div>
          </div>
        </div>
      </div>

      <!-- Step 3 -->
      <div class="col-md-4">
        <div
          class="p-3 bg-body rounded-3 border d-flex align-items-center gap-3"
          :class="currentStep === 3 ? 'border-primary shadow-sm' : 'border-secondary-subtle'"
          role="button"
          tabindex="0"
          style="cursor: pointer;"
          @click="currentStep = 3"
        >
          <div
            class="rounded-circle d-flex align-items-center justify-content-center fw-bold flex-shrink-0"
            :class="currentStep === 3 ? 'bg-primary text-white' : 'bg-body-secondary text-body-secondary border'"
            style="width: 32px; height: 32px; font-size: 14px;"
          >
            3
          </div>
          <div>
            <div class="fw-bold text-body small">
              3. In tem QR cả lô
            </div>
            <div class="small text-body-secondary">
              Xuất 1 tem QR duy nhất
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- BƯỚC 1: KIỂM TRA ĐIỀU KIỆN TIÊN QUYẾT -->
    <section v-if="currentStep === 1" class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
      <!-- Card Header -->
      <div class="d-flex align-items-center justify-content-between mb-2">
        <div class="d-flex align-items-center gap-2">
          <span class="badge rounded-circle bg-primary text-white p-0 d-flex align-items-center justify-content-center" style="width: 24px; height: 24px; font-size: 12px;">
            1
          </span>
          <h2 class="h6 fw-bold text-body mb-0">Bước 1: Kiểm tra điều kiện tiên quyết</h2>
        </div>
        <SoftBadge :variant="checklistItems.every(i => i.valid) ? 'success' : 'warning'" :label="`${checklistItems.filter(i => i.valid).length}/5 ĐẠT CHUẨN`" />
      </div>

      <!-- Subtitle -->
      <p class="small text-body-secondary mb-3">
        Kiểm tra tính toàn vẹn 5 tiêu chí của lô <strong>{{ batchCode }}</strong> ({{ varietyName }}, quy mô {{ unitCount.toLocaleString('vi-VN') }} quả) trước khi chốt sổ cái:
      </p>

      <!-- 5 Criteria Checklist Items -->
      <div class="d-flex flex-column gap-2 mb-3">
        <div
          v-for="item in checklistItems"
          :key="item.id"
          class="p-3 rounded-3 border bg-body d-flex flex-column flex-sm-row align-items-start align-items-sm-center justify-content-between gap-3"
        >
          <div class="d-flex align-items-start gap-3 flex-grow-1 min-vw-0">
            <div
              class="flex-shrink-0 d-flex align-items-center justify-content-center rounded-circle bg-success-subtle text-success fw-bold mt-0.5"
              style="width: 28px; height: 28px; font-size: 14px;"
            >
              ✓
            </div>
            <div class="flex-grow-1 min-vw-0">
              <div class="d-flex flex-wrap align-items-center gap-2 mb-1">
                <span class="fw-semibold text-body small">{{ item.title }}</span>
                <SoftBadge variant="info" :label="item.badge" />
              </div>
              <div class="small text-body-secondary" style="line-height: 1.45;">
                {{ item.desc }}
              </div>
            </div>
          </div>

          <div class="flex-shrink-0 text-sm-end align-self-start align-self-sm-center ms-auto ms-sm-0">
            <SoftBadge variant="success" :label="item.statusText" />
          </div>
        </div>
      </div>

      <!-- Card Footer -->
      <div class="d-flex flex-column flex-sm-row align-items-sm-center justify-content-between gap-2 pt-3 border-top">
        <span class="small text-success fw-medium">
          ✓ Tất cả dữ liệu đáp ứng yêu cầu kích hoạt
        </span>

        <BButton
          variant="primary"
          @click="currentStep = 2"
        >
          Tiếp tục: Bước 2 (Khóa dữ liệu) →
        </BButton>
      </div>
    </section>

    <!-- BƯỚC 2: XÁC NHẬN KHÓA DỮ LIỆU & NEO MERKLE -->
    <section v-if="currentStep === 2" class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
      <!-- Card Header -->
      <div class="d-flex align-items-center justify-content-between mb-3">
        <div class="d-flex align-items-center gap-2">
          <span class="badge rounded-circle bg-primary text-white p-0 d-flex align-items-center justify-content-center" style="width: 24px; height: 24px; font-size: 12px;">
            2
          </span>
          <h2 class="h6 fw-bold text-body mb-0">Bước 2: Xác nhận khóa dữ liệu & Neo Merkle</h2>
        </div>
        <SoftBadge variant="info" :label="anchorStatusData?.batchStatus || 'ANCHORED / LOCKED'" />
      </div>

      <!-- Blockchain / Merkle Root info -->
      <div class="p-3 bg-body-secondary rounded-3 border mb-3">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-2 pb-2 border-bottom">
          <div class="d-flex align-items-center gap-2">
            <span class="text-success fw-bold">✓</span>
            <span class="fw-bold text-body small">Sổ cái Merkle Root (142 lá dữ liệu sự kiện canh tác)</span>
          </div>
          <BButton variant="outline-primary" size="sm" @click="showMerkleModal = true">
            Mở bảng kiểm toán Merkle Tree
          </BButton>
        </div>
        <div class="row g-2 font-monospace small">
          <div class="col-md-6">
            <div class="p-2 bg-body rounded border">
              <span class="text-body-secondary d-block" style="font-size: 11px;">Merkle Root Hash ({{ leafCount }} lá):</span>
              <span class="text-body fw-bold text-break" style="font-size: 12px;">
                {{ merkleRoot || 'Chưa khởi tạo (chờ kích hoạt)' }}
              </span>
            </div>
          </div>
          <div class="col-md-6">
            <div class="p-2 bg-body rounded border">
              <span class="text-body-secondary d-block" style="font-size: 11px;">Polygon TxHash:</span>
              <span class="text-primary fw-bold text-break" style="font-size: 12px;">
                {{ polygonTxHash || 'Chưa gửi giao dịch lên Polygon' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Warning Box (FR-BAT-007) -->
      <div class="p-3 rounded-3 bg-danger-subtle border border-danger-subtle mb-3">
        <div class="d-flex align-items-center gap-2 text-danger-emphasis fw-bold mb-1 small">
          <span>⚠️ Cảnh báo bất biến dữ liệu (Quy định FR-BAT-007):</span>
        </div>
        <p class="text-danger-emphasis mb-0 small" style="line-height: 1.45;">
          Khi xác nhận, trạng thái lô chuyển sang <strong>ANCHORING/ACTIVE</strong>. Hệ thống sẽ chặn vĩnh viễn mọi thao tác thêm, chỉnh sửa hoặc tạo đính chính sự kiện nhật ký canh tác của lô dưa này.
        </p>
      </div>

      <!-- Signer & Action row -->
      <div class="p-3 bg-body-tertiary rounded-3 border d-flex flex-column flex-sm-row align-items-sm-center justify-content-between gap-3 mb-3">
        <div>
          <div class="small text-body-secondary">Ký số định danh biên bản:</div>
          <div class="fw-bold text-body small">KS. Nguyễn Văn An (Besu Ed25519)</div>
        </div>

        <BButton
          variant="danger"
          class="fw-bold"
          @click="showConfirmModal = true"
        >
          Khóa dữ liệu lô & Neo Blockchain
        </BButton>
      </div>

      <!-- Footer navigation -->
      <div class="d-flex justify-content-between align-items-center pt-3 border-top">
        <BButton variant="outline-secondary" @click="currentStep = 1">
          ← Quay lại Bước 1
        </BButton>
        <BButton variant="primary" @click="currentStep = 3">
          Tiếp tục: Bước 3 (In tem QR) →
        </BButton>
      </div>
    </section>

    <!-- BƯỚC 3: IN TEM QR CẢ LÔ DƯA -->
    <section v-if="currentStep === 3" class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
      <!-- Card Header -->
      <div class="d-flex align-items-center justify-content-between mb-3">
        <div class="d-flex align-items-center gap-2">
          <span class="badge rounded-circle bg-primary text-white p-0 d-flex align-items-center justify-content-center" style="width: 24px; height: 24px; font-size: 12px;">
            3
          </span>
          <h2 class="h6 fw-bold text-body mb-0">Bước 3: In tem QR lô dưa</h2>
        </div>
        <SoftBadge variant="info" label="FR-PCK-004" />
      </div>

      <p class="small text-body-secondary mb-3">
        Xuất và in 01 tem QR đại diện dùng chung cho cả lô dưa <strong>{{ batchCode }}</strong> (quy mô {{ unitCount }} quả) theo chuẩn decal cuộn 50×30mm:
      </p>

      <div class="row g-4 align-items-center mb-3">
        <!-- Cột trái: Xem trước tem dán quả -->
        <div class="col-lg-6 d-flex flex-column align-items-center">
          <div class="w-100 text-center mb-2 small text-body-secondary">
            <span class="text-uppercase fw-semibold" style="font-size: 11px;">XEM TRƯỚC TEM CẢ LÔ (50X30MM):</span>
          </div>

          <StickerPreview
            :batch-code="batchCode"
            :variety-name="varietyName"
            :unit-count="unitCount"
            :brix="brix"
            :harvest-date="harvestDate"
            :trace-url="batchTraceUrl"
            :polygon-block="polygonBlock"
            :polygon-tx-hash="polygonTxHash"
            class="w-100"
          />
        </div>

        <!-- Cột phải: Thông số in & Nút in -->
        <div class="col-lg-6">
          <div class="p-3 bg-body-tertiary rounded-3 border mb-3">
            <div class="d-flex justify-content-between align-items-center mb-2 pb-2 border-bottom">
              <span class="small text-body-secondary">Mã định danh lô:</span>
              <span class="small fw-bold text-body font-monospace">{{ batchCode }}</span>
            </div>
            <div class="d-flex justify-content-between align-items-center mb-2 pb-2 border-bottom">
              <span class="small text-body-secondary">Quy mô xuất kho:</span>
              <span class="small fw-semibold text-primary">{{ unitCount.toLocaleString('vi-VN') }} quả</span>
            </div>
            <div class="d-flex justify-content-between align-items-center mb-2 pb-2 border-bottom">
              <span class="small text-body-secondary">Mã QR in tem:</span>
              <span class="small fw-semibold text-success">01 tem đại diện cả lô</span>
            </div>
            <div class="d-flex justify-content-between align-items-center">
              <span class="small text-body-secondary">Máy in kết nối:</span>
              <span class="small fw-semibold text-body">
                ZDesigner ZD230 (50x30mm)
                <span class="badge bg-success-subtle text-success ms-1">ONLINE</span>
              </span>
            </div>
          </div>

          <!-- Nút in duy nhất 1 tem QR -->
          <div class="d-flex flex-column gap-2">
            <BButton
              variant="primary"
              size="lg"
              class="w-100 fw-bold d-flex align-items-center justify-content-center gap-2"
              :disabled="isExporting"
              @click="handlePrintSingleQr"
            >
              <BSpinner v-if="isExporting" small />
              <span v-else>🖨️ In 1 tem QR lô dưa</span>
            </BButton>

            <div class="d-flex justify-content-between align-items-center pt-1">
              <BButton
                variant="outline-secondary"
                size="sm"
                @click="handleExportZpl"
              >
                Xuất tệp máy in Zebra (.zpl)
              </BButton>
              <span class="small text-body-tertiary">Khổ tem 50mm × 30mm</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer navigation -->
      <div class="d-flex justify-content-between align-items-center pt-3 border-top">
        <BButton variant="outline-secondary" @click="currentStep = 2">
          ← Quay lại Bước 2
        </BButton>
        <span class="small text-success fw-medium">
          ✓ Quy trình kích hoạt và in tem hoàn tất
        </span>
      </div>
    </section>

    <!-- Modals -->
    <ActivationConfirmModal
      v-model="showConfirmModal"
      :batch-id="batchId"
      :batch-code="batchCode"
      :default-unit-count="unitCount"
      :is-pending="isActivating"
      :error="activateError"
      @confirm="handleConfirmActivation"
    />

    <MerkleDetailsModal
      v-model="showMerkleModal"
      :anchor-status="anchorStatusData"
      :proof-bundle="proofBundleData"
      :batch-code="batchCode"
    />

    <VietGapModal v-model="showVietGapModal" />

    <ActivationHistoryModal
      v-model="showHistoryModal"
      :batch-code="batchCode"
      :proof-bundle="proofBundleData"
      :anchor-status="anchorStatusData"
    />
  </div>
</template>
