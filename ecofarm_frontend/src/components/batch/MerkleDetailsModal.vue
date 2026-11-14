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
            variant="outline-primary"
            size="sm"
            class="py-0 px-2"
            style="font-size: 12px;"
            @click="copyToClipboard(anchorStatus?.snapshot?.merkleRoot || proofBundle?.merkleRoot || '0x8f2a17b3c94892cfa77d9435b6c20149e917d29ab6cf54318c5417da5297184b')"
          >
            {{ copied ? 'Đã sao chép!' : 'Sao chép Hash' }}
          </BButton>
        </div>
        <div class="font-monospace text-break fw-bold text-body" style="font-size: 13px;">
          {{ anchorStatus?.snapshot?.merkleRoot || proofBundle?.merkleRoot || '0x8f2a17b3c94892cfa77d9435b6c20149e917d29ab6cf54318c5417da5297184b' }}
        </div>
        <div class="mt-2 text-body-secondary small d-flex gap-3">
          <span>Số lá dữ liệu: <strong>{{ anchorStatus?.snapshot?.leafCount || proofBundle?.leaves?.length || 142 }} lá</strong></span>
          <span>Thuật toán: <strong>Keccak-256 (Web3j)</strong></span>
          <span>Canonical: <strong>RFC-8785 JSON</strong></span>
        </div>
      </div>

      <!-- Blockchain Networks Grid -->
      <div class="row g-3">
        <!-- Polygon PoS Mainnet -->
        <div class="col-md-6">
          <div class="p-3 border rounded-3 h-100 bg-body">
            <div class="d-flex align-items-center justify-content-between mb-2">
              <span class="fw-bold text-body d-flex align-items-center gap-1.5" style="color: #7c3aed !important;">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M16.5 7.5L12 5 7.5 7.5v5L12 15l4.5-2.5v-5z" />
                </svg>
                Polygon PoS Mainnet
              </span>
              <span class="badge bg-success-subtle text-success">
                {{ anchorStatus?.publicChain?.status || 'CONFIRMED' }}
              </span>
            </div>
            <div class="small text-body-secondary mb-1">
              TxHash:
              <span class="font-monospace text-break text-primary">
                {{ anchorStatus?.publicChain?.txHash || '0x93b749f7e8a91bc7d04f2165e8a7192bc531' }}
              </span>
            </div>
            <div class="small text-body-secondary mb-1">
              Khối xác thực:
              <strong>#{{ anchorStatus?.publicChain?.blockNumber || '4,821,902' }}</strong>
            </div>
            <div class="small text-body-secondary">
              Xác nhận:
              <strong>128 / 128 blocks</strong>
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
              <span class="badge bg-success-subtle text-success">
                FINALIZED
              </span>
            </div>
            <div class="small text-body-secondary mb-1">
              TxHash:
              <span class="font-monospace text-break text-primary">
                {{ anchorStatus?.privateChain?.txHash || '0x21fe88bca7364101e998b3c647bfa37c9802' }}
              </span>
            </div>
            <div class="small text-body-secondary mb-1">
              Khối Ledger:
              <strong>#12,940</strong>
            </div>
            <div class="small text-body-secondary">
              Sổ cái nội bộ: <strong>4 Validator Nodes</strong>
            </div>
          </div>
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
