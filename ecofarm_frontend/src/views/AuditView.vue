<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { auditService } from '@/api/services/audit.service'
import type { AuditFilters, AuditLog } from '@/api/types/audit'

const form = reactive({ actorId: '', action: '', entity: '', entityId: '', from: '', to: '' })
const filters = ref<AuditFilters>({})
const rows = ref<AuditLog[]>([])
const total = ref<number | null>(null)
const cursors = ref<(number | undefined)[]>([undefined])
const pageIndex = ref(0)
const hasMore = ref(false)
const loading = ref(false)
const error = ref('')
const countError = ref('')
const validationError = ref('')
const selected = ref<AuditLog | null>(null)
const showDetail = ref(false)
const hasPrev = computed(() => pageIndex.value > 0)

const formatTime = (timestamp: number) => new Date(timestamp).toLocaleString('vi-VN')
const messageOf = (err: unknown) => err instanceof Error ? err.message : 'Vui lòng thử lại.'

const parseId = (value: string, label: string) => {
  if (!value.trim()) return undefined
  const id = Number(value)
  if (!/^\d+$/.test(value.trim()) || !Number.isSafeInteger(id) || id <= 0) {
    throw new Error(`${label} phải là số nguyên dương.`)
  }
  return id
}

const parseTime = (value: string) => {
  if (!value) return undefined
  // datetime-local is intentionally interpreted in the browser's local timezone.
  const timestamp = new Date(value).getTime()
  if (!Number.isFinite(timestamp)) throw new Error('Ngày giờ không hợp lệ.')
  return timestamp
}

const load = async (target = 0, refreshCount = false) => {
  if (loading.value) return
  const lastIndex = cursors.value[target]
  if (target > 0 && (lastIndex == null || !Number.isSafeInteger(lastIndex) || lastIndex <= 0)) {
    error.value = 'Con trỏ phân trang không hợp lệ. Vui lòng đặt lại bộ lọc.'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const page = await auditService.list({ ...filters.value, lastIndex })
    const nextCursor = page.lastIndex
    if (page.hasMore && (nextCursor == null || !Number.isSafeInteger(nextCursor) || nextCursor <= 0 || cursors.value.slice(0, target + 1).includes(nextCursor))) {
      throw new Error('Con trỏ phân trang không hợp lệ. Vui lòng đặt lại bộ lọc.')
    }
    rows.value = page.content
    hasMore.value = page.hasMore
    pageIndex.value = target
    cursors.value = cursors.value.slice(0, target + 1)
    if (page.hasMore && nextCursor != null) cursors.value.push(nextCursor)
  } catch (err) {
    error.value = `Không tải được nhật ký: ${messageOf(err)}`
  }
  if (refreshCount) {
    countError.value = ''
    total.value = null
    try {
      total.value = await auditService.count(filters.value)
    } catch (err) {
      countError.value = `Không tải được tổng số bản ghi: ${messageOf(err)}`
    }
  }
  loading.value = false
}

const applyFilters = () => {
  if (loading.value) return
  validationError.value = ''
  try {
    const from = parseTime(form.from)
    const to = parseTime(form.to)
    if (from != null && to != null && from > to) {
      throw new Error('Thời điểm bắt đầu không được sau thời điểm kết thúc.')
    }
    filters.value = {
      actorId: parseId(form.actorId, 'ID người thực hiện'),
      action: form.action.trim() || undefined,
      entity: form.entity.trim() || undefined,
      entityId: parseId(form.entityId, 'ID đối tượng'),
      from,
      to,
    }
    rows.value = []
    total.value = null
    pageIndex.value = 0
    cursors.value = [undefined]
    hasMore.value = false
    selected.value = null
    showDetail.value = false
    void load(0, true)
  } catch (err) {
    validationError.value = messageOf(err)
  }
}

const reset = () => {
  if (loading.value) return
  Object.assign(form, { actorId: '', action: '', entity: '', entityId: '', from: '', to: '' })
  applyFilters()
}

const openDetail = (row: AuditLog) => {
  selected.value = row
  showDetail.value = true
}

onMounted(() => load(0, true))
</script>

