import type { PageResponse } from '@/api/types/catalog'

export type BatchStatus =
  | 'DRAFT'
  | 'GROWING'
  | 'HARVESTED'
  | 'QC_PASSED'
  | 'REJECTED'
  | 'ANCHORING'
  | 'ANCHOR_FAILED'
  | 'ACTIVE'
  | 'SHIPPED'
  | 'RECALLED'

export interface Batch {
  id: number
  batchCode: string
  greenhouseId: number
  varietyId: number
  status: BatchStatus
  sowDate: string
  harvestDate: string | null
  pollinationDate: string | null
  expectedHarvestDate: string | null
  createdBy: number
  activatedAt: number | null
  version: number
  createdAt: number
  updatedAt: number
  greenhouseCode: string
  varietyName: string
  workerCount: number
}

export interface BatchGreenhouse {
  id: number
  code: string
  name: string
}

export interface BatchVariety {
  id: number
  code: string
  name: string
  daysPollinationToHarvest: number | null
}

export interface BatchDetail extends Batch {
  greenhouse: BatchGreenhouse
  variety: BatchVariety
  assignedUserIds: number[]
  assignedWorkerNames: string[]
  eventCount: number
  pendingApprovalCount: number
}

export interface BatchCreateInput {
  greenhouseId: number
  varietyId: number
  sowDate: string
}

export interface BatchListParams {
  page: number
  size: number
  status?: BatchStatus
  greenhouseId?: number
  q?: string
  mine?: boolean
}

export interface UserSummary {
  id: number
  fullName: string
  roles: string[]
  status: 'ACTIVE' | 'LOCKED'
}

export interface ProblemError {
  field: string
  message: string
}

export interface Problem {
  status: number
  code: string
  detail: string
  errors?: ProblemError[]
}

export type BatchPage = PageResponse<Batch>

type Tone = 'success' | 'warning' | 'danger' | 'info' | 'muted'

export const STATUS_META: Record<BatchStatus, { label: string; tone: Tone; locked: boolean }> = {
  DRAFT: { label: 'Nháp', tone: 'muted', locked: false },
  GROWING: { label: 'Đang canh tác', tone: 'info', locked: false },
  HARVESTED: { label: 'Đã thu hoạch', tone: 'info', locked: false },
  QC_PASSED: { label: 'Đạt kiểm định', tone: 'success', locked: false },
  REJECTED: { label: 'Không đạt', tone: 'danger', locked: false },
  ANCHORING: { label: 'Đang neo blockchain', tone: 'warning', locked: true },
  ANCHOR_FAILED: { label: 'Neo lỗi', tone: 'danger', locked: true },
  ACTIVE: { label: 'Đã kích hoạt', tone: 'success', locked: true },
  SHIPPED: { label: 'Đã xuất kho', tone: 'success', locked: true },
  RECALLED: { label: 'Đã thu hồi', tone: 'danger', locked: true },
}

export const ALLOWED_NEXT: Record<BatchStatus, BatchStatus[]> = {
  DRAFT: ['GROWING'],
  GROWING: ['HARVESTED'],
  HARVESTED: ['QC_PASSED', 'REJECTED'],
  QC_PASSED: ['ANCHORING'],
  REJECTED: [],
  ANCHORING: ['ACTIVE', 'ANCHOR_FAILED'],
  ANCHOR_FAILED: ['ANCHORING'],
  ACTIVE: ['SHIPPED', 'RECALLED'],
  SHIPPED: ['RECALLED'],
  RECALLED: [],
}

export const isLocked = (status: BatchStatus): boolean => STATUS_META[status].locked

export const formatDate = (iso: string | null): string => {
  if (!iso) return '—'
  const [year, month, day] = iso.slice(0, 10).split('-')
  if (!year || !month || !day) return '—'
  return `${day}/${month}/${year}`
}

const ddMMyy = (iso: string): string => {
  const [year, month, day] = iso.slice(0, 10).split('-')
  return `${day}${month}${year?.slice(2) ?? ''}`
}

export const previewBatchCode = (
  greenhouseCode: string,
  sowDate: string,
  existingCodes: string[],
): string => {
  const base = `DL-${greenhouseCode.replace(/-/g, '')}-${ddMMyy(sowDate)}`
  const used = existingCodes.filter((code) => code === base || code.startsWith(`${base}-`)).length
  return used === 0 ? base : `${base}-${used + 1}`
}

export const expectedHarvest = (
  pollinationDate: string | null,
  days: number | null | undefined,
): string | null => {
  if (!pollinationDate || days == null) return null
  const date = new Date(`${pollinationDate.slice(0, 10)}T00:00:00Z`)
  date.setUTCDate(date.getUTCDate() + days)
  return date.toISOString().slice(0, 10)
}
