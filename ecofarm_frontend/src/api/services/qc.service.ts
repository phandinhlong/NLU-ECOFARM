import axios from '@/api/clients/axios'
import { API_V1 } from '@/api/constants'
import type { ApiResponse } from '@/api/types/common'
import type {
  HarvestRequest,
  HarvestResponse,
  QcResponse,
  RecordQcRequest,
  ReviewQcRequest,
} from '@/api/types/qc'

export const qcService = {
  recordQc: (batchId: number, data: RecordQcRequest) =>
    axios
      .post<ApiResponse<QcResponse>>(`${API_V1}/batches/${batchId}/qc`, data)
      .then((r) => r.data.data),

  getLatestQc: (batchId: number) =>
    axios
      .get<ApiResponse<QcResponse>>(`${API_V1}/batches/${batchId}/qc`)
      .then((r) => r.data.data),

  reviewQc: (batchId: number, data: ReviewQcRequest) =>
    axios
      .post<ApiResponse<QcResponse>>(`${API_V1}/batches/${batchId}/qc/review`, data)
      .then((r) => r.data.data),

  harvestBatch: (batchId: number, data: HarvestRequest) =>
    axios
      .post<ApiResponse<HarvestResponse>>(`${API_V1}/batches/${batchId}/harvest`, data)
      .then((r) => r.data.data),
}
