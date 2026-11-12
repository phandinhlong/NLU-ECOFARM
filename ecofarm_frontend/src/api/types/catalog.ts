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

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
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
