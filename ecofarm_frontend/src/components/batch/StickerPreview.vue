<script setup lang="ts">
import { computed } from 'vue'
import { generateQrSvg } from '@/utils/qrcode'

const props = defineProps<{
  batchCode?: string
  varietyName?: string
  unitCount?: number
  brix?: number
  harvestDate?: string
  traceUrl?: string
  polygonBlock?: string
  polygonTxHash?: string
}>()

const targetQrUrl = computed(() => {
  // Priority 1: Polygon explorer link (on-chain, always public)
  if (props.polygonTxHash) {
    return `https://amoy.polygonscan.com/tx/${props.polygonTxHash}`
  }
  // Priority 2: Explicit trace URL that is not localhost
  if (props.traceUrl && props.traceUrl.startsWith('http') && !props.traceUrl.includes('localhost')) {
    return props.traceUrl
  }
  // Priority 3: Stable public trace URL (production domain)
  if (props.batchCode) {
    return `https://ecofarm.vn/t/${props.batchCode}`
  }
  return `https://ecofarm.vn`
})

const qrSvg = computed(() => {
  return generateQrSvg(targetQrUrl.value, 68)
})
</script>

<template>
  <div class="d-flex justify-content-center p-3 rounded-3 bg-body-tertiary border">
    <!-- Physical 50x30mm aspect ratio label: Chỉ 1 tem QR đại diện cho cả lô dưa -->
    <div id="ef-printable-single-sticker" class="ef-sticker-card position-relative bg-body p-2.5 rounded-2 border">
      <!-- Top header row -->
      <div class="d-flex align-items-center justify-content-between mb-2 pb-1 border-bottom">
        <div class="d-flex align-items-center gap-1.5">
          <span class="badge rounded-circle bg-primary text-white p-1 d-flex align-items-center justify-content-center" style="width: 16px; height: 16px; font-size: 8px;">
            EF
          </span>
          <span class="fw-bold small text-body lh-1" style="font-size: 11px;">EcoFarm</span>
        </div>

        <div class="d-flex align-items-center gap-1">
          <span class="badge bg-success-subtle text-success-emphasis border border-success-subtle ef-badge-micro">
            VietGAP
          </span>
          <span class="badge bg-primary-subtle text-primary-emphasis border border-primary-subtle ef-badge-micro">
            POLYGON
          </span>
        </div>
      </div>

      <!-- Center content: QR Cả Lô + Product Info -->
      <div class="d-flex align-items-center gap-2">
        <!-- QR Code SVG động lấy đúng đường link mạng Polygon -->
        <div
          class="ef-qr-box bg-white p-1 rounded border flex-shrink-0 d-flex align-items-center justify-content-center"
          v-html="qrSvg"
        />

        <!-- Info fields -->
        <div class="flex-grow-1 min-vw-0">
          <div class="text-uppercase fw-semibold text-body-secondary ef-sticker-sublabel">
            Tem truy xuất cả lô
          </div>
          <div class="fw-bold text-body text-truncate ef-sticker-title">
            {{ varietyName || 'Dưa lưới Vân Nhật' }}
          </div>
          <div class="text-body-secondary ef-sticker-meta text-truncate">
            Lô: {{ batchCode || 'DL-NM01-141126' }}
          </div>
          <div class="text-body-tertiary font-monospace ef-sticker-id text-truncate">
            Quy mô: {{ unitCount ? `${unitCount.toLocaleString('vi-VN')} quả` : '2.850 quả' }}
          </div>
          <div class="d-flex align-items-center gap-1.5 mt-1">
            <span class="badge bg-success ef-badge-micro">
              {{ brix ? `${brix}° Bx` : '14.2° Bx' }}
            </span>
            <span class="text-body-secondary ef-sticker-meta">
              Thu: {{ harvestDate || '15/01/27' }}
            </span>
          </div>
        </div>
      </div>

      <!-- Bottom footer row -->
      <div class="d-flex align-items-center justify-content-between mt-2 pt-1 border-top text-body-secondary ef-sticker-footer">
        <span class="font-monospace text-primary text-truncate">{{ targetQrUrl.replace('https://', '') }}</span>
        <span class="text-end text-truncate">{{ polygonBlock || 'Polygon #4,821,902' }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ef-sticker-card {
  width: 100%;
  max-width: 320px;
  background-color: var(--bs-body-bg);
  border: 1px dashed var(--bs-border-color) !important;
}

.ef-badge-micro {
  font-size: 8.5px;
  padding: 2px 4px;
  font-weight: 600;
  letter-spacing: 0.2px;
}

.ef-sticker-sublabel {
  font-size: 9px;
  letter-spacing: 0.4px;
}

.ef-sticker-title {
  font-size: 11.5px;
  line-height: 1.25;
}

.ef-sticker-meta {
  font-size: 9.5px;
  line-height: 1.3;
}

.ef-sticker-id {
  font-size: 9px;
  line-height: 1.2;
}

.ef-sticker-footer {
  font-size: 8.5px;
}
</style>
