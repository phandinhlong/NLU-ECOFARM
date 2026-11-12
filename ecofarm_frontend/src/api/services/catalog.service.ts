import axios from '@/api/clients/axios'
import type { ApiResponse } from '@/api/types/common'
import type {
  CatalogQuery,
  Greenhouse,
  GreenhouseInput,
  PageResponse,
} from '@/api/types/catalog'
import { API_V1 } from '@/api/constants'

export const catalogService = {
  listGreenhouses: (params: CatalogQuery) =>
    axios.get<ApiResponse<PageResponse<Greenhouse>>>(`${API_V1}/greenhouses`, { params }),

  getGreenhouse: (id: number) =>
    axios.get<ApiResponse<Greenhouse>>(`${API_V1}/greenhouses/${id}`),

  createGreenhouse: (data: GreenhouseInput) =>
    axios.post<ApiResponse<Greenhouse>>(`${API_V1}/greenhouses`, data),

  updateGreenhouse: (id: number, data: GreenhouseInput) =>
    axios.put<ApiResponse<Greenhouse>>(`${API_V1}/greenhouses/${id}`, data),
}
