<script setup lang="ts">
import type { AnchorStatus, ProofBundle } from '@/api/types/batch'

const props = defineProps<{
  modelValue: boolean
  batchCode?: string
  proofBundle?: ProofBundle | null
  anchorStatus?: AnchorStatus | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const formatTime = (ts?: number) => {
  if (!ts) return '—'
  return new Date(ts).toLocaleString('vi-VN')
}
</script>

<template>
  <BModal
    :model-value="modelValue"
    size="lg"
    title="Lịch sử Kích hoạt & Nhật ký Kiểm toán (Audit Logs)"
    ok-only
    ok-title="Đóng"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="d-flex flex-column gap-3 small">
      <div class="text-body-secondary">
        Lịch sử sự kiện kích hoạt và neo chuỗi của lô <strong>{{ batchCode || 'Chưa chọn lô' }}</strong>:
      </div>

      <div class="list-group list-group-flush border rounded-3">
        <!-- Sự kiện neo chuỗi Polygon từ API -->
        <div class="list-group-item p-3 d-flex flex-column gap-1 bg-body">
          <div class="d-flex align-items-center justify-content-between">
            <span class="fw-bold text-body font-monospace">POLYGON_ANCHOR</span>
            <span class="badge" :class="anchorStatus?.publicChain?.status === 'CONFIRMED' || anchorStatus?.publicChain?.status === 'SUCCESS' ? 'bg-success-subtle text-success' : 'bg-warning-subtle text-warning-emphasis'">
              {{ anchorStatus?.publicChain?.status || proofBundle?.anchor?.status || 'PENDING' }}
            </span>
          </div>
          <div class="text-body-secondary">
            TxHash: <span class="font-monospace text-primary text-break">{{ anchorStatus?.publicChain?.txHash || proofBundle?.anchor?.txHash || 'Chưa gửi giao dịch lên Polygon' }}</span>
          </div>
          <div class="d-flex align-items-center gap-3 text-body-tertiary" style="font-size: 11px;">
            <span>Khối: {{ anchorStatus?.publicChain?.blockNumber || proofBundle?.anchor?.blockNumber ? '#' + (anchorStatus?.publicChain?.blockNumber || proofBundle?.anchor?.blockNumber) : '—' }}</span>
            <span>Mạng: Polygon Amoy (Chain ID 80002)</span>
          </div>
        </div>

        <!-- Sự kiện neo chuỗi Besu QBFT từ API -->
        <div class="list-group-item p-3 d-flex flex-column gap-1 bg-body">
          <div class="d-flex align-items-center justify-content-between">
            <span class="fw-bold text-body font-monospace">BESU_LEDGER_ANCHOR</span>
            <span class="badge" :class="anchorStatus?.privateChain?.finalized ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary-emphasis'">
              {{ anchorStatus?.privateChain?.finalized ? 'FINALIZED' : 'PENDING' }}
            </span>
          </div>
          <div class="text-body-secondary">
            TxHash: <span class="font-monospace text-primary text-break">{{ anchorStatus?.privateChain?.txHash || 'Chưa gửi giao dịch lên Besu' }}</span>
          </div>
          <div class="d-flex align-items-center gap-3 text-body-tertiary" style="font-size: 11px;">
            <span>Sổ cái nội bộ: QBFT Consensus</span>
          </div>
        </div>

        <!-- Các sự kiện canh tác thực tế từ ProofBundle -->
        <template v-if="proofBundle?.events?.length">
          <div
            v-for="ev in proofBundle.events"
            :key="ev.seq"
            class="list-group-item p-3 d-flex flex-column gap-1 bg-body"
          >
            <div class="d-flex align-items-center justify-content-between">
              <span class="fw-bold text-body font-monospace">EVENT #{{ ev.seq }} · {{ ev.eventType }}</span>
              <span class="badge bg-success-subtle text-success">CONFIRMED</span>
            </div>
            <div class="text-body-secondary">{{ ev.actionName || ev.eventType }}</div>
            <div class="d-flex align-items-center gap-3 text-body-tertiary" style="font-size: 11px;">
              <span>Thời gian: {{ formatTime(ev.timestamp) }}</span>
              <span>Thực hiện: {{ ev.performedBy || 'Hệ thống' }}</span>
            </div>
          </div>
        </template>
      </div>
    </div>
  </BModal>
</template>
