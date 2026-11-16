<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { batchService } from '@/api/services/batch.service'
import type { PublicTrace, VerifyResult, TimelineEvent } from '@/api/types/batch'

const route = useRoute()

const isBlockchainOpen = ref(true)
const inputCode = ref('')
const currentCode = ref('')
const traceData = ref<PublicTrace | null>(null)
const verifyData = ref<VerifyResult | null>(null)
const isLoading = ref(false)
const errorMessage = ref('')
const showCertModal = ref(false)

const fetchTrace = async (codeToFetch: string) => {
  const code = codeToFetch.trim()
  if (!code) return

  isLoading.value = true
  errorMessage.value = ''

  try {
    const res = await batchService.getPublicTrace(code)
    traceData.value = res.data
    currentCode.value = code

    try {
      const verifyRes = await batchService.verifyTrace(code)
      verifyData.value = verifyRes.data
    } catch {
      verifyData.value = null
    }
  } catch (err: any) {
    errorMessage.value =
      err?.response?.data?.detail ||
      err?.response?.data?.message ||
      'Không tìm thấy thông tin cho mã tem dưa này hoặc hệ thống máy chủ đang bận.'
  } finally {
    isLoading.value = false
  }
}

const handleSearch = () => {
  if (inputCode.value.trim()) {
    fetchTrace(inputCode.value.trim())
  }
}

const formatDate = (val?: string | number | null) => {
  if (!val) return '—'
  try {
    const d = typeof val === 'number' ? new Date(val) : new Date(val)
    if (isNaN(d.getTime())) return String(val)
    const day = String(d.getDate()).padStart(2, '0')
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const y = d.getFullYear()
    return `${day}/${m}/${y}`
  } catch {
    return String(val)
  }
}

const formatDateTime = (val?: string | number | null) => {
  if (!val) return '—'
  try {
    const d = typeof val === 'number' ? new Date(val) : new Date(val)
    if (isNaN(d.getTime())) return String(val)
    const day = String(d.getDate()).padStart(2, '0')
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const y = String(d.getFullYear()).slice(-2)
    const hh = String(d.getHours()).padStart(2, '0')
    const mm = String(d.getMinutes()).padStart(2, '0')
    return `${day}/${m}/${y} ${hh}:${mm}`
  } catch {
    return String(val)
  }
}

const formatCertYear = (val?: string) => {
  if (!val) return '—'
  try {
    const d = new Date(val)
    return isNaN(d.getTime()) ? '—' : String(d.getFullYear())
  } catch {
    return '—'
  }
}

const displayVarietyName = computed(() => {
  return traceData.value?.variety?.name || traceData.value?.batch?.varietyName || '—'
})

const displayVarietyCode = computed(() => {
  const c = traceData.value?.variety?.code
  return c ? `Giống ${c}` : ''
})

const displayBatchCode = computed(() => {
  return traceData.value?.batch?.batchCode || currentCode.value || '—'
})

const displayBrix = computed(() => {
  const b = traceData.value?.metrics?.brix
  return b !== undefined && b !== null ? `${b}°` : '—'
})

const displayWeight = computed(() => {
  const w = traceData.value?.metrics?.avgWeightKg
  return w !== undefined && w !== null ? `${w}` : '—'
})

const displayLocation = computed(() => {
  return traceData.value?.greenhouse?.location || traceData.value?.greenhouse?.name || '—'
})

const displayCertNo = computed(() => {
  return traceData.value?.certificates?.[0]?.certNo || '—'
})

const displayCertValidYear = computed(() => {
  return formatCertYear(traceData.value?.certificates?.[0]?.validTo)
})

const getStepTitle = (eventType?: string, seq?: number) => {
  const t = eventType?.toUpperCase() || ''
  if (t.includes('GIEO') || t.includes('SOW')) return `Bước ${seq || 1}: Xuống giống hạt & Ươm mầm`
  if (t.includes('THU_PHAN') || t.includes('POLLIN')) return `Bước ${seq || 2}: Thụ phấn & Tuyển trái`
  if (t.includes('CACH_LY') || t.includes('ISOLAT')) return `Bước ${seq || 3}: Ngừng bón phân cách ly`
  if (t.includes('CHAM_SOC') || t.includes('CARE')) return `Bước ${seq || 3}: Chăm sóc dinh dưỡng chuẩn`
  if (t.includes('KIEM_DINH') || t.includes('BRIX') || t.includes('QC')) return `Bước ${seq || 4}: Đo Brix & Kiểm định chất lượng`
  if (t.includes('THU_HOACH') || t.includes('HARVEST')) return `Bước ${seq || 5}: Thu hoạch quả`
  if (t.includes('DONG_GOI') || t.includes('PACK')) return `Bước ${seq || 5}: Đóng gói & Dán tem QR`
  return `Bước ${seq || 1}: ${eventType || 'Ghi nhận canh tác'}`
}

