import axios from '@/api/clients/axios'
import { API_V1 } from '@/api/constants'
import type { ApiResponse } from '@/api/types/common'
import type { CursorPageResponse } from '@/api/types/batch'
import type { BatchTask, CompleteTaskInput, CreateTaskInput, TaskStatus } from '@/api/types/task'

export const taskService = {
  listTasks: (batchId: number, params?: { lastIndex?: number; status?: TaskStatus }) =>
    axios
      .get<ApiResponse<CursorPageResponse<BatchTask>>>(`${API_V1}/batches/${batchId}/tasks`, { params })
      .then((r) => r.data.data),

  createTask: (batchId: number, input: CreateTaskInput) =>
    axios
      .post<ApiResponse<BatchTask>>(`${API_V1}/batches/${batchId}/tasks`, input)
      .then((r) => r.data.data),

  completeTask: (taskId: number, input: CompleteTaskInput) =>
    axios
      .post<ApiResponse<BatchTask>>(`${API_V1}/tasks/${taskId}/complete`, input)
      .then((r) => r.data.data),

  cancelTask: (taskId: number, reason: string) =>
    axios
      .post<ApiResponse<boolean>>(`${API_V1}/tasks/${taskId}/cancel`, { reason })
      .then((r) => r.data.data),
}
