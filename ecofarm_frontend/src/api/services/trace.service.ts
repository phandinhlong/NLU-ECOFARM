import axios from '@/api/clients/axios'
import type { ApiResponse } from '@/api/types/common'
import type { PublicTrace, VerifyResult } from '@/api/types/batch'

const PUBLIC_TRACE = '/public/trace'

export const traceService = {
  getPublicTrace: (code: string) =>
    axios
      .get<ApiResponse<PublicTrace>>(`${PUBLIC_TRACE}/${encodeURIComponent(code)}`)
      .then((r) => r.data.data),

  verifyTrace: (code: string) =>
    axios
      .post<ApiResponse<VerifyResult>>(`${PUBLIC_TRACE}/${encodeURIComponent(code)}/verify`)
      .then((r) => r.data.data),
}
