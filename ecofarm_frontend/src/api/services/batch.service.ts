import type { PageResponse } from '@/api/types/catalog'
import type {
  Batch,
  BatchCreateInput,
  BatchDetail,
  BatchListParams,
  BatchStatus,
  Problem,
  UserSummary,
} from '@/api/types/batch'
import { isLocked, previewBatchCode } from '@/api/types/batch'
import { useUserStore } from '@/stores'

interface MockUser {
  id: number
  fullName: string
  roles: string[]
}

interface GreenhouseMock {
  id: number
  code: string
  name: string
  gpsLat: number
  gpsLng: number
  areaM2: number
  status: 'ACTIVE'
}

interface VarietyMock {
  id: number
  code: string
  name: string
  daysPollinationToHarvest: number
  minBrix: number
  minIsolationDays: number
}

interface BatchRow {
  id: number
  batchCode: string
  greenhouseId: number
  varietyId: number
  status: BatchStatus
  sowDate: string
  pollinationDate: string | null
  expectedHarvestDate: string | null
  harvestDate: string | null
  activatedAt: number | null
}

const NOW = Date.UTC(2026, 10, 13)

const currentUser = (): MockUser => {
  const user = useUserStore().user
  return { id: user?.id ?? 0, fullName: user?.fullName ?? '', roles: user?.role ?? [] }
}

const greenhouses: GreenhouseMock[] = [
  { id: 1, code: 'GH-01', name: 'Nhà màng số 1', gpsLat: 10.8231, gpsLng: 106.6297, areaM2: 1000, status: 'ACTIVE' },
  { id: 2, code: 'GH-02', name: 'Nhà màng số 2', gpsLat: 10.8248, gpsLng: 106.6312, areaM2: 800, status: 'ACTIVE' },
  { id: 3, code: 'GH-03', name: 'Nhà màng số 3', gpsLat: 10.8265, gpsLng: 106.6338, areaM2: 1200, status: 'ACTIVE' },
]

const varieties: VarietyMock[] = [
  { id: 1, code: 'HUYNH_LONG', name: 'Dưa lưới Huỳnh Long', daysPollinationToHarvest: 55, minBrix: 13, minIsolationDays: 14 },
  { id: 2, code: 'KIM_HOANG', name: 'Dưa lưới Kim Hoàng', daysPollinationToHarvest: 60, minBrix: 12.5, minIsolationDays: 10 },
  { id: 3, code: 'TAN_THANH', name: 'Dưa lưới Tân Thanh', daysPollinationToHarvest: 50, minBrix: 14, minIsolationDays: 12 },
]

const users: UserSummary[] = [
  { id: 1, fullName: 'Nguyễn Văn Kỹ', roles: ['ENGINEER'], status: 'ACTIVE' },
  { id: 2, fullName: 'Trần Thị Quản', roles: ['ADMIN'], status: 'ACTIVE' },
  { id: 11, fullName: 'Lê Văn Công', roles: ['WORKER'], status: 'ACTIVE' },
  { id: 12, fullName: 'Phạm Thị Hoa', roles: ['WORKER'], status: 'ACTIVE' },
  { id: 13, fullName: 'Hoàng Văn Nam', roles: ['WORKER'], status: 'ACTIVE' },
  { id: 14, fullName: 'Võ Thị Lan', roles: ['WORKER'], status: 'LOCKED' },
]

