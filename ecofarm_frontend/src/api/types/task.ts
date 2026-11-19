export type TaskStatus = 'ASSIGNED' | 'COMPLETED' | 'CANCELLED'

export type TaskType =
  | 'POLLINATION'
  | 'IRRIGATE'
  | 'FERTILIZE_IRRIGATE'
  | 'PRUNE_SHOOTS'
  | 'POLLINATION_ISOLATION'
  | 'NOTE'

export interface BatchTask {
  id: number
  batchId: number
  workerId: number | null
  workerName: string | null
  taskType: string
  note: string | null
  assignedById: number | null
  assignedAt: number | null
  completedAt: number | null
  status: TaskStatus
  eventId: number | null
  completionEventId: number | null
}

export interface CreateTaskInput {
  workerId: number
  taskType: TaskType
  note?: string
}

export interface CompleteTaskInput {
  clientEventId: string
  occurredAt: string
  data: Record<string, unknown>
}

export const TASK_TYPE_LABELS: Record<string, string> = {
  POLLINATION: 'Thụ phấn',
  IRRIGATE: 'Tưới nước',
  FERTILIZE_IRRIGATE: 'Bón phân',
  PRUNE_SHOOTS: 'Tỉa chồi',
  POLLINATION_ISOLATION: 'Cách li',
  NOTE: 'Ghi chú',
}

export const TASK_STATUS_LABELS: Record<TaskStatus, string> = {
  ASSIGNED: 'Đang thực hiện',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
}

export const TASK_STATUS_TONES: Record<TaskStatus, 'success' | 'warning' | 'danger' | 'info' | 'muted'> = {
  ASSIGNED: 'muted',
  COMPLETED: 'muted',
  CANCELLED: 'muted',
}

export const TASK_FIELDS: Record<
  string,
  { key: string; label: string; type: 'number' | 'text' | 'datetime-local' | 'select'; options?: string[] }[]
> = {
  POLLINATION: [
    { key: 'pollinationDate', label: 'Ngày thụ phấn', type: 'datetime-local' },
    { key: 'method', label: 'Phương pháp', type: 'select', options: ['manual', 'bee'] },
    { key: 'flowerCount', label: 'Số hoa', type: 'number' },
  ],
  IRRIGATE: [
    { key: 'waterLiters', label: 'Lượng nước (lít)', type: 'number' },
    { key: 'appliedAt', label: 'Thời điểm', type: 'datetime-local' },
  ],
  FERTILIZE_IRRIGATE: [
    { key: 'fertilizerName', label: 'Loại phân', type: 'text' },
    { key: 'amountKg', label: 'Khối lượng (kg)', type: 'number' },
    { key: 'unit', label: 'Đơn vị', type: 'select', options: ['kg', 'l'] },
    { key: 'appliedAt', label: 'Thời điểm', type: 'datetime-local' },
  ],
  PRUNE_SHOOTS: [
    { key: 'shootsRemoved', label: 'Số chồi đã tỉa', type: 'number' },
    { key: 'note', label: 'Ghi chú', type: 'text' },
  ],
  POLLINATION_ISOLATION: [
    { key: 'stoppedAt', label: 'Thời điểm ngừng', type: 'datetime-local' },
    { key: 'isolationDays', label: 'Số ngày cách li', type: 'number' },
    { key: 'reason', label: 'Lý do', type: 'text' },
  ],
  NOTE: [{ key: 'text', label: 'Nội dung ghi chú', type: 'text' }],
}
