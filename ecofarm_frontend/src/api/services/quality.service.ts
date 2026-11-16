import axios from '@/api/clients/axios'
import { API_V1 } from '@/api/constants'
import type { ApiResponse } from '@/api/types/common'
import type { BatchView, HarvestRequest, QcRequest, QcView } from '@/api/types/quality'

// Harvest and QC endpoints. Harvest records a THU_HOACH event and flips the batch to HARVESTED;
// QC lets the server compute pass/fail from variety thresholds and the real journal.
export const qualityService = {
  harvest: (batchId: number, body: HarvestRequest) =>
    axios
      .post<ApiResponse<BatchView>>(`${API_V1}/batches/${batchId}/harvest`, body)
      .then((r) => r.data.data),

  recordQc: (batchId: number, body: QcRequest) =>
    axios
      .post<ApiResponse<QcView>>(`${API_V1}/batches/${batchId}/qc`, body)
      .then((r) => r.data.data),

  listQc: (batchId: number) =>
    axios
      .get<ApiResponse<QcView[]>>(`${API_V1}/batches/${batchId}/qc`)
      .then((r) => r.data.data),
}