const rawBatches: BatchRow[] = [
  { id: 1, batchCode: 'DL-GH01-051126', greenhouseId: 1, varietyId: 1, status: 'DRAFT', sowDate: '2026-11-20', pollinationDate: null, expectedHarvestDate: null, harvestDate: null, activatedAt: null },
  { id: 2, batchCode: 'DL-GH01-051126-2', greenhouseId: 1, varietyId: 2, status: 'DRAFT', sowDate: '2026-11-22', pollinationDate: null, expectedHarvestDate: null, harvestDate: null, activatedAt: null },
  { id: 3, batchCode: 'DL-GH02-100926', greenhouseId: 2, varietyId: 1, status: 'GROWING', sowDate: '2026-09-15', pollinationDate: '2026-10-10', expectedHarvestDate: '2026-12-04', harvestDate: null, activatedAt: null },
  { id: 4, batchCode: 'DL-GH02-200926', greenhouseId: 2, varietyId: 3, status: 'GROWING', sowDate: '2026-09-25', pollinationDate: null, expectedHarvestDate: null, harvestDate: null, activatedAt: null },
  { id: 5, batchCode: 'DL-GH03-010826', greenhouseId: 3, varietyId: 2, status: 'HARVESTED', sowDate: '2026-08-05', pollinationDate: '2026-09-01', expectedHarvestDate: '2026-10-31', harvestDate: '2026-10-30', activatedAt: null },
  { id: 6, batchCode: 'DL-GH03-150826', greenhouseId: 3, varietyId: 1, status: 'QC_PASSED', sowDate: '2026-08-15', pollinationDate: '2026-09-10', expectedHarvestDate: '2026-11-04', harvestDate: '2026-11-03', activatedAt: null },
  { id: 7, batchCode: 'DL-GH01-100826', greenhouseId: 1, varietyId: 3, status: 'REJECTED', sowDate: '2026-08-10', pollinationDate: '2026-09-05', expectedHarvestDate: '2026-10-25', harvestDate: '2026-10-26', activatedAt: null },
  { id: 8, batchCode: 'DL-GH01-010826', greenhouseId: 1, varietyId: 1, status: 'ANCHORING', sowDate: '2026-08-01', pollinationDate: '2026-08-28', expectedHarvestDate: '2026-10-22', harvestDate: '2026-10-22', activatedAt: null },
  { id: 9, batchCode: 'DL-GH02-250726', greenhouseId: 2, varietyId: 2, status: 'ANCHOR_FAILED', sowDate: '2026-07-25', pollinationDate: '2026-08-20', expectedHarvestDate: '2026-10-19', harvestDate: '2026-10-19', activatedAt: null },
  { id: 10, batchCode: 'DL-GH03-150726', greenhouseId: 3, varietyId: 1, status: 'ACTIVE', sowDate: '2026-07-15', pollinationDate: '2026-08-10', expectedHarvestDate: '2026-10-04', harvestDate: '2026-10-04', activatedAt: Date.UTC(2026, 9, 5) },
  { id: 11, batchCode: 'DL-GH01-050726', greenhouseId: 1, varietyId: 3, status: 'SHIPPED', sowDate: '2026-07-05', pollinationDate: '2026-08-01', expectedHarvestDate: '2026-09-20', harvestDate: '2026-09-20', activatedAt: Date.UTC(2026, 8, 21) },
  { id: 12, batchCode: 'DL-GH02-250626', greenhouseId: 2, varietyId: 2, status: 'RECALLED', sowDate: '2026-06-25', pollinationDate: '2026-07-20', expectedHarvestDate: '2026-09-18', harvestDate: '2026-09-18', activatedAt: Date.UTC(2026, 8, 19) },
]

const assignments: Record<number, number[]> = {
  1: [], 2: [11, 12], 3: [11, 12], 4: [12, 13], 5: [13], 6: [12],
  7: [13], 8: [11], 9: [12], 10: [11, 13], 11: [13], 12: [12],
}

const eventCounts: Record<number, { events: number; pending: number }> = {
  1: { events: 0, pending: 0 },
  3: { events: 6, pending: 2 },
  8: { events: 14, pending: 0 },
  10: { events: 18, pending: 0 },
}

const rows: BatchRow[] = rawBatches.map((row) => ({ ...row }))

const problem = (status: number, code: string, detail: string, errors?: Problem['errors']) =>
  Object.assign(new Error(detail), { status, code, detail, errors }) as Error & Problem

const delay = () => new Promise((resolve) => setTimeout(resolve, 200 + Math.random() * 150))

const greenhouseOf = (id: number) => greenhouses.find((item) => item.id === id)
const varietyOf = (id: number) => varieties.find((item) => item.id === id)
const userOf = (id: number) => users.find((item) => item.id === id)

const toBatch = (row: BatchRow): Batch => {
  const gh = greenhouseOf(row.greenhouseId)
  const variety = varietyOf(row.varietyId)
  return {
    id: row.id,
    batchCode: row.batchCode,
    greenhouseId: row.greenhouseId,
    varietyId: row.varietyId,
    status: row.status,
    sowDate: row.sowDate,
    harvestDate: row.harvestDate,
    pollinationDate: row.pollinationDate,
    expectedHarvestDate: row.expectedHarvestDate,
    createdBy: 1,
    activatedAt: row.activatedAt,
    version: 1,
    createdAt: NOW,
    updatedAt: NOW,
    greenhouseCode: gh?.code ?? '—',
    varietyName: variety?.name ?? '—',
    workerCount: assignments[row.id]?.length ?? 0,
  }
}

const isWorker = () => currentUser().roles.includes('WORKER')
const isEngineer = () => currentUser().roles.includes('ENGINEER')

const visibleRows = (): BatchRow[] => {
  if (!isWorker()) return rows
  return rows.filter((row) => (assignments[row.id] ?? []).includes(currentUser().id))
}