const growthSteps = computed(() => {
  const events = traceData.value?.timeline || []
  return events.map((ev: TimelineEvent, idx: number) => {
    const seq = ev.seq ?? idx + 1
    const type = ev.eventType || ''
    return {
      step: seq,
      title: getStepTitle(type, seq),
      date: formatDate(ev.recordedAt),
      status: ev.status ? `Trạng thái: ${ev.status}` : '',
    }
  })
})

const displayMerkleRoot = computed(() => {
  return verifyData.value?.computedRoot || traceData.value?.blockchain?.merkleRoot || '—'
})

const displayOnChainRoot = computed(() => {
  return verifyData.value?.onChainRoot || traceData.value?.blockchain?.merkleRoot || '—'
})

const displayBlockNumber = computed(() => {
  return verifyData.value?.blockNumber ? `#${verifyData.value.blockNumber.toLocaleString('en-US')}` : '—'
})

const displayAnchoredTime = computed(() => {
  const time = verifyData.value?.anchoredAt || traceData.value?.blockchain?.anchoredAt
  return time ? formatDateTime(time) : '—'
})

const displayTxHash = computed(() => {
  return verifyData.value?.txHash || traceData.value?.blockchain?.txHash || '—'
})

const displayExplorerUrl = computed(() => {
  const tx = displayTxHash.value
  if (tx && tx !== '—') {
    return `https://amoy.polygonscan.com/tx/${tx}`
  }
  return ''
})

const isVerifiedMatch = computed(() => {
  return verifyData.value?.result?.toUpperCase() === 'MATCH'
})

const verifyStatusText = computed(() => {
  if (!verifyData.value) return 'Đang cập nhật'
  if (verifyData.value.result?.toUpperCase() === 'MATCH') return 'Khớp 100% (Match)'
  return verifyData.value.result || 'Chưa đối soát'
})

onMounted(() => {
  const codeParam = (route.params.code as string) || (route.query.code as string)
  if (codeParam) {
    inputCode.value = codeParam.trim()
    fetchTrace(codeParam.trim())
  }
})

watch(
  () => route.params.code,
  (newCode) => {
    if (newCode && typeof newCode === 'string') {
      inputCode.value = newCode.trim()
      fetchTrace(newCode.trim())
    }
  }
)
</script>

