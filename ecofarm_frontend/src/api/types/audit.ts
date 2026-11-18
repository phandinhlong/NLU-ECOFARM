import type { CursorPageResponse } from './batch'

export interface AuditLog {
  id: number
  actorId: number | null
  actorName: string | null
  actorEmail: string | null
  action: string
  entity: string
  entityId: number
  detail: string | null
  ipAddr: string | null
  createdAt: number
}

export interface AuditFilters {
  actorId?: number
  action?: string
  entity?: string
  entityId?: number
  from?: number
  to?: number
}

export interface AuditListParams extends AuditFilters {
  lastIndex?: number
}

export type AuditPage = CursorPageResponse<AuditLog>
