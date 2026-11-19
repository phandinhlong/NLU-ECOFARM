export type QcStatus = 'QC_PASSED' | 'FAILED' | 'REJECTED' | 'PENDING_REVIEW' | 'SUPERSEDED'

export interface RecordQcRequest {
  clientEventId: string
  brixSamples: number[]
  weightSamples: number[]
  passed: boolean
  photoHashes?: string[]
  harvestDate?: string | null
  note?: string | null
  occurredAt?: number | null
}

export interface ReviewQcRequest {
  decision: 'APPROVED' | 'REJECTED'
  note: string
}

export interface HarvestRequest {
  clientEventId: string
  harvestedAt?: number | null
  estimatedQuantity?: number | null
  area?: string | null
  note?: string | null
}

export interface QcCriteriaEvaluation {
  brix?: {
    avg?: number
    min?: number
    max?: number
    passed?: boolean
  }
  weight?: {
    avg?: number
    min?: number
    max?: number
    passed?: boolean
  }
  quarantine?: {
    days?: number
    required?: number
    passed?: boolean
  }
  [key: string]: any
}

export interface QcResponse {
  id: number
  eventId?: number | null
  batchId: number
  batchCode: string
  avgBrix: number
  avgWeightKg: number
  passed: boolean
  status: QcStatus
  attemptNumber: number
  failureReason?: string | null
  quarantineDaysActual?: number | null
  quarantineDaysRequired?: number | null
  quarantineDaysDeficit?: number | null
  brixSamples?: number[]
  weightSamples?: number[]
  photoHashes?: string[]
  inspectedBy?: number | null
  inspectedAt?: number | null
  actorRef?: string | null
  clientEventId?: string | null
  eventHash?: string | null
  adminDecision?: string | null
  adminNote?: string | null
  reviewedBy?: number | null
  reviewedAt?: number | null
  systemSuggestedPassed?: boolean | null
  expectedHarvestDate?: string | null
  daysSinceSowing?: number | null
  criteriaEvaluation?: QcCriteriaEvaluation | null
  message?: string | null
}

export interface HarvestResponse {
  batchId: number
  batchCode: string
  status: string
  harvestDate?: string | null
  estimatedQuantity?: number | null
  area?: string | null
  eventHash?: string | null
  besuTxHash?: string | null
  besuBlockNumber?: number | null
  explorerUrl?: string | null
  message?: string | null
}
