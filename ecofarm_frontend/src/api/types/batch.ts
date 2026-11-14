import type { ApiResponse } from './common'

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
  blockchain?: BlockchainStatus
  scanInfo?: ScanInfo
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

