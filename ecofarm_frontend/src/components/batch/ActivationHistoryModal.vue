<script setup lang="ts">
defineProps<{
  modelValue: boolean
  batchCode?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const historyLogs = [
  {
    action: 'BATCH_ACTIVATE',
    time: '14/11/2026 10:45:12',
    actor: 'KS. Nguyễn Văn An',
    note: 'Kích hoạt lô dưa, chốt 2.850 quả và tạo Merkle Snapshot (142 lá)',
    status: 'COMPLETED',
  },
  {
    action: 'OUTBOX_FINALIZED',
    time: '14/11/2026 10:45:15',
    actor: 'Polygon Outbox Worker',
    note: 'Gửi giao dịch lên Polygon Mainnet Tx: 0x93b749... thành công',
    status: 'CONFIRMED',
  },
  {
    action: 'BATCH_QR_GENERATED',
    time: '14/11/2026 10:45:18',
    actor: 'Hệ thống Đóng gói',
    note: 'Sinh mã QR đại diện cho cả lô dưa DL-NM01-141126 để in 2.850 tem quả',
    status: 'READY',
  },
]
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
        Lịch sử sự kiện kích hoạt và neo chuỗi của lô <strong>{{ batchCode || 'DL-NM01-141126' }}</strong>:
      </div>

      <div class="list-group list-group-flush border rounded-3">
        <div
          v-for="(log, idx) in historyLogs"
          :key="idx"
          class="list-group-item p-3 d-flex flex-column gap-1 bg-body"
        >
          <div class="d-flex align-items-center justify-content-between">
            <span class="fw-bold text-body font-monospace">{{ log.action }}</span>
            <span class="badge bg-success-subtle text-success">{{ log.status }}</span>
          </div>
          <div class="text-body-secondary">{{ log.note }}</div>
          <div class="d-flex align-items-center gap-3 text-body-tertiary" style="font-size: 11px;">
            <span>Thời gian: {{ log.time }}</span>
            <span>Thực hiện: {{ log.actor }}</span>
          </div>
        </div>
      </div>
    </div>
  </BModal>
</template>