const toDetail = (row: BatchRow): BatchDetail => {
  const ids = assignments[row.id] ?? []
  const counts = eventCounts[row.id] ?? { events: 0, pending: 0 }
  return {
    ...toBatch(row),
    greenhouse: greenhouseOf(row.greenhouseId) ?? { id: row.greenhouseId, code: '—', name: '—' },
    variety: varietyOf(row.varietyId) ?? { id: row.varietyId, code: '—', name: '—', daysPollinationToHarvest: null },
    assignedUserIds: [...ids],
    assignedWorkerNames: ids.map((id) => userOf(id)?.fullName ?? `#${id}`),
    eventCount: counts.events,
    pendingApprovalCount: counts.pending,
  }
}

const listBatches = async (params: BatchListParams): Promise<PageResponse<Batch>> => {
  await delay()
  const me = currentUser()
  const q = params.q?.toLowerCase().trim()
  const filtered = visibleRows()
    .filter((row) => (params.status ? row.status === params.status : true))
    .filter((row) => (params.greenhouseId ? row.greenhouseId === params.greenhouseId : true))
    .filter((row) => (q ? row.batchCode.toLowerCase().includes(q) : true))
    .filter((row) => (params.mine ? (assignments[row.id] ?? []).includes(me.id) : true))

  const size = params.size || 10
  const totalElements = filtered.length
  const totalPages = Math.ceil(totalElements / size) || 0
  const page = Math.min(params.page, Math.max(totalPages - 1, 0))
  const content = filtered.slice(page * size, page * size + size).map(toBatch)
  return { content, page, size, totalElements, totalPages }
}

const getBatch = async (id: number): Promise<BatchDetail> => {
  await delay()
  const me = currentUser()
  const row = rows.find((item) => item.id === id)
  if (!row) throw problem(404, 'NOT_FOUND', 'Không tìm thấy lô.')
  if (me.roles.includes('WORKER') && !(assignments[row.id] ?? []).includes(me.id)) {
    throw problem(403, 'NOT_ASSIGNED', 'Bạn không được giao lô này.')
  }
  return toDetail(row)
}

const createBatch = async (input: BatchCreateInput): Promise<Batch> => {
  await delay()
  if (!isEngineer()) throw problem(403, 'FORBIDDEN', 'Chỉ kỹ sư mới có thể tạo lô.')
  const errors: Problem['errors'] = []
  const gh = input.greenhouseId ? greenhouseOf(input.greenhouseId) : undefined
  const variety = input.varietyId ? varietyOf(input.varietyId) : undefined
  if (!gh) errors.push({ field: 'greenhouseId', message: 'Vui lòng chọn nhà màng.' })
  if (!variety) errors.push({ field: 'varietyId', message: 'Vui lòng chọn giống dưa.' })
  if (!input.sowDate) errors.push({ field: 'sowDate', message: 'Vui lòng chọn ngày xuống giống.' })
  if (errors.length > 0 || !gh || !variety) {
    throw problem(422, 'VALIDATION_FAILED', 'Dữ liệu không hợp lệ.', errors)
  }

  const batchCode = previewBatchCode(gh.code, input.sowDate, rows.map((row) => row.batchCode))
  const row: BatchRow = {
    id: Math.max(...rows.map((item) => item.id), 0) + 1,
    batchCode,
    greenhouseId: gh.id,
    varietyId: variety.id,
    status: 'DRAFT',
    sowDate: input.sowDate,
    pollinationDate: null,
    expectedHarvestDate: null,
    harvestDate: null,
    activatedAt: null,
  }
  rows.unshift(row)
  assignments[row.id] = []
  return toBatch(row)
}

const assignWorkers = async (batchId: number, userIds: number[]): Promise<string[]> => {
  await delay()
  if (!isEngineer()) throw problem(403, 'FORBIDDEN', 'Chỉ kỹ sư mới có thể phân công.')
  const row = rows.find((item) => item.id === batchId)
  if (!row) throw problem(404, 'NOT_FOUND', 'Không tìm thấy lô.')
  if (isLocked(row.status)) throw problem(409, 'BATCH_LOCKED', 'Lô đã khóa dữ liệu, không thể phân công.')
  assignments[batchId] = [...userIds]
  return userIds.map((id) => userOf(id)?.fullName ?? `#${id}`)
}

const listWorkers = async (): Promise<UserSummary[]> => {
  await delay()
  return users.filter((user) => user.roles.includes('WORKER') && user.status === 'ACTIVE')
}

const listGreenhouses = async (): Promise<{ id: number; code: string; name: string }[]> => {
  await delay()
  return greenhouses.map(({ id, code, name }) => ({ id, code, name }))
}

const listVarieties = async (): Promise<{ id: number; code: string; name: string; daysPollinationToHarvest: number }[]> => {
  await delay()
  return varieties.map(({ id, code, name, daysPollinationToHarvest }) => ({ id, code, name, daysPollinationToHarvest }))
}

export const batchService = {
  listBatches,
  getBatch,
  createBatch,
  assignWorkers,
  listWorkers,
  listGreenhouses,
  listVarieties,
}
