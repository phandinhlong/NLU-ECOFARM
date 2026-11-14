<script setup lang="ts">
defineProps<{
  batchCode?: string
  varietyName?: string
  unitCount?: number
  brix?: number
  harvestDate?: string
  traceUrl?: string
  polygonBlock?: string
}>()
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
        <!-- QR Code SVG dùng chung cho cả lô dưa -->
        <div class="ef-qr-box bg-white p-1 rounded border flex-shrink-0 d-flex align-items-center justify-content-center">
          <svg width="68" height="68" viewBox="0 0 29 29" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="0" y="0" width="7" height="7" fill="#1e293b" />
            <rect x="1" y="1" width="5" height="5" fill="#ffffff" />
            <rect x="2" y="2" width="3" height="3" fill="#1e293b" />

            <rect x="22" y="0" width="7" height="7" fill="#1e293b" />
            <rect x="23" y="1" width="5" height="5" fill="#ffffff" />
            <rect x="24" y="2" width="3" height="3" fill="#1e293b" />

            <rect x="0" y="22" width="7" height="7" fill="#1e293b" />
            <rect x="1" y="23" width="5" height="5" fill="#ffffff" />
            <rect x="2" y="24" width="3" height="3" fill="#1e293b" />

            <rect x="8" y="2" width="2" height="2" fill="#1e293b" />
            <rect x="12" y="0" width="2" height="2" fill="#1e293b" />
            <rect x="16" y="2" width="2" height="2" fill="#1e293b" />
            <rect x="10" y="4" width="2" height="2" fill="#1e293b" />
            <rect x="18" y="4" width="2" height="2" fill="#1e293b" />

            <rect x="2" y="8" width="2" height="2" fill="#1e293b" />
            <rect x="4" y="10" width="2" height="2" fill="#1e293b" />
            <rect x="0" y="12" width="2" height="2" fill="#1e293b" />
            <rect x="4" y="14" width="2" height="2" fill="#1e293b" />

            <rect x="9" y="9" width="3" height="3" fill="#328bfa" />
            <rect x="14" y="8" width="2" height="2" fill="#1e293b" />
            <rect x="17" y="11" width="2" height="2" fill="#1e293b" />
            <rect x="13" y="14" width="3" height="3" fill="#1e293b" />

            <rect x="24" y="8" width="2" height="2" fill="#1e293b" />
            <rect x="22" y="11" width="2" height="2" fill="#1e293b" />
            <rect x="25" y="14" width="2" height="2" fill="#1e293b" />
            <rect x="20" y="16" width="2" height="2" fill="#1e293b" />

            <rect x="8" y="18" width="2" height="2" fill="#1e293b" />
            <rect x="11" y="20" width="2" height="2" fill="#1e293b" />
            <rect x="8" y="24" width="2" height="2" fill="#1e293b" />
            <rect x="14" y="23" width="2" height="2" fill="#1e293b" />
            <rect x="18" y="21" width="2" height="2" fill="#1e293b" />
            <rect x="20" y="24" width="2" height="2" fill="#1e293b" />
            <rect x="25" y="20" width="2" height="2" fill="#1e293b" />
            <rect x="23" y="25" width="2" height="2" fill="#1e293b" />
          </svg>
        </div>

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
        <span class="font-monospace text-primary text-truncate">{{ traceUrl || `ecofarm.vn/t/${batchCode || 'DL-NM01-141126'}` }}</span>
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
