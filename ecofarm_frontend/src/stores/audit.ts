import { defineStore } from 'pinia'
import { computed, reactive, ref } from 'vue'
import { auditService } from '@/api/services/audit.service'
import type { AuditFilters, AuditLog } from '@/api/types/audit'

export const AUDIT_LOCALE = 'vi-VN'

export const AUDIT_EMPTY_FORM = {
  actorId: '',
  action: '',
  entity: '',
  entityId: '',
  from: '',
  to: '',
}

export type AuditForm = typeof AUDIT_EMPTY_FORM

const messageOf = (err: unknown) => (err instanceof Error ? err.message : 'Vui lòng thử lại.')

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

export const useAuditListStore = defineStore('auditList', () => {
  const form = reactive<AuditForm>({ ...AUDIT_EMPTY_FORM })
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

  const hasPrev = computed(() => pageIndex.value > 0)

  const formatTime = (timestamp: number) => new Date(timestamp).toLocaleString(AUDIT_LOCALE)

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
      if (
        page.hasMore &&
        (nextCursor == null ||
          !Number.isSafeInteger(nextCursor) ||
          nextCursor <= 0 ||
          cursors.value.slice(0, target + 1).includes(nextCursor))
      ) {
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
      void load(0, true)
    } catch (err) {
      validationError.value = messageOf(err)
    }
  }

  const reset = () => {
    if (loading.value) return
    Object.assign(form, AUDIT_EMPTY_FORM)
    applyFilters()
  }

  return {
    form,
    filters,
    rows,
    total,
    cursors,
    pageIndex,
    hasMore,
    hasPrev,
    loading,
    error,
    countError,
    validationError,
    formatTime,
    load,
    applyFilters,
    reset,
  }
})
