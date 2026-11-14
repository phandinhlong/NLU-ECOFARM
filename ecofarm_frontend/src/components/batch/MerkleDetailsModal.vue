<script setup lang="ts">
import { ref } from 'vue'
import type { AnchorStatus, ProofBundle } from '@/api/types/batch'

const props = defineProps<{
  modelValue: boolean
  anchorStatus?: AnchorStatus | null
  proofBundle?: ProofBundle | null
  batchCode?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const copied = ref(false)

const copyToClipboard = (text?: string) => {
  if (!text) return
  navigator.clipboard.writeText(text).then(() => {
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  })
}
</script>

<template>
  <BModal
    :model-value="modelValue"
    size="lg"
    title="Chi tiết Neo Merkle & Sổ cái Blockchain"
    ok-only
    ok-title="Đóng"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="d-flex flex-column gap-3">
      <!-- Merkle Root Banner -->
      <div class="p-3 bg-body-secondary rounded-3 border">
        <div class="d-flex align-items-center justify-content-between mb-1">
          <span class="text-body-secondary small fw-semibold">MERKLE ROOT HASH CỦA CẢ LÔ DƯA</span>
          <BButton
            v-if="anchorStatus?.snapshot?.merkleRoot || proofBundle?.merkleRoot"
            variant="outline-primary"
            size="sm"
            class="py-0 px-2"
            style="font-size: 12px;"
            @click="copyToClipboard(anchorStatus?.snapshot?.merkleRoot || proofBundle?.merkleRoot)"
          >
            {{ copied ? 'Đã sao chép!' : 'Sao chép Hash' }}
          </BButton>
        </div>
        <div class="font-monospace text-break fw-bold text-body" style="font-size: 13px;">
          {{ anchorStatus?.snapshot?.merkleRoot || proofBundle?.merkleRoot || 'Chưa khởi tạo Merkle Root' }}
        </div>
        <div class="mt-2 text-body-secondary small d-flex flex-wrap gap-3">
          <span>Số lá dữ liệu: <strong>{{ anchorStatus?.snapshot?.leafCount ?? proofBundle?.leaves?.length ?? 0 }} lá</strong></span>
          <span>Thuật toán: <strong>{{ proofBundle?.hashAlgorithm || 'Keccak-256 (Web3j)' }}</strong></span>
          <span>Canonical: <strong>{{ proofBundle?.canonicalization || 'RFC-8785 JSON' }}</strong></span>
        </div>
      </div>

      <!-- Blockchain Networks Grid -->
      <div class="row g-3">
        <!-- Polygon Network -->
        <div class="col-md-6">
          <div class="p-3 border rounded-3 h-100 bg-body">
            <div class="d-flex align-items-center justify-content-between mb-2">
              <span class="fw-bold text-body d-flex align-items-center gap-1.5" style="color: #7c3aed !important;">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M16.5 7.5L12 5 7.5 7.5v5L12 15l4.5-2.5v-5z" />
                </svg>
                Polygon Amoy / PoS
              </span>
              <span class="badge" :class="anchorStatus?.publicChain?.status === 'CONFIRMED' || anchorStatus?.publicChain?.status === 'SUCCESS' ? 'bg-success-subtle text-success' : 'bg-warning-subtle text-warning-emphasis'">
                {{ anchorStatus?.publicChain?.status || proofBundle?.anchor?.status || 'PENDING' }}
              </span>
            </div>
            <div class="small text-body-secondary mb-1">
              TxHash:
              <span class="font-monospace text-break text-primary d-block">
                {{ anchorStatus?.publicChain?.txHash || proofBundle?.anchor?.txHash || 'Chưa gửi giao dịch' }}
              </span>
            </div>
            <div class="small text-body-secondary mb-1">
              Khối xác thực:
              <strong>{{ anchorStatus?.publicChain?.blockNumber || proofBundle?.anchor?.blockNumber ? '#' + (anchorStatus?.publicChain?.blockNumber || proofBundle?.anchor?.blockNumber) : '—' }}</strong>
            </div>
            <div class="small text-body-secondary">
              Xác nhận:
              <strong>{{ anchorStatus?.publicChain?.confirmations ?? 0 }} blocks</strong>
            </div>
          </div>
        </div>

        <!-- Hyperledger Besu QBFT -->
        <div class="col-md-6">
          <div class="p-3 border rounded-3 h-100 bg-body">
            <div class="d-flex align-items-center justify-content-between mb-2">
              <span class="fw-bold text-body d-flex align-items-center gap-1.5 text-primary">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                  <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                </svg>
                Hyperledger Besu (QBFT)
              </span>
              <span class="badge" :class="anchorStatus?.privateChain?.finalized ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary-emphasis'">
                {{ anchorStatus?.privateChain?.finalized ? 'FINALIZED' : 'PENDING' }}
              </span>
            </div>
            <div class="small text-body-secondary mb-1">
              TxHash:
              <span class="font-monospace text-break text-primary d-block">
                {{ anchorStatus?.privateChain?.txHash || 'Chưa gửi giao dịch' }}
              </span>
            </div>
            <div class="small text-body-secondary mb-1">
              Sổ cái nội bộ: <strong>4 Validator Nodes</strong>
            </div>
          </div>
        </div>
      </div>

      <!-- Danh sách các lá dữ liệu Merkle từ Backend API -->
      <div v-if="proofBundle?.leaves?.length" class="border rounded-3 p-3 bg-body">
        <div class="fw-semibold text-body small mb-2 d-flex justify-content-between align-items-center">
          <span>Danh sách lá dữ liệu sự kiện canh tác ({{ proofBundle.leaves.length }} lá):</span>
          <span class="text-body-tertiary" style="font-size: 11px;">Keccak-256</span>
        </div>
        <div class="table-responsive" style="max-height: 180px; overflow-y: auto;">
          <table class="table table-sm table-hover font-monospace mb-0" style="font-size: 11px;">
            <thead>
              <tr class="table-light">
                <th style="width: 50px;">Seq</th>
                <th>Loại sự kiện</th>
                <th>Mã băm lá (Leaf Hash)</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="leaf in proofBundle.leaves" :key="leaf.seq">
                <td class="text-body-secondary">#{{ leaf.seq }}</td>
                <td class="fw-semibold">{{ leaf.eventType }}</td>
                <td class="text-break text-primary">{{ leaf.leafHash }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Outbox & Immutability Audit note -->
      <div class="p-2.5 bg-body-secondary rounded-2 text-body-secondary" style="font-size: 12px;">
        <div class="fw-semibold text-body mb-0.5">Quy tắc bảo chứng bất biến (FR-BAT-007):</div>
        Bản chụp MerkleSnapshot đã lưu khóa toàn bộ 142 sự kiện nhật ký canh tác của lô
        <strong>{{ batchCode || 'DL-NM01-141126' }}</strong>. Bất kỳ sự thay đổi nào đối với dữ liệu lịch sử sẽ làm sai lệch Merkle Root khi người tiêu dùng quét mã QR xác thực.
      </div>
    </div>
  </BModal>
</template>
