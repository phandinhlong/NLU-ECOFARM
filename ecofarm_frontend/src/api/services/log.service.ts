import axios from '@/api/clients/axios'
import { API_V1 } from '@/api/constants'
import type { ApiResponse } from '@/api/types/common'
import type {
  AmendRequest,
  AttachmentView,
  CreateEventRequest,
  EventListParams,
  EventPage,
  EventView,
  RejectRequest,
} from '@/api/types/log'

// LOG endpoints. Batch-scoped paths create/list events and manage attachments;
// event-scoped paths handle engineer review (approve/reject) and amendment.
export const logService = {
  listEvents: (batchId: number, params: EventListParams) =>
    axios
      .get<ApiResponse<EventPage>>(`${API_V1}/batches/${batchId}/events`, { params })
      .then((r) => r.data.data),

  createEvent: (batchId: number, body: CreateEventRequest) =>
    axios
      .post<ApiResponse<EventView>>(`${API_V1}/batches/${batchId}/events`, body)
      .then((r) => r.data.data),

  uploadAttachments: (batchId: number, files: File[]) => {
    const form = new FormData()
    files.forEach((file) => form.append('files', file))
    return axios
      .post<ApiResponse<AttachmentView[]>>(`${API_V1}/batches/${batchId}/events/attachments`, form, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then((r) => r.data.data)
  },

  attachmentUrl: (batchId: number, fileKey: string) =>
    `${API_V1}/batches/${batchId}/events/attachments/${fileKey}`,

  approveEvent: (eventId: number) =>
    axios.post<ApiResponse<unknown>>(`${API_V1}/events/${eventId}/approve`).then((r) => r.data.data),

  rejectEvent: (eventId: number, body: RejectRequest) =>
    axios
      .post<ApiResponse<unknown>>(`${API_V1}/events/${eventId}/reject`, body)
      .then((r) => r.data.data),

  amendEvent: (eventId: number, body: AmendRequest) =>
    axios
      .post<ApiResponse<unknown>>(`${API_V1}/events/${eventId}/amend`, body)
      .then((r) => r.data.data),
}