<template>
  <div class="trace-page-bg">
    <div class="trace-mobile-container">
      <header class="app-header">
        <div class="brand-left">
          <div class="brand-avatar">
            <svg viewBox="0 0 24 24" fill="none" class="brand-leaf-icon">
              <path
                d="M12 3C7.03 3 3 7.03 3 12c0 2.22.81 4.25 2.15 5.82L15.82 7.15C14.25 5.81 12.22 3 12 3zm6.85 3.18L8.18 16.85C9.75 18.19 11.78 21 12 21c4.97 0 9-4.03 9-9 0-2.22-.81-4.25-2.15-5.82z"
                fill="#059669"
              />
            </svg>
          </div>
          <div class="brand-info">
            <div class="brand-name-row">
              <span class="brand-title">EcoFarm</span>
              <svg viewBox="0 0 24 24" fill="#2563eb" class="verify-check-icon">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z" />
              </svg>
            </div>
            <span class="brand-sub">MELON ORIGIN PASS</span>
          </div>
        </div>

        <div class="brand-actions">
          <span class="pass-status-pill">
            <svg viewBox="0 0 24 24" fill="#059669" class="pass-shield-icon">
              <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z" />
            </svg>
            Minh bạch chuỗi
          </span>
        </div>
      </header>

      <div v-if="errorMessage" class="error-banner">
        <span>{{ errorMessage }}</span>
        <button type="button" class="btn-retry" @click="handleSearch">Thử lại</button>
      </div>

      <main class="page-content">
        <div v-if="!currentCode && !isLoading" class="search-box-card">
          <div class="search-box-title">Tra cứu nguồn gốc dưa lưới</div>
          <div class="search-input-group">
            <input
              v-model="inputCode"
              type="text"
              placeholder="Nhập mã lô hoặc mã tem (VD: DL-NM01-141126)..."
              class="search-input"
              @keyup.enter="handleSearch"
            />
            <button
              type="button"
              class="btn-search-main"
              :disabled="isLoading || !inputCode.trim()"
              @click="handleSearch"
            >
              Tra cứu
            </button>
          </div>
        </div>

        <article class="hero-product-card">
          <div class="hero-top-badge-row">
            <div class="verified-badge-pill">
              <svg viewBox="0 0 24 24" fill="#059669" class="badge-check-svg">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z" />
              </svg>
              <span>{{ isVerifiedMatch ? 'Đã xác thực nguồn gốc' : 'Nguồn gốc chuẩn EcoFarm' }}</span>
            </div>
            <span class="active-status-tag">{{ traceData?.batch?.status || 'ACTIVE' }}</span>
          </div>

          <div class="hero-details">
            <h1 class="melon-title">{{ displayVarietyName }}</h1>
            <div class="melon-meta">
              <span class="meta-batch">Mã lô: <strong>{{ displayBatchCode }}</strong></span>
              <template v-if="displayVarietyCode">
                <span class="meta-dot">•</span>
                <span class="meta-seed">{{ displayVarietyCode }}</span>
              </template>
            </div>

            <div class="metrics-grid">
              <div class="metric-box">
                <span class="metric-label">ĐỘ NGỌT BRIX</span>
                <div class="metric-value-row">
                  <span class="metric-number text-blue">{{ displayBrix }}</span>
                  <span v-if="displayBrix !== '—'" class="metric-unit text-blue">Bx</span>
                </div>
                <span class="metric-desc">Đạt chuẩn xuất khẩu</span>
              </div>

              <div class="metric-box">
                <span class="metric-label">CÂN NẶNG</span>
                <div class="metric-value-row">
                  <span class="metric-number text-dark">{{ displayWeight }}</span>
                  <span v-if="displayWeight !== '—'" class="metric-unit text-dark">kg</span>
                </div>
                <span class="metric-desc">Chuẩn quả tuyển chọn</span>
              </div>
            </div>

            <div class="farm-location-row">
              <svg viewBox="0 0 24 24" fill="#64748b" class="row-icon">
                <path d="M12 3L2 12h3v8h14v-8h3L12 3zm0 4.5l5 4.5h-2v6h-6v-6H7l5-4.5z" />
              </svg>
              <span class="farm-name">{{ displayLocation }}</span>
            </div>

            <div class="vietgap-cert-row" @click="showCertModal = true">
              <div class="cert-left">
                <svg viewBox="0 0 24 24" fill="#059669" class="cert-shield-icon">
                  <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z" />
                </svg>
                <span class="cert-text">
                  Chứng nhận VietGAP <strong>#{{ displayCertNo }}</strong>
                </span>
              </div>
              <span class="cert-valid-tag">Hiệu lực {{ displayCertValidYear }}</span>
            </div>
          </div>
        </article>

        <section class="growth-journey-card">
          <div class="journey-header">
            <div class="journey-title-wrap">
              <h2 class="journey-heading">Hành Trình Sinh Trưởng</h2>
              <p class="journey-sub">
                {{ growthSteps.length > 0 ? `${growthSteps.length} giai đoạn chăm sóc minh bạch tại nhà màng` : 'Nhật ký chăm sóc minh bạch tại nhà màng' }}
              </p>
            </div>
            <svg viewBox="0 0 24 24" fill="none" class="journey-header-leaf">
              <path
                d="M12 3C7.03 3 3 7.03 3 12c0 2.22.81 4.25 2.15 5.82L15.82 7.15C14.25 5.81 12.22 3 12 3zm6.85 3.18L8.18 16.85C9.75 18.19 11.78 21 12 21c4.97 0 9-4.03 9-9 0-2.22-.81-4.25-2.15-5.82z"
                fill="#2563eb"
              />
            </svg>
          </div>

          <div v-if="growthSteps.length > 0" class="steps-timeline">
            <div v-for="st in growthSteps" :key="st.step" class="timeline-step-item">
              <div class="step-meta-head">
                <div class="step-title-left">
                  <span class="step-badge-num">{{ st.step }}</span>
                  <span class="step-name">{{ st.title }}</span>
                </div>
                <span class="step-date">{{ st.date }}</span>
              </div>
              <p v-if="st.status" class="step-description">{{ st.status }}</p>
            </div>
          </div>
          <div v-else class="empty-timeline-hint">
            <span>Chưa ghi nhận nhật ký canh tác cho lô này.</span>
          </div>
        </section>

        <section class="blockchain-card">
          <button
            type="button"
            class="blockchain-accordion-btn"
            @click="isBlockchainOpen = !isBlockchainOpen"
          >
            <div class="btn-inner-left">
              <svg viewBox="0 0 24 24" fill="currentColor" class="bc-shield-icon">
                <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z" />
              </svg>
              <span>Xác thực dữ liệu Blockchain</span>
            </div>
            <svg
              viewBox="0 0 24 24"
              fill="currentColor"
              class="arrow-icon"
              :class="{ rotated: isBlockchainOpen }"
            >
              <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z" />
            </svg>
          </button>

          <div v-show="isBlockchainOpen" class="blockchain-body">
            <div class="verify-status-row">
              <span class="status-title-label">TRẠNG THÁI KIỂM TRA</span>
              <span class="status-match-badge" :class="{ 'status-badge-pending': !isVerifiedMatch }">
                <svg v-if="isVerifiedMatch" viewBox="0 0 24 24" fill="currentColor" class="check-tiny">
                  <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
                </svg>
                {{ verifyStatusText }}
              </span>
            </div>

            <div class="bc-field-group">
              <span class="bc-field-label">MERKLE ROOT TÍNH TOÁN</span>
              <div class="bc-hash-box">
                {{ displayMerkleRoot }}
              </div>
            </div>

            <div class="bc-field-group">
              <span class="bc-field-label">KHÓA NEO ON-CHAIN POLYGON ANCHOR</span>
              <div class="bc-hash-box">
                {{ displayOnChainRoot }}
              </div>
            </div>

            <div class="bc-meta-row">
              <div class="bc-meta-col">
                <span class="meta-label-dim">Số khối:</span>
                <span class="meta-val-bold">{{ displayBlockNumber }}</span>
              </div>
              <div class="bc-meta-col text-end">
                <span class="meta-label-dim">Thời điểm:</span>
                <span class="meta-val-bold">{{ displayAnchoredTime }}</span>
              </div>
            </div>

            <div class="bc-tx-row">
              <span class="meta-label-dim">TxHash:</span>
              <a
                v-if="displayExplorerUrl"
                :href="displayExplorerUrl"
                target="_blank"
                rel="noopener noreferrer"
                class="tx-link-text"
              >
                {{ displayTxHash }}
              </a>
              <span v-else class="tx-link-text text-muted">{{ displayTxHash }}</span>
            </div>
          </div>
        </section>

        <footer class="consumer-footer">
          <div class="footer-divider"></div>
          <div class="footer-center">
            <div class="footer-title-row">
              <svg viewBox="0 0 24 24" fill="#059669" class="footer-shield-svg">
                <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z" />
              </svg>
              <span class="footer-title">CHỨNG NHẬN NLU ECOFARM</span>
            </div>
            <p class="footer-desc">
              Bảo hộ dữ liệu chuỗi cung ứng nông sản thông minh. Quét mã QR chính hãng để ngăn chặn hàng giả.
            </p>
            <p class="footer-copyright">
              © 2026 NLU EcoFarm Project. All rights reserved.
            </p>
          </div>
        </footer>
      </main>
    </div>

    <div v-if="showCertModal" class="modal-overlay" @click.self="showCertModal = false">
      <div class="modal-card">
        <div class="modal-head">
          <h3 class="modal-title">Giấy chứng nhận VietGAP</h3>
          <button type="button" class="btn-close-modal" @click="showCertModal = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="cert-card-badge-preview">
            <svg viewBox="0 0 24 24" fill="#059669" class="cert-modal-shield">
              <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z" />
            </svg>
            <div class="cert-badge-headline">TIÊU CHUẨN VIETGAP CHÍNH THỨC</div>
            <div class="cert-badge-sub">Mã số: {{ displayCertNo }}</div>
          </div>
          <div class="modal-cert-meta">
            <p><strong>Mã chứng nhận:</strong> {{ displayCertNo }}</p>
            <p><strong>Loại chứng nhận:</strong> VietGAP Trồng trọt dưa lưới</p>
            <p><strong>Hiệu lực đến:</strong> Năm {{ displayCertValidYear }}</p>
            <p><strong>Đơn vị xác thực:</strong> NLU EcoFarm Smart Agriculture</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.trace-page-bg {
  min-height: 100vh;
  background-color: #f1f5f9;
  display: flex;
  justify-content: center;
  padding: 0;
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #0f172a;
}

