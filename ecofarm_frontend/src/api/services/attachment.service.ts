import axios from '@/api/clients/axios'
import { API_V1 } from '@/api/constants'
import type { ApiResponse } from '@/api/types/common'

export interface CreateAttachmentInput {
  eventId: number
  file: File
  capturedAt?: number
  gpsLat?: number
  gpsLng?: number
  gpsAccuracyM?: number
}

export const attachmentService = {
  createAttachment: (input: CreateAttachmentInput) => {
    const formData = new FormData()
    formData.append('eventId', input.eventId.toString())
    formData.append('file', input.file)
    if (input.capturedAt) {
      formData.append('capturedAt', input.capturedAt.toString())
    }
    if (input.gpsLat !== undefined) {
      formData.append('gpsLat', input.gpsLat.toString())
    }
    if (input.gpsLng !== undefined) {
      formData.append('gpsLng', input.gpsLng.toString())
    }
    if (input.gpsAccuracyM !== undefined) {
      formData.append('gpsAccuracyM', input.gpsAccuracyM.toString())
    }

    return axios
      .post<ApiResponse<boolean>>(`${API_V1}/event-attachments`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
      .then((r) => r.data.data)
  },
}
