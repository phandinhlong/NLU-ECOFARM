<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  modelValue: boolean
  batchId: number
  batchCode: string
  defaultUnitCount?: number
  isPending?: boolean
  error?: Error | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm', payload: { unitCount: number }): void
}>()

const unitCount = ref(props.defaultUnitCount || 2850)

const onConfirm = () => {
  emit('confirm', { unitCount: unitCount.value })
}
</script>

<template>
  <BModal
    :model-value="modelValue"
    title="Xác nhận Khóa dữ liệu lô & Neo Blockchain"
    ok-title="Xác nhận & Khóa dữ liệu"
    cancel-title="Hủy"
    :ok-loading="isPending"
    @ok.prevent="onConfirm"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="d-flex flex-column gap-3">
      <!-- Warning alert box matching screenshot -->
      <div class="p-3 bg-danger-subtle border border-danger-subtle rounded-3 text-danger">
        <div class="d-flex align-items-center gap-2 mb-1.5 fw-bold">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
            <line x1="12" y1="9" x2="12" y2="13"></line>
            <line x1="12" y1="17" x2="12.01" y2="17"></line>
          </svg>
          Cảnh báo bất biến dữ liệu (Quy định FR-BAT-007):
        </div>
        <p class="mb-0 small" style="line-height: 1.45;">
          Khi xác nhận, trạng thái lô chuyển sang <strong>ANCHORING/ACTIVE</strong>. Hệ thống sẽ chặn vĩnh viễn mọi thao tác thêm, chỉnh sửa hoặc tạo đính chính sự kiện nhật ký canh tác của lô dưa này.
        </p>
      </div>

      <!-- Batch Details Summary -->
      <div class="p-3 bg-body-secondary rounded-3 border">
        <div class="d-flex justify-content-between mb-2 pb-2 border-bottom">
          <span class="text-body-secondary">Mã định danh lô:</span>
          <span class="fw-bold text-body">{{ batchCode }}</span>
        </div>
        <div class="d-flex justify-content-between mb-2 pb-2 border-bottom">
          <span class="text-body-secondary">Mã QR truy xuất:</span>
          <span class="fw-bold text-primary">Dùng chung cho cả lô dưa {{ batchCode }}</span>
        </div>
        <div class="d-flex justify-content-between mb-2 pb-2 border-bottom">
          <span class="text-body-secondary">Kỹ sư xác thực chữ ký:</span>
          <span class="fw-bold text-primary">KS. Nguyễn Văn An (Besu Ed25519)</span>
        </div>
        <div class="d-flex justify-content-between align-items-center">
          <label for="unitCountInput" class="text-body-secondary mb-0">Quy mô xuất kho lô dưa:</label>
          <div style="width: 130px;">
            <input
              id="unitCountInput"
              v-model.number="unitCount"
              type="number"
              min="1"
              max="100000"
              class="form-control form-control-sm text-end fw-bold"
            />
          </div>
        </div>
      </div>

      <!-- Error alert if any -->
      <div v-if="error" class="alert alert-danger p-2 small mb-0">
        {{ error.message }}
      </div>
    </div>
  </BModal>
</template>