.trace-mobile-container {
  width: 100%;
  max-width: 440px;
  min-height: 100vh;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  box-shadow: 0 0 25px rgba(0, 0, 0, 0.05);
}

@media (min-width: 768px) {
  .trace-mobile-container {
    max-width: 480px;
    margin: 16px auto;
    border-radius: 24px;
    overflow: hidden;
    min-height: calc(100vh - 32px);
  }
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 30;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
}

.brand-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #ecfdf5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-leaf-icon {
  width: 22px;
  height: 22px;
}

.brand-info {
  display: flex;
  flex-direction: column;
}

.brand-name-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.brand-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
}

.verify-check-icon {
  width: 14px;
  height: 14px;
}

.brand-sub {
  font-size: 10px;
  font-weight: 600;
  color: #94a3b8;
  letter-spacing: 0.5px;
}

.brand-actions {
  display: flex;
  align-items: center;
}

.pass-status-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  background-color: #ecfdf5;
  color: #059669;
  font-size: 11px;
  font-weight: 600;
  border-radius: 20px;
  border: 1px solid #d1fae5;
}

.pass-shield-icon {
  width: 13px;
  height: 13px;
}

.search-box-card {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
}

.search-box-title {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 10px;
}

.search-input-group {
  display: flex;
  gap: 8px;
}

