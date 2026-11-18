import axios from '@/api/clients/axios'
import type { ApiResponse } from '@/api/types/common'
import type {
  Certificate,
  CertificateInput,
  CertificateQuery,
  CursorPageResponse,
  Greenhouse,
  GreenhouseInput,
  GreenhouseQuery,
  Variety,
  VarietyInput,
  VarietyQuery,
} from '@/api/types/catalog'
import { API_V1 } from '@/api/constants'

const unwrap = <T>(promise: Promise<{ data: ApiResponse<T> }>) => promise.then((r) => r.data.data)

const fetchAll = async <T>(
  fetchPage: (lastIndex?: number) => Promise<CursorPageResponse<T>>,
): Promise<T[]> => {
  const all: T[] = []
  let lastIndex: number | undefined
  let hasMore = true
  while (hasMore) {
    const page = await fetchPage(lastIndex)
    all.push(...page.content)
    hasMore = page.hasMore
    lastIndex = page.lastIndex ?? undefined
  }
  return all
}

export const catalogService = {
  listGreenhouses: (params: GreenhouseQuery = {}) =>
    unwrap(axios.get<ApiResponse<CursorPageResponse<Greenhouse>>>(`${API_V1}/greenhouses`, { params })),

  countGreenhouses: (params: GreenhouseQuery = {}) =>
    unwrap(axios.get<ApiResponse<number>>(`${API_V1}/greenhouses/statistics`, { params })),

  createGreenhouse: (data: GreenhouseInput) =>
    unwrap(axios.post<ApiResponse<Greenhouse>>(`${API_V1}/greenhouses`, data)),

  updateGreenhouse: (id: number, data: GreenhouseInput) =>
    unwrap(axios.put<ApiResponse<Greenhouse>>(`${API_V1}/greenhouses/${id}`, data)),

  listVarieties: (params: VarietyQuery = {}) =>
    unwrap(axios.get<ApiResponse<CursorPageResponse<Variety>>>(`${API_V1}/varieties`, { params })),

  countVarieties: (params: VarietyQuery = {}) =>
    unwrap(axios.get<ApiResponse<number>>(`${API_V1}/varieties/statistics`, { params })),

  createVariety: (data: VarietyInput) =>
    unwrap(axios.post<ApiResponse<Variety>>(`${API_V1}/varieties`, data)),

  updateVariety: (id: number, data: VarietyInput) =>
    unwrap(axios.put<ApiResponse<Variety>>(`${API_V1}/varieties/${id}`, data)),

  listCertificates: (params: CertificateQuery = {}) =>
    unwrap(
      axios.get<ApiResponse<CursorPageResponse<Certificate>>>(`${API_V1}/certificates`, { params }),
    ),

  countCertificates: (params: CertificateQuery = {}) =>
    unwrap(axios.get<ApiResponse<number>>(`${API_V1}/certificates/statistics`, { params })),

  createCertificate: (data: CertificateInput) =>
    unwrap(axios.post<ApiResponse<Certificate>>(`${API_V1}/certificates`, data)),

  fetchAllGreenhouses: () => fetchAll((lastIndex) => catalogService.listGreenhouses({ lastIndex })),

  fetchAllVarieties: () => fetchAll((lastIndex) => catalogService.listVarieties({ lastIndex })),
}
