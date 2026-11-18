import axios from '@/api/clients/axios'
import { API_V1 } from '@/api/constants'
import type { ApiResponse } from '@/api/types/common'
import type {
  Shipment,
  ShipmentCreateInput,
  ShipmentEligibleBatch,
  ShipmentReceiveInput,
} from '@/api/types/shipment'

export const shipmentService = {
  createShipment: (batchId: number, data: ShipmentCreateInput) =>
    axios
      .post<ApiResponse<Shipment>>(`${API_V1}/batches/${batchId}/shipments`, data)
      .then((r) => r.data.data),

  receiveShipment: (shipmentId: number, data: ShipmentReceiveInput) =>
    axios
      .post<ApiResponse<Shipment>>(`${API_V1}/shipments/${shipmentId}/receive`, data)
      .then((r) => r.data.data),

  listEligibleBatches: () =>
    axios
      .get<ApiResponse<ShipmentEligibleBatch[]>>(`${API_V1}/shipments/eligible-batches`)
      .then((r) => r.data.data),

  listShipmentsByBatch: (batchId: number) =>
    axios
      .get<ApiResponse<Shipment[]>>(`${API_V1}/batches/${batchId}/shipments`)
      .then((r) => r.data.data),

  listShipments: (params?: { status?: string }) =>
    axios
      .get<ApiResponse<Shipment[]>>(`${API_V1}/shipments`, { params })
      .then((r) => r.data.data),
}