.search-input {
  flex: 1;
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  font-size: 14px;
  outline: none;
  background-color: #f8fafc;
}

.search-input:focus {
  border-color: #2563eb;
  background-color: #ffffff;
}

.btn-search-main {
  padding: 10px 16px;
  background-color: #059669;
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
  border-radius: 10px;
  border: none;
  cursor: pointer;
}

.btn-search-main:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.error-banner {
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 12px 16px;
  margin: 12px 16px 0;
  border-radius: 12px;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.btn-retry {
  background-color: #dc2626;
  color: #ffffff;
  border: none;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.page-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-product-card {
  background-color: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f1f5f9;
  padding: 16px;
}

.hero-top-badge-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.verified-badge-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background-color: #ecfdf5;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  color: #059669;
  border: 1px solid #d1fae5;
}

.badge-check-svg {
  width: 14px;
  height: 14px;
}

.active-status-tag {
  font-size: 10px;
  font-weight: 700;
  color: #2563eb;
  background-color: #eff6ff;
  padding: 3px 8px;
  border-radius: 6px;
  letter-spacing: 0.5px;
}

.hero-details {
  display: flex;
  flex-direction: column;
}

.melon-title {
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 6px 0;
  letter-spacing: -0.3px;
}

.melon-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
  margin-bottom: 16px;
}

.meta-batch strong {
  color: #2563eb;
}

.meta-dot {
  color: #cbd5e1;
}

.metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}

.metric-box {
  background-color: #f8fafc;
  border-radius: 12px;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  border: 1px solid #f1f5f9;
}

.metric-label {
  font-size: 10px;
  font-weight: 700;
  color: #94a3b8;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}

.metric-value-row {
  display: flex;
  align-items: baseline;
  gap: 3px;
  margin-bottom: 4px;
}

.metric-number {
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
}

.metric-number.text-blue {
  color: #1e40af;
}

.metric-number.text-dark {
  color: #0f172a;
}

.metric-unit {
  font-size: 12px;
  font-weight: 700;
}

.metric-unit.text-blue {
  color: #3b82f6;
}

.metric-unit.text-dark {
  color: #64748b;
}

.metric-desc {
  font-size: 10px;
  color: #94a3b8;
}

.farm-location-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
  border-top: 1px solid #f8fafc;
  font-size: 12px;
  color: #475569;
}

.row-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.farm-name {
  font-weight: 500;
}

.vietgap-cert-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ecfdf5;
  border: 1px solid #d1fae5;
  border-radius: 10px;
  padding: 9px 12px;
  cursor: pointer;
  transition: background-color 0.15s;
}

.vietgap-cert-row:hover {
  background-color: #d1fae5;
}

.cert-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cert-shield-icon {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
}

.cert-text {
  font-size: 12px;
  color: #065f46;
}

.cert-valid-tag {
  font-size: 10px;
  color: #047857;
  font-weight: 600;
}

.growth-journey-card {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f1f5f9;
}

.journey-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
}

.journey-heading {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 4px 0;
}

.journey-sub {
  font-size: 11px;
  color: #94a3b8;
  margin: 0;
}

.journey-header-leaf {
  width: 20px;
  height: 20px;
}

