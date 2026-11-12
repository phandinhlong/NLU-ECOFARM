export type GreenhouseStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE'

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

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
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

export interface CertificateQuery {
  page?: number
  size?: number
  greenhouseId?: number
  expiringInDays?: number
}

export const CERT_TYPE_LABELS: Record<string, string> = {
  VIETGAP: 'VietGAP',
  GLOBALGAP: 'GlobalGAP',
  ORGANIC_INTERNAL: 'Organic (nội bộ)',
}

export interface CatalogQuery {
  page?: number
  size?: number
  status?: string
  q?: string
}

export const GREENHOUSE_STATUS_LABELS: Record<GreenhouseStatus, string> = {
  ACTIVE: 'Đang hoạt động',
  INACTIVE: 'Ngừng hoạt động',
  MAINTENANCE: 'Bảo trì',
}
