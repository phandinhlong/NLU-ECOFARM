import axios from '@/api/clients/axios'
import { API_V1 } from '@/api/constants'
import type { ApiResponse } from '@/api/types/common'
import type { CursorPageResponse } from '@/api/types/batch'
import type {
  AmendEventInput,
  CreateEventInput,
  CultivationEvent,
  EventReviewStatus,
  ReassignApproverInput,
  RejectEventInput,
} from '@/api/types/event'

export const eventService = {
  listEvents: (batchId: number, params?: { lastIndex?: number; reviewStatus?: EventReviewStatus }) =>
    axios
      .get<ApiResponse<CursorPageResponse<CultivationEvent>>>(`${API_V1}/batches/${batchId}/events`, { params })
      .then((r) => r.data.data),

  createEvent: (batchId: number, input: CreateEventInput) =>
    axios
      .post<ApiResponse<CultivationEvent>>(`${API_V1}/batches/${batchId}/events`, input)
      .then((r) => r.data.data),

  findReviewQueue: (params?: { lastIndex?: number; greenhouseId?: number; reviewStatus?: EventReviewStatus }) =>
    axios
      .get<ApiResponse<CursorPageResponse<CultivationEvent>>>(`${API_V1}/events`, { params })
      .then((r) => r.data.data),

  countReviewQueue: (params?: { greenhouseId?: number; reviewStatus?: EventReviewStatus }) =>
    axios
      .get<ApiResponse<number>>(`${API_V1}/events/review-count`, { params })
      .then((r) => r.data.data),

  findEvent: (id: number) =>
    axios.get<ApiResponse<CultivationEvent>>(`${API_V1}/events/${id}`).then((r) => r.data.data),

  approve: (id: number) =>
    axios
      .post<ApiResponse<CultivationEvent>>(`${API_V1}/events/${id}/approve`)
      .then((r) => r.data.data),

  reject: (id: number, input: RejectEventInput) =>
    axios
      .post<ApiResponse<CultivationEvent>>(`${API_V1}/events/${id}/reject`, input)
      .then((r) => r.data.data),

  reassignApprover: (id: number, input: ReassignApproverInput) =>
    axios
      .post<ApiResponse<CultivationEvent>>(`${API_V1}/events/${id}/reassign-approver`, input)
      .then((r) => r.data.data),

  amend: (id: number, input: AmendEventInput) =>
    axios
      .post<ApiResponse<CultivationEvent>>(`${API_V1}/events/${id}/amend`, input)
      .then((r) => r.data.data),
}