.steps-timeline {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.timeline-step-item {
  display: flex;
  flex-direction: column;
  padding-bottom: 14px;
  border-bottom: 1px solid #f1f5f9;
}

.timeline-step-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.step-meta-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.step-title-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.step-badge-num {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #2563eb;
  color: #ffffff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.step-name {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}

.step-date {
  font-size: 11px;
  color: #94a3b8;
}

.step-description {
  font-size: 12px;
  color: #64748b;
  margin: 0;
  line-height: 1.4;
}

.empty-timeline-hint {
  padding: 24px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
  background-color: #f8fafc;
  border-radius: 12px;
}

.blockchain-card {
  background-color: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f1f5f9;
}

.blockchain-accordion-btn {
  width: 100%;
  padding: 14px 16px;
  background-color: #1d4ed8;
  color: #ffffff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.btn-inner-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bc-shield-icon {
  width: 16px;
  height: 16px;
}

.arrow-icon {
  width: 18px;
  height: 18px;
  transition: transform 0.2s ease;
}

.arrow-icon.rotated {
  transform: rotate(180deg);
}

.blockchain-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background-color: #ffffff;
}

.verify-status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 8px;
  border-bottom: 1px solid #f1f5f9;
}

.status-title-label {
  font-size: 10px;
  font-weight: 700;
  color: #64748b;
  letter-spacing: 0.5px;
}

.status-match-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background-color: #ecfdf5;
  color: #059669;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 12px;
  border: 1px solid #a7f3d0;
}

.status-badge-pending {
  background-color: #fef3c7;
  color: #b45309;
  border-color: #fde68a;
}

.check-tiny {
  width: 12px;
  height: 12px;
}

.bc-field-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.bc-field-label {
  font-size: 10px;
  font-weight: 700;
  color: #94a3b8;
  letter-spacing: 0.5px;
}

.bc-hash-box {
  background-color: #f8fafc;
  padding: 8px 10px;
  border-radius: 8px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 11px;
  color: #1e3a8a;
  word-break: break-all;
  line-height: 1.4;
  border: 1px solid #e2e8f0;
}

.bc-meta-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding-top: 4px;
}

.bc-meta-col {
  display: flex;
  align-items: baseline;
  gap: 6px;
  font-size: 11px;
}

.meta-label-dim {
  color: #94a3b8;
}

.meta-val-bold {
  font-weight: 700;
  color: #0f172a;
}

.text-end {
  justify-content: flex-end;
}

.bc-tx-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  font-size: 11px;
  padding-top: 4px;
  border-top: 1px solid #f8fafc;
}

.tx-link-text {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 11px;
  color: #2563eb;
  text-decoration: underline;
  word-break: break-all;
}

.text-muted {
  color: #94a3b8;
  text-decoration: none;
}

.consumer-footer {
  margin-top: 8px;
  padding: 16px 0 24px;
  text-align: center;
}

.footer-divider {
  width: 40px;
  height: 2px;
  background-color: #cbd5e1;
  margin: 0 auto 16px;
  border-radius: 2px;
}

.footer-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.footer-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.footer-shield-svg {
  width: 14px;
  height: 14px;
}

.footer-title {
  font-size: 11px;
  font-weight: 700;
  color: #059669;
  letter-spacing: 0.5px;
}

.footer-desc {
  font-size: 10px;
  color: #94a3b8;
  margin: 0;
  max-width: 320px;
  line-height: 1.4;
}

.footer-copyright {
  font-size: 10px;
  color: #cbd5e1;
  margin: 4px 0 0;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  background-color: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.modal-card {
  background-color: #ffffff;
  border-radius: 16px;
  max-width: 380px;
  width: 100%;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.modal-head {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f1f5f9;
}

.modal-title {
  font-size: 14px;
  font-weight: 700;
  margin: 0;
  color: #0f172a;
}

.btn-close-modal {
  background: none;
  border: none;
  font-size: 16px;
  color: #94a3b8;
  cursor: pointer;
  padding: 4px;
}

.modal-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cert-card-badge-preview {
  background-color: #ecfdf5;
  border: 1px solid #a7f3d0;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.cert-modal-shield {
  width: 36px;
  height: 36px;
}

.cert-badge-headline {
  font-size: 12px;
  font-weight: 800;
  color: #065f46;
  letter-spacing: 0.5px;
}

.cert-badge-sub {
  font-size: 11px;
  color: #047857;
  font-weight: 600;
}

.modal-cert-meta {
  font-size: 12px;
  color: #475569;
  line-height: 1.6;
}

.modal-cert-meta p {
  margin: 4px 0;
}
</style>
