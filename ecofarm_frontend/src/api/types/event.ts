export type EventType =
  | 'SOWING'
  | 'POLLINATION'
  | 'FERTILIZE_IRRIGATE'
  | 'POLLINATION_ISOLATION'
  | 'BRIX_MEASUREMENT'
  | 'HARVEST'
  | 'NOTE'
  | 'PRUNE_SHOOTS'
  | 'IRRIGATE'
  | 'TASK_ASSIGNED'
  | 'EVENT_APPROVED'
  | 'EVENT_REJECTED'

export type EventReviewStatus = 'UNREVIEWED' | 'APPROVED' | 'REJECTED'

export type EventChainStatus = 'PENDING' | 'CONFIRMED' | 'SUPERSEDED'

export interface CultivationEvent {
  id: number
  batchId: number
  seq: number | null
  eventType: EventType
  status: EventChainStatus | null
  reviewStatus: EventReviewStatus
  data: Record<string, unknown> | null
  eventHash: string | null
  recordedById: number | null
  recordedByName: string | null
  occurredAt: string | null
  recordedAt: string | null
  clientEventId: string | null
  amendsEventId: number | null
  batchCode?: string | null
  greenhouseId?: number | null
  greenhouseName?: string | null
  pendingApproverId?: number | null
  assignerId?: number | null
  assignerActive?: boolean | null
  rejectReason?: string | null
  amendReason?: string | null
  amendNote?: string | null
}

export type AmendReason = 'WRONG_QUANTITY' | 'WRONG_BED' | 'WRONG_TIME' | 'OTHER'

export const AMEND_REASON_LABELS: Record<AmendReason, string> = {
  WRONG_QUANTITY: 'Sai số lượng',
  WRONG_BED: 'Sai luống',
  WRONG_TIME: 'Sai thời điểm',
  OTHER: 'Khác',
}

export interface RejectEventInput {
  reason: string
}

export interface ReassignApproverInput {
  engineerId: number
}

export interface AmendEventInput {
  clientEventId: string
  occurredAt: string
  data: Record<string, unknown>
  reasonCode: AmendReason
  note?: string
}

export interface CreateEventInput {
  clientEventId: string
  eventType: EventType
  schemaVersion: number
  occurredAt: string
  data: Record<string, unknown>
}

type Tone = 'success' | 'warning' | 'danger' | 'info' | 'muted'

export const LOG_EVENT_TYPES = [
  'POLLINATION',
  'IRRIGATE',
  'FERTILIZE_IRRIGATE',
  'PRUNE_SHOOTS',
  'POLLINATION_ISOLATION',
  'NOTE',
] as const

export const EVENT_TYPE_LABELS: Record<EventType, string> = {
  POLLINATION: 'Thụ phấn',
  IRRIGATE: 'Tưới nước',
  FERTILIZE_IRRIGATE: 'Bón phân',
  PRUNE_SHOOTS: 'Tỉa chồi',
  POLLINATION_ISOLATION: 'Cách li',
  NOTE: 'Ghi chú',
  SOWING: 'Xuống giống',
  BRIX_MEASUREMENT: 'Đo Brix',
  HARVEST: 'Thu hoạch',
  TASK_ASSIGNED: 'Giao việc',
  EVENT_APPROVED: 'Duyệt',
  EVENT_REJECTED: 'Từ chối',
}

export const EVENT_TYPE_ICONS: Record<string, string> = {
  POLLINATION: 'seedling',
  IRRIGATE: 'droplet',
  FERTILIZE_IRRIGATE: 'leaf',
  PRUNE_SHOOTS: 'scissors',
  POLLINATION_ISOLATION: 'ban',
  NOTE: 'pen-to-square',
}

export const REVIEW_STATUS_LABELS: Record<EventReviewStatus, string> = {
  UNREVIEWED: 'Chờ duyệt',
  APPROVED: 'Đã duyệt',
  REJECTED: 'Từ chối',
}

export const REVIEW_STATUS_TONES: Record<EventReviewStatus, Tone> = {
  UNREVIEWED: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
}

export const CHAIN_STATUS_LABELS: Record<EventChainStatus, string> = {
  PENDING: 'Chờ ghi chuỗi',
  CONFIRMED: 'Đã ghi chuỗi',
  SUPERSEDED: 'Đã thay thế',
}

export const CHAIN_STATUS_TONES: Record<EventChainStatus, Tone> = {
  PENDING: 'muted',
  CONFIRMED: 'success',
  SUPERSEDED: 'muted',
}

export const EVENT_FIELDS: Record<
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
    { key: 'amountKg', label: 'Khối lượng', type: 'number' },
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

export const formatEventTime = (value: string | number | null): string =>
  value ? new Date(value).toLocaleString('vi-VN') : '—'
