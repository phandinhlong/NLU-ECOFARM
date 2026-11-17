import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { batchService } from '@/api/services/batch.service'
import type { Batch, BatchListParams, WorkerSummary } from '@/api/types/batch'

export const useBatchListStore = defineStore('batchList', () => {
  const params = ref<BatchListParams>({})
  const items = ref<Batch[]>([])
  const cursors = ref<(number | null)[]>([null])
  const pageIndex = ref(0)
  const hasMore = ref(false)
  const loading = ref(false)
  const error = ref<unknown>(null)

  const hasPrev = computed(() => pageIndex.value > 0)

  const reset = () => {
    items.value = []
    cursors.value = [null]
    pageIndex.value = 0
    hasMore.value = false
    error.value = null
  }

  const setFilters = (next: BatchListParams) => {
    params.value = next
    reset()
  }

  const load = async () => {
    if (loading.value) return
    reset()
    loading.value = true
    try {
      const page = await batchService.listBatches({ ...params.value })
      items.value = page.content
      hasMore.value = page.hasMore
      cursors.value = page.hasMore ? [null, page.lastIndex] : [null]
    } catch (err) {
      error.value = err
    } finally {
      loading.value = false
    }
  }

  const next = async () => {
    if (!hasMore.value || loading.value) return
    const target = pageIndex.value + 1
    loading.value = true
    try {
      const page = await batchService.listBatches({
        ...params.value,
        lastIndex: cursors.value[target] ?? undefined,
      })
      items.value = page.content
      hasMore.value = page.hasMore
      cursors.value = [...cursors.value.slice(0, target + 1)]
      if (page.hasMore) cursors.value = [...cursors.value, page.lastIndex]
      pageIndex.value = target
    } catch (err) {
      error.value = err
    } finally {
      loading.value = false
    }
  }

  const prev = async () => {
    if (!hasPrev.value || loading.value) return
    const target = pageIndex.value - 1
    loading.value = true
    try {
      const page = await batchService.listBatches({
        ...params.value,
        lastIndex: cursors.value[target] ?? undefined,
      })
      items.value = page.content
      hasMore.value = page.hasMore
      pageIndex.value = target
    } catch (err) {
      error.value = err
    } finally {
      loading.value = false
    }
  }

  const first = async () => {
    if (pageIndex.value === 0 || loading.value) return
    loading.value = true
    try {
      const page = await batchService.listBatches({ ...params.value })
      items.value = page.content
      hasMore.value = page.hasMore
      cursors.value = page.hasMore ? [null, page.lastIndex] : [null]
      pageIndex.value = 0
    } catch (err) {
      error.value = err
    } finally {
      loading.value = false
    }
  }

  return { params, items, cursors, pageIndex, hasMore, hasPrev, loading, error, setFilters, reset, load, next, prev, first }
})

export const useWorkerLookupStore = defineStore('workerLookup', () => {
  const workers = ref<WorkerSummary[]>([])
  const lastIndex = ref<number | null>(null)
  const hasMore = ref(false)
  const loading = ref(false)
  const loaded = ref(false)

  const load = async () => {
    if (loaded.value) return
    loading.value = true
    try {
      const page = await batchService.listWorkers()
      workers.value = page.content
      lastIndex.value = page.lastIndex
      hasMore.value = page.hasMore
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  const loadMore = async () => {
    if (!hasMore.value || loading.value) return
    loading.value = true
    try {
      const page = await batchService.listWorkers({ lastIndex: lastIndex.value ?? undefined })
      workers.value = [...workers.value, ...page.content]
      lastIndex.value = page.lastIndex
      hasMore.value = page.hasMore
    } finally {
      loading.value = false
    }
  }

  return { workers, lastIndex, hasMore, loading, loaded, load, loadMore }
})

export const useAssignedWorkersStore = defineStore('assignedWorkers', () => {
  const batchId = ref<number | null>(null)
  const workers = ref<WorkerSummary[]>([])
  const lastIndex = ref<number | null>(null)
  const hasMore = ref(false)
  const loading = ref(false)

  const load = async (id: number) => {
    batchId.value = id
    workers.value = []
    lastIndex.value = null
    hasMore.value = false
    loading.value = true
    try {
      const page = await batchService.listAssignedWorkers(id)
      workers.value = page.content
      lastIndex.value = page.lastIndex
      hasMore.value = page.hasMore
    } finally {
      loading.value = false
    }
  }

  const loadMore = async () => {
    if (!hasMore.value || loading.value || batchId.value == null) return
    loading.value = true
    try {
      const page = await batchService.listAssignedWorkers(batchId.value, {
        lastIndex: lastIndex.value ?? undefined,
      })
      workers.value = [...workers.value, ...page.content]
      lastIndex.value = page.lastIndex
      hasMore.value = page.hasMore
    } finally {
      loading.value = false
    }
  }

  const refresh = async () => {
    if (batchId.value != null) await load(batchId.value)
  }

  const add = async (userIds: number[]) => {
    if (batchId.value == null) return
    await batchService.addWorkers(batchId.value, userIds)
    await refresh()
  }

  const remove = async (userId: number) => {
    if (batchId.value == null) return
    await batchService.removeWorkers(batchId.value, [userId])
    await refresh()
  }

  return { batchId, workers, lastIndex, hasMore, loading, load, loadMore, refresh, add, remove }
})

export const useBatchFormStore = defineStore('batchForm', () => {
  const greenhouseId = ref<number | ''>('')
  const varietyId = ref<number | ''>('')
  const sowDate = ref('')

  const reset = () => {
    greenhouseId.value = ''
    varietyId.value = ''
    sowDate.value = ''
  }

  return { greenhouseId, varietyId, sowDate, reset }
})
