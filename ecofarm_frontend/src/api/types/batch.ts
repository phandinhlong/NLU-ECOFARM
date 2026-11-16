import type { ApiResponse } from './common'
// MOCK-ONLY import: delete with the mock block below
import type { PageResponse } from '@/api/types/catalog'

// ============================================================================
// REAL (master)
// ============================================================================

export interface ActivateBatchRequest {
  unitCount: number
  version?: number
}

export interface PackageResponse {
  id: number
  batchId: number
  packageCode: string
  unitCount: number
  packedBy: number | null
  packedAt: number
}

export interface ActivateResponse {
  batchId: number
  status: string
  package?: PackageResponse
  snapshotId: number
  merkleRoot: string
  leafCount: number
  statusUrl: string
}

export interface ExtraQrRequest {
  extraCount: number
  reason: string
}

export interface ExtraQrResponse {
  batchId: number
  packageId: number
  previousUnitCount: number
  addedCount: number
  totalUnitCount: number
  fromSerial: number
  toSerial: number
  reason: string
  codes: string[]
}

export interface SnapshotInfo {
  id: number
  merkleRoot: string
  leafCount: number
}

export interface PrivateChainStatus {
  finalized: boolean
  txHash: string | null
}

export interface PublicChainStatus {
  id: number | null
  snapshotId: number | null
  chainId: number | null
  contractAddress: string | null
  txHash: string | null
  blockNumber: number | null
  status: string | null
  confirmations: number | null
  requiredConfirmations: number | null
}

export interface AnchorStatus {
  batchId: number
  batchStatus: 'DRAFT' | 'QC_PASSED' | 'ANCHORING' | 'ACTIVE' | 'ANCHOR_FAILED' | string
  snapshot?: SnapshotInfo
  privateChain?: PrivateChainStatus
  publicChain?: PublicChainStatus
  attempts?: number
  lastError?: string
  nextAttemptAt?: number
}

export interface ProofLeaf {
  seq: number
  eventType: string
  leafHash: string
  dataHash: string
  timestamp: number
}

export interface ProofEvent {
  seq: number
  eventType: string
  actionName: string
  performedBy: string
  timestamp: number
  metadataJson: string
}

export interface AnchorEntity {
  id: number
  chainId: number
  contractAddress: string
  txHash: string
  blockNumber: number
  status: string
  createdAt: number
}

export interface ProofBundle {
  batchCode: string
  batchIdHash: string
  hashAlgorithm: string
  canonicalization: string
  salt: string
  merkleRoot: string
  leaves: ProofLeaf[]
  events: ProofEvent[]
  anchor?: AnchorEntity
  generatedAt: number
}

export interface ExportQrParams {
  format?: 'pdf' | 'zpl' | 'csv'
  reprint?: boolean
  fromSerial?: number
  toSerial?: number
  batchQrOnly?: boolean
}

export interface BatchInfo {
  batchCode?: string
  status?: string
  varietyName?: string
  greenhouseCode?: string
  greenhouseName?: string
  sowDate?: string
  harvestDate?: string
  activatedAt?: number
  active?: boolean
  recalled?: boolean
}

export interface VarietyInfo {
  code?: string
  name?: string
}

export interface GreenhouseInfo {
  code?: string
  name?: string
  location?: string
}

export interface CertificateInfo {
  certType?: string
  certNo?: string
  validFrom?: string
  validTo?: string
}

export interface QcMetricInfo {
  brix?: number
  avgWeightKg?: number
  passed?: boolean
  inspectedAt?: number
}

export interface TimelineEvent {
  seq?: number
  eventType?: string
  status?: string
  recordedAt?: number
  eventHash?: string
  actorRef?: string
}

export interface BlockchainStatus {
  anchored: boolean
  chainName?: string
  merkleRoot?: string
  txHash?: string
  anchoredAt?: number
  explorerUrl?: string
}

export interface ScanInfo {
  scanCount: number
  lastScannedAt: number
  suspicious: boolean
  warningMessage?: string
}

export interface PublicTrace {
  code: string
  batch?: BatchInfo
  variety?: VarietyInfo
  greenhouse?: GreenhouseInfo
  certificates?: CertificateInfo[]
  metrics?: QcMetricInfo
  timeline?: TimelineEvent[]
  blockchain?: BlockchainStatus
  scanInfo?: ScanInfo
  recall?: {
    reason?: string
    createdAt?: number
  }
}

export interface VerifyResult {
  result: string
  computedRoot?: string
  onChainRoot?: string
  txHash?: string
  blockNumber?: number
  anchoredAt?: number
  explorerUrl?: string
  recalled: boolean
  checkedAt: number
  message: string
}

// ============================================================================
// MOCK (feat/build-ui)
// TODO: DELETE everything from here to the end of the file, plus the
// PageResponse import at the top, when the mock UI is removed
// ============================================================================

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