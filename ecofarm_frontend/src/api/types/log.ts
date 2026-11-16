// Cultivation journal (LOG) request/response types, mirroring the backend contract.

export type EventTypeCode =
  | 'XUONG_GIONG'
  | 'THU_PHAN'
  | 'BON_PHAN'
  | 'TUOI_NUOC'
  | 'PHUN_VI_LUONG'
  | 'NGUNG_PHAN'
  | 'DO_BRIX'
  | 'THU_HOACH'
  | 'GHI_CHU'
  | 'EVENT_APPROVED'

export type EventStatus = 'PENDING' | 'CONFIRMED' | 'APPROVED' | 'REJECTED' | 'SUPERSEDED'

export interface AttachmentView {
  id: number
  fileKey: string
  sha256: string
  mimeType: string
}

export interface EventView {
  id: number
  batchId: number
  seq: number
  eventType: EventTypeCode
  payload: Record<string, unknown> | null
  eventHash: string
  status: EventStatus
  recordedBy: number | null
  recordedAt: number | null
  occurredAt: number | null
  deviceClockSkewMs: number | null
  needsReview: boolean | null
  clientEventId: string | null
  amendsEventId: number | null
  attachments: AttachmentView[]
}

export interface EventPage {
  content: EventView[]
  page: number
  size: number
  totalElements: number
}

export interface EventListParams {
  page?: number
  size?: number
  status?: EventStatus
  eventType?: EventTypeCode
  includeSuperseded?: boolean
}

export interface CreateEventRequest {
  eventType: EventTypeCode
  occurredAt: number
  clientEventId: string
  payload: Record<string, unknown>
  attachmentFileKeys?: string[]
  // Device wall-clock at submit time (epoch ms); lets the server flag clock skew.
  deviceSentAt?: number
}

export interface AmendRequest {
  clientEventId: string
  occurredAt: number
  payload: Record<string, unknown>
  reason: string
  attachmentFileKeys?: string[]
}

export interface RejectRequest {
  reason: string
}

// Event types a worker/engineer may create from the UI (harvest and approval are excluded).
export const RECORDABLE_EVENT_TYPES: EventTypeCode[] = [
  'XUONG_GIONG',
  'THU_PHAN',
  'BON_PHAN',
  'TUOI_NUOC',
  'PHUN_VI_LUONG',
  'NGUNG_PHAN',
  'DO_BRIX',
  'GHI_CHU',
]