<template>
  <div>
    <PageHeader title="Quản lý audit" />
    <p class="text-body-secondary small">Nhật ký kiểm toán chỉ đọc. Ngày giờ được hiển thị và lọc theo múi giờ trên thiết bị.</p>

    <form class="border rounded-3 bg-body p-3 p-lg-4 mb-3" novalidate @submit.prevent="applyFilters">
      <fieldset :disabled="loading">
        <legend class="h6">Bộ lọc nhật ký</legend>
        <div class="row g-3">
          <div class="col-12 col-md-4">
            <label for="audit-actor" class="form-label small fw-semibold">ID người thực hiện</label>
            <input id="audit-actor" v-model="form.actorId" class="form-control" inputmode="numeric" placeholder="Số nguyên dương" />
          </div>
          <div class="col-12 col-md-4">
            <label for="audit-action" class="form-label small fw-semibold">Hành động</label>
            <input id="audit-action" v-model="form.action" class="form-control" placeholder="VD: CREATE" />
          </div>
          <div class="col-12 col-md-4">
            <label for="audit-entity" class="form-label small fw-semibold">Đối tượng</label>
            <input id="audit-entity" v-model="form.entity" class="form-control" placeholder="Tên loại đối tượng" />
          </div>
          <div class="col-12 col-md-4">
            <label for="audit-entity-id" class="form-label small fw-semibold">ID đối tượng</label>
            <input id="audit-entity-id" v-model="form.entityId" class="form-control" inputmode="numeric" placeholder="Số nguyên dương" />
          </div>
          <div class="col-12 col-md-4">
            <label for="audit-from" class="form-label small fw-semibold">Từ thời điểm</label>
            <input id="audit-from" v-model="form.from" class="form-control" type="datetime-local" />
          </div>
          <div class="col-12 col-md-4">
            <label for="audit-to" class="form-label small fw-semibold">Đến thời điểm</label>
            <input id="audit-to" v-model="form.to" class="form-control" type="datetime-local" />
          </div>
          <div class="col-12 d-flex gap-2">
            <button class="btn btn-primary" type="submit">Lọc dữ liệu</button>
            <button class="btn btn-outline-secondary" type="button" @click="reset">Đặt lại</button>
          </div>
        </div>
      </fieldset>
      <div v-if="validationError" class="alert alert-danger mt-3 mb-0" role="alert">{{ validationError }}</div>
    </form>

    <section class="border rounded-3 bg-body" :aria-busy="loading">
      <div class="p-3 border-bottom d-flex flex-wrap justify-content-between gap-2">
        <h2 class="h6 mb-0">Nhật ký kiểm toán</h2>
        <span class="small text-body-secondary">{{ rows.length }} bản ghi trên trang · Tổng theo bộ lọc: {{ total == null ? '—' : total.toLocaleString('vi-VN') }}</span>
      </div>
      <div v-if="error || countError" class="alert alert-danger m-3" role="alert">
        <p v-if="error" class="mb-1">{{ error }}</p>
        <p v-if="countError" class="mb-1">{{ countError }}</p>
        <button class="btn btn-sm btn-outline-danger" :disabled="loading" @click="load(pageIndex, true)">Thử lại</button>
      </div>
      <div v-if="loading" class="text-center p-4" role="status"><BSpinner small /> Đang tải nhật ký...</div>
      <div v-else-if="!rows.length && !error" class="text-center text-body-secondary p-5">Không có nhật ký phù hợp.</div>
      <div v-else-if="rows.length" class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th scope="col">Thời gian</th>
              <th scope="col">Người thực hiện</th>
              <th scope="col">Hành động</th>
              <th scope="col">Đối tượng / ID</th>
              <th scope="col">IP</th>
              <th scope="col">Chi tiết</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in rows" :key="row.id">
              <td class="text-nowrap">{{ formatTime(row.createdAt) }}</td>
              <td>
                <div>{{ row.actorName || row.actorEmail || 'Không xác định' }}</div>
                <div v-if="row.actorName && row.actorEmail" class="small text-body-secondary">{{ row.actorEmail }}</div>
                <div class="small text-body-secondary">ID: {{ row.actorId ?? '—' }}</div>
              </td>
              <td class="text-break">{{ row.action }}</td>
              <td class="text-break">{{ row.entity }} · {{ row.entityId === 0 ? 'Tập hợp / không có ID số' : `#${row.entityId}` }}</td>
              <td>{{ row.ipAddr || '—' }}</td>
              <td><button class="btn btn-sm btn-outline-secondary text-nowrap" @click="openDetail(row)">Xem chi tiết</button></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="p-3 border-top d-flex flex-wrap gap-2 justify-content-between align-items-center">
        <button class="btn btn-sm btn-outline-secondary" :disabled="loading || !hasPrev" @click="load(0)">Trang đầu</button>
        <button class="btn btn-sm btn-outline-secondary" :disabled="loading || !hasPrev" @click="load(pageIndex - 1)">Trang trước</button>
        <span class="small text-body-secondary">Trang {{ pageIndex + 1 }}</span>
        <button class="btn btn-sm btn-outline-secondary" :disabled="loading || !hasMore" @click="load(pageIndex + 1)">Trang sau</button>
      </div>
    </section>

    <BModal v-model="showDetail" title="Chi tiết nhật ký kiểm toán" size="lg" @hidden="selected = null">
      <template v-if="selected">
        <p class="small text-body-secondary">#{{ selected.id }} · {{ formatTime(selected.createdAt) }} · {{ selected.action }} · {{ selected.entity }}</p>
        <pre class="audit-detail border rounded bg-body-tertiary p-3 mb-0">{{ selected.detail || 'Không có nội dung chi tiết.' }}</pre>
      </template>
      <template #footer><BButton variant="secondary" @click="showDetail = false">Đóng</BButton></template>
    </BModal>
  </div>
</template>

<style scoped>
.audit-detail {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  max-height: 60vh;
  overflow: auto;
}
</style>
