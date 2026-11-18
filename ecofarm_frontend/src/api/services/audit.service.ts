import axios from '@/api/clients/axios'
import { API_V1 } from '@/api/constants'
import type { ApiResponse } from '@/api/types/common'
import type { AuditFilters, AuditListParams, AuditPage } from '@/api/types/audit'

export const auditService = {
  list: (params: AuditListParams = {}) =>
    axios.get<ApiResponse<AuditPage>>(`${API_V1}/audit-logs`, { params }).then((r) => r.data.data),

  count: (params: AuditFilters = {}) =>
    axios.get<ApiResponse<number>>(`${API_V1}/audit-logs/statistics`, { params }).then((r) => r.data.data),
}
