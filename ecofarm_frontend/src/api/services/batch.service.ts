import axios from '@/api/clients/axios'
import { API_V1 } from '@/api/constants'
import type { ApiResponse } from '@/api/types/common'
import type {
  ActivateBatchRequest,
  ActivateResponse,
  AnchorStatus,
  Batch,
  BatchCreateInput,
  BatchDetail,
  BatchListParams,
  CursorPageResponse,
  ExportQrParams,
  ExtraQrRequest,
  ExtraQrResponse,
  ProofBundle,
  PublicTrace,
  VerifyResult,
  WorkerSummary,
} from '@/api/types/batch'
import { catalogService } from '@/api/services/catalog.service'

export const batchService = {
  activateBatch: (batchId: number, data: ActivateBatchRequest) =>
    axios.post<ApiResponse<ActivateResponse>>(`${API_V1}/batches/${batchId}/activate`, data),

  exportQrLabels: (batchId: number, params?: ExportQrParams) =>
    axios.get(`${API_V1}/batches/${batchId}/qr`, {
      params,
      responseType: 'blob',
    }),

  generateExtraQrCodes: (batchId: number, data: ExtraQrRequest) =>
    axios.post<ApiResponse<ExtraQrResponse>>(`${API_V1}/batches/${batchId}/qr/extra`, data),

  getAnchorStatus: (batchId: number) =>
    axios.get<AnchorStatus>(`${API_V1}/block-chain/batches/${batchId}/anchor-status`),

  getProofBundle: (batchId: number) =>
    axios.get<ProofBundle>(`${API_V1}/block-chain/batches/${batchId}/proof-bundle`),

  getPublicTrace: (code: string) =>
    axios.get<PublicTrace>(`${API_V1}/block-chain/public/trace/${code}`),

  verifyTrace: (code: string) =>
    axios.post<VerifyResult>(`${API_V1}/block-chain/public/trace/${code}/verify`),

  listBatches: (params: BatchListParams) =>
    axios
      .get<ApiResponse<CursorPageResponse<Batch>>>(`${API_V1}/batches`, { params })
      .then((r) => r.data.data),

  getBatch: (id: number) =>
    axios.get<ApiResponse<BatchDetail>>(`${API_V1}/batches/${id}`).then((r) => r.data.data),

  createBatch: (input: BatchCreateInput) =>
    axios.post<ApiResponse<BatchDetail>>(`${API_V1}/batches`, input).then((r) => r.data.data),

  addWorkers: (batchId: number, userIds: number[]) =>
    axios
      .post<ApiResponse<boolean>>(`${API_V1}/batches/${batchId}/workers`, { userIds })
      .then((r) => r.data.data),

  removeWorkers: (batchId: number, userIds: number[]) =>
    axios
      .delete<ApiResponse<boolean>>(`${API_V1}/batches/${batchId}/workers`, { data: { userIds } })
      .then((r) => r.data.data),

  listWorkers: (params?: { lastIndex?: number }) =>
    axios
      .get<ApiResponse<CursorPageResponse<WorkerSummary>>>(`${API_V1}/batches/workers`, { params })
      .then((r) => r.data.data),

  listAssignedWorkers: (batchId: number, params?: { lastIndex?: number }) =>
    axios
      .get<ApiResponse<CursorPageResponse<WorkerSummary>>>(`${API_V1}/batches/${batchId}/workers`, {
        params,
      })
      .then((r) => r.data.data),

  listGreenhouses: () =>
    catalogService.listGreenhouses({ page: 0, size: 100 }).then((r) => r.data.data.content),

  listVarieties: () =>
    catalogService.listVarieties({ page: 0, size: 100 }).then((r) => r.data.data.content),
}
