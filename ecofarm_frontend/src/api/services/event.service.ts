import axios from '@/api/clients/axios'
import { API_V1 } from '@/api/constants'
import type { ApiResponse } from '@/api/types/common'
import type { CursorPageResponse } from '@/api/types/batch'
import type { CreateEventInput, CultivationEvent, EventReviewStatus } from '@/api/types/event'

export const eventService = {
  listEvents: (batchId: number, params?: { lastIndex?: number; reviewStatus?: EventReviewStatus }) =>
    axios
      .get<ApiResponse<CursorPageResponse<CultivationEvent>>>(`${API_V1}/batches/${batchId}/events`, { params })
      .then((r) => r.data.data),

  createEvent: (batchId: number, input: CreateEventInput) =>
    axios
      .post<ApiResponse<CultivationEvent>>(`${API_V1}/batches/${batchId}/events`, input)
      .then((r) => r.data.data),
}
