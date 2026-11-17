export type GreenhouseStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE'

export interface CursorPageResponse<T> {
  content: T[]
  lastIndex: number | null
  hasMore: boolean
}

export interface GreenhouseQuery {
  status?: string
  q?: string
  lastIndex?: number
}

export interface VarietyQuery {
  q?: string
  lastIndex?: number
}

export interface CertificateQuery {
  greenhouseId?: number
  expiringInDays?: number
  lastIndex?: number
}

export interface Greenhouse {
  id: number
  code: string
  name: string
  gpsLat: number | null
  gpsLng: number | null
  areaM2: number | null
  status: GreenhouseStatus
  version: number
  batchCount: number
}

export interface GreenhouseInput {
  code: string
  name: string
  gpsLat?: number | null
  gpsLng?: number | null
  areaM2?: number | null
  status?: GreenhouseStatus
  version?: number
}

export interface Variety {
  id: number
  code: string
  name: string
  daysPollinationToHarvest: number | null
  minBrix: number | null
  minIsolationDays: number | null
  version: number
  batchCount: number
}

export interface VarietyInput {
  code: string
  name: string
  daysPollinationToHarvest?: number | null
  minBrix?: number | null
  minIsolationDays?: number | null
  version?: number
}

export type CertType = 'VIETGAP' | 'GLOBALGAP' | 'ORGANIC_INTERNAL'

export interface Certificate {
  id: number
  greenhouseId: number | null
  certType: CertType | string
  certNo: string
  validFrom: string | null
  validTo: string | null
  fileKey: string | null
  active: boolean
  version: number
  createdAt: number
  updatedAt: number
  downloadUrl: string | null
}

export interface CertificateInput {
  greenhouseId: number | null
  certType: string
  certNo: string
  validFrom: string
  validTo: string
  fileKey?: string | null
}

export const CERT_TYPE_LABELS: Record<string, string> = {
  VIETGAP: 'VietGAP',
  GLOBALGAP: 'GlobalGAP',
  ORGANIC_INTERNAL: 'Organic (nội bộ)',
}

export const GREENHOUSE_STATUS_LABELS: Record<GreenhouseStatus, string> = {
  ACTIVE: 'Đang hoạt động',
  INACTIVE: 'Ngừng hoạt động',
  MAINTENANCE: 'Bảo trì',
}
