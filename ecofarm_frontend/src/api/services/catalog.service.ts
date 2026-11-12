import axios from '@/api/clients/axios'
import type { ApiResponse } from '@/api/types/common'
import type {
  CatalogQuery,
  Greenhouse,
  GreenhouseInput,
  PageResponse,
  Variety,
  VarietyInput,
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

  listVarieties: (params: CatalogQuery) =>
    axios.get<ApiResponse<PageResponse<Variety>>>(`${API_V1}/varieties`, { params }),

  createVariety: (data: VarietyInput) =>
    axios.post<ApiResponse<Variety>>(`${API_V1}/varieties`, data),

  updateVariety: (id: number, data: VarietyInput) =>
    axios.put<ApiResponse<Variety>>(`${API_V1}/varieties/${id}`, data),
}
