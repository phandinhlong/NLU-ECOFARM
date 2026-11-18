export type ShipmentStatus = 'SHIPPED' | 'RECEIVED' | 'REJECTED' | 'IN_TRANSIT' | 'DELIVERED'

export interface ShipmentCreateInput {
  destination: string
  vehiclePlate: string
  shippedAt?: number
  quantity: number
}

export interface ShipmentReceiveInput {
  accepted: boolean
  receivedAt?: number
  conditionNote?: string
  photoUrls?: string[]
}

export interface Shipment {
  id: number
  batchId: number | null
  batchCode: string | null
  destination: string
  vehiclePlate: string
  shippedAt: number | null
  receivedAt: number | null
  receivedBy: number | null
  quantity: number | null
  status: ShipmentStatus | null
  conditionNote: string | null
  photoUrls: string[] | null
  version: number | null
  createdAt: number | null
  updatedAt: number | null
}

export interface ShipmentEligibleBatch {
  id: number
  batchCode: string
  greenhouseCode: string | null
  greenhouseName: string | null
  varietyName: string | null
  status: string | null
}

type Tone = 'success' | 'warning' | 'danger' | 'info' | 'muted'

export const SHIPMENT_STATUS_META: Record<ShipmentStatus, { label: string; tone: Tone }> = {
  SHIPPED: { label: 'Đang giao', tone: 'info' },
  RECEIVED: { label: 'Đã nhận hàng', tone: 'success' },
  REJECTED: { label: 'Bị từ chối', tone: 'danger' },
  IN_TRANSIT: { label: 'Đang vận chuyển', tone: 'info' },
  DELIVERED: { label: 'Đã giao', tone: 'success' },
}

export const shipmentStatusLabel = (status?: string | null): string => {
  if (!status) return '—'
  return SHIPMENT_STATUS_META[status as ShipmentStatus]?.label ?? status
}

export const shipmentStatusTone = (status?: string | null): Tone => {
  if (!status) return 'muted'
  return SHIPMENT_STATUS_META[status as ShipmentStatus]?.tone ?? 'muted'
}

export const formatDateTime = (val?: number | null): string => {
  if (!val) return '—'
  const d = new Date(val)
  if (isNaN(d.getTime())) return String(val)
  const day = String(d.getDate()).padStart(2, '0')
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const year = d.getFullYear()
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${day}/${month}/${year} ${hh}:${mm}`
}
