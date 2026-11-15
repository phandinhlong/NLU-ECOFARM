import axios from '@/api/clients/axios'
import { API_V1 } from '@/api/constants'
import type { ApiResponse } from '@/api/types/common'
import type {
  ActivateBatchRequest,
  ActivateResponse,
  AnchorStatus,
  ExportQrParams,
  ExtraQrRequest,
  ExtraQrResponse,
  ProofBundle,
  PublicTrace,
  VerifyResult,
} from '@/api/types/batch'

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
}
