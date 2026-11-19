import axios from '@/api/clients/axios'
import { API_V1 } from '@/api/constants'
import type { ApiResponse } from '@/api/types/common'
import type { CursorPageResponse, WorkerSummary } from '@/api/types/batch'

export const userService = {
  findUsers: (params: { role?: string; keyword?: string; lastIndex?: number }) =>
    axios
      .get<ApiResponse<CursorPageResponse<WorkerSummary>>>(`${API_V1}/users`, { params })
      .then((r) => r.data.data),
}
