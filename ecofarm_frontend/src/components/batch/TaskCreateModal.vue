<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { TASK_TYPE_LABELS, type TaskType } from '@/api/types/task'

const props = defineProps<{
  modelValue: boolean
  workers: { id: number; fullName: string }[]
  isPending?: boolean
  error?: Error | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm', payload: { workerId: number; taskType: TaskType; note?: string }): void
}>()

const workerId = ref<number | null>(null)
const taskType = ref<TaskType | ''>('')
const note = ref('')

const typeOptions = Object.entries(TASK_TYPE_LABELS) as [TaskType, string][]
const canConfirm = computed(() => workerId.value !== null && taskType.value !== '')

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      workerId.value = null
      taskType.value = ''
      note.value = ''
    }
  },
)

const onConfirm = () => {
  if (workerId.value === null || taskType.value === '') return
  emit('confirm', {
    workerId: workerId.value,
    taskType: taskType.value,
    note: note.value.trim() || undefined,
  })
}
</script>

<template>
  <BModal
    :model-value="modelValue"
    title="Giao việc"
    ok-title="Giao việc"
    cancel-title="Hủy"
    :ok-loading="isPending"
    :ok-disabled="!canConfirm"
    @ok.prevent="onConfirm"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="d-flex flex-column gap-3">
      <div>
        <label for="task-worker" class="form-label small fw-semibold mb-1">Công nhân</label>
        <select id="task-worker" v-model="workerId" class="form-select">
          <option :value="null" disabled>Chọn công nhân</option>
          <option v-for="w in workers" :key="w.id" :value="w.id">{{ w.fullName }}</option>
        </select>
      </div>
      <div>
        <label for="task-type" class="form-label small fw-semibold mb-1">Loại công việc</label>
        <select id="task-type" v-model="taskType" class="form-select">
          <option value="" disabled>Chọn loại công việc</option>
          <option v-for="[value, label] in typeOptions" :key="value" :value="value">{{ label }}</option>
        </select>
      </div>
      <div>
        <label for="task-note" class="form-label small fw-semibold mb-1">Ghi chú</label>
        <textarea id="task-note" v-model="note" class="form-control" rows="2" />
      </div>
      <div v-if="error" class="alert alert-danger p-2 small mb-0">{{ error.message }}</div>
    </div>
  </BModal>
</template>
