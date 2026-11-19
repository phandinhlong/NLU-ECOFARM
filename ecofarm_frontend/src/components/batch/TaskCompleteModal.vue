<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { TASK_FIELDS, TASK_TYPE_LABELS, type BatchTask, type CompleteTaskInput } from '@/api/types/task'

const props = defineProps<{
  modelValue: boolean
  task: BatchTask | null
  isPending?: boolean
  error?: Error | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm', payload: CompleteTaskInput): void
}>()

const toLocalInput = (d: Date) => {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const clientEventId = ref('')
const occurredAt = ref('')
const data = ref<Record<string, unknown>>({})

const fields = computed(() => (props.task ? TASK_FIELDS[props.task.taskType] ?? [] : []))
const title = computed(() =>
  props.task ? `Hoàn thành: ${TASK_TYPE_LABELS[props.task.taskType] ?? props.task.taskType}` : 'Hoàn thành công việc',
)

watch(
  () => props.modelValue,
  (open) => {
    if (!open) return
    clientEventId.value = crypto.randomUUID()
    occurredAt.value = toLocalInput(new Date())
    data.value = {}
  },
)

const onConfirm = () => {
  const payload: Record<string, unknown> = {}
  for (const f of fields.value) {
    const value = data.value[f.key]
    if (value === '' || value === undefined || value === null) continue
    payload[f.key] = f.type === 'number' ? Number(value) : value
  }
  emit('confirm', {
    clientEventId: clientEventId.value,
    occurredAt: new Date(occurredAt.value).toISOString(),
    data: payload,
  })
}
</script>

<template>
  <BModal
    :model-value="modelValue"
    :title="title"
    ok-title="Hoàn thành"
    cancel-title="Hủy"
    :ok-loading="isPending"
    @ok.prevent="onConfirm"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="d-flex flex-column gap-3">
      <div v-if="task?.note" class="p-2 bg-body-secondary rounded-2 small">{{ task.note }}</div>
      <div>
        <label for="task-occurred-at" class="form-label small fw-semibold mb-1">Thời điểm</label>
        <input id="task-occurred-at" v-model="occurredAt" type="datetime-local" class="form-control" />
      </div>
      <div v-for="f in fields" :key="f.key">
        <label :for="`task-field-${f.key}`" class="form-label small fw-semibold mb-1">{{ f.label }}</label>
        <BFormSelect v-if="f.type === 'select'" :id="`task-field-${f.key}`" v-model="data[f.key]">
          <option value="">Chọn</option>
          <option v-for="opt in f.options" :key="opt" :value="opt">{{ opt }}</option>
        </BFormSelect>
        <input
          v-else
          :id="`task-field-${f.key}`"
          v-model="data[f.key]"
          :type="f.type"
          class="form-control"
          :step="f.type === 'number' ? 'any' : undefined"
        />
      </div>
      <div v-if="error" class="alert alert-danger p-2 small mb-0">{{ error.message }}</div>
    </div>
  </BModal>
</template>
