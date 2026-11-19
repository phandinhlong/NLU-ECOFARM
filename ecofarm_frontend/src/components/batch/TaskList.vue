<script setup lang="ts">
import { computed, ref } from 'vue'
import SoftBadge from '@/components/common/SoftBadge.vue'
import TaskCreateModal from '@/components/batch/TaskCreateModal.vue'
import TaskCompleteModal from '@/components/batch/TaskCompleteModal.vue'
import {
  useCancelTaskMutation,
  useCompleteTaskMutation,
  useCreateTaskMutation,
  useTasksQuery,
} from '@/composables/useTasks'
import {
  TASK_STATUS_LABELS,
  TASK_STATUS_TONES,
  TASK_TYPE_LABELS,
  type BatchTask,
  type CompleteTaskInput,
  type CreateTaskInput,
} from '@/api/types/task'

const props = defineProps<{
  batchId: number
  isEngineer: boolean
  isFarmer: boolean
  locked: boolean
  workers: { id: number; fullName: string }[]
}>()

const batchIdRef = computed(() => props.batchId)
const { data, isLoading, isError, error } = useTasksQuery(batchIdRef)
const createMutation = useCreateTaskMutation(batchIdRef)
const completeMutation = useCompleteTaskMutation(batchIdRef)
const cancelMutation = useCancelTaskMutation(batchIdRef)

const tasks = computed(() => data.value?.content ?? [])

const createPending = computed(() => createMutation.isPending.value)
const createError = computed(() => createMutation.error.value)
const completePending = computed(() => completeMutation.isPending.value)
const completeError = computed(() => completeMutation.error.value)
const cancelPending = computed(() => cancelMutation.isPending.value)
const cancelError = computed(() => cancelMutation.error.value)

const showCreate = ref(false)
const showComplete = ref(false)
const activeTask = ref<BatchTask | null>(null)

const typeLabel = (type: string) => TASK_TYPE_LABELS[type] ?? type
const formatTime = (value: number | null) => (value ? new Date(value).toLocaleString('vi-VN') : '—')

const openCreate = () => {
  showCreate.value = true
}

const onCreate = (payload: CreateTaskInput) => {
  createMutation.mutate(payload, { onSuccess: () => (showCreate.value = false) })
}

const openComplete = (task: BatchTask) => {
  activeTask.value = task
  showComplete.value = true
}

const onComplete = (input: CompleteTaskInput) => {
  if (!activeTask.value) return
  completeMutation.mutate(
    { taskId: activeTask.value.id, input },
    { onSuccess: () => (showComplete.value = false) },
  )
}

const cancelTask = (task: BatchTask) => {
  const reason = window.prompt('Lý do hủy công việc:')?.trim()
  if (!reason) return
  cancelMutation.mutate({ taskId: task.id, reason })
}
</script>

<template>
  <div>
    <div class="d-flex flex-wrap gap-2 justify-content-between align-items-center mb-2">
      <span class="small text-body-secondary">Danh sách công việc</span>
      <BButton v-if="isEngineer && !locked" size="sm" variant="outline-primary" @click="openCreate">
        <FontAwesomeIcon icon="plus" aria-hidden="true" /> Giao việc
      </BButton>
    </div>

    <div v-if="isLoading" class="d-flex justify-content-center py-4">
      <BSpinner small />
    </div>

    <BAlert v-else-if="isError" variant="danger" class="small mb-0">
      Không tải được công việc: {{ error?.message }}
    </BAlert>

    <p v-else-if="tasks.length === 0" class="small text-body-secondary mb-0">Chưa có công việc nào</p>

    <div v-else class="d-flex flex-column gap-2">
      <div
        v-for="task in tasks"
        :key="task.id"
        class="border rounded-3 p-2 p-md-3 d-flex flex-wrap gap-2 align-items-center justify-content-between"
      >
        <div class="d-flex flex-column gap-1">
          <div class="d-flex flex-wrap gap-2 align-items-center">
            <span class="fw-semibold">{{ typeLabel(task.taskType) }}</span>
            <SoftBadge :label="TASK_STATUS_LABELS[task.status]" :variant="TASK_STATUS_TONES[task.status]" />
          </div>
          <span class="small text-body-secondary">
            {{ task.workerName ?? 'Chưa phân công' }} · {{ formatTime(task.assignedAt) }}
          </span>
        </div>
        <div v-if="task.status === 'ASSIGNED'" class="d-flex gap-2">
          <BButton v-if="isFarmer" size="sm" variant="success" @click="openComplete(task)">
            Hoàn thành
          </BButton>
          <BButton
            v-if="isEngineer && !locked"
            size="sm"
            variant="outline-danger"
            :disabled="cancelPending"
            @click="cancelTask(task)"
          >
            Hủy
          </BButton>
        </div>
      </div>
    </div>

    <BAlert v-if="createError" variant="danger" class="small mt-2 mb-0">
      Giao việc thất bại: {{ createError?.message }}
    </BAlert>
    <BAlert v-if="completeError" variant="danger" class="small mt-2 mb-0">
      Hoàn thành thất bại: {{ completeError?.message }}
    </BAlert>
    <BAlert v-if="cancelError" variant="danger" class="small mt-2 mb-0">
      Hủy thất bại: {{ cancelError?.message }}
    </BAlert>

    <TaskCreateModal
      v-model="showCreate"
      :workers="workers"
      :is-pending="createPending"
      :error="createError"
      @confirm="onCreate"
    />
    <TaskCompleteModal
      v-model="showComplete"
      :task="activeTask"
      :is-pending="completePending"
      :error="completeError"
      @confirm="onComplete"
    />
  </div>
</template>
