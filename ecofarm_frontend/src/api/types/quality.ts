// Harvest and quality-control (QC) request/response types, mirroring the backend contract.

export interface HarvestRequest {
  clientEventId: string
  harvestedAt: number
  estimatedQuantity: number
  area?: string
}

export interface BatchView {
  id: number
  batchCode: string
  varietyId: number | null
  status: string
  harvestedAt: number | null
  harvestDate: string | null
}

export interface QcRequest {
  brix: number
  avgWeightKg: number
  confirmPassed?: boolean
  sizeDescription?: string
  note?: string
  attachmentFileKeys?: string[]
}

export interface QcView {
  id: number
  batchId: number | null
  brix: number | null
  avgWeightKg: number | null
  passed: boolean | null
  status: string | null
  inspectedBy: number | null
  inspectedAt: number | null
  sizeDescription: string | null
  note: string | null
  thresholdBrixUsed: number | null
  minIsolationDaysUsed: number | null
  isolationEligibleAt: number | null
  harvestedAtUsed: number | null
  superseded: boolean | null
}
