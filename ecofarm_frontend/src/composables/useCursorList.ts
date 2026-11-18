import { ref, shallowRef } from 'vue'
import type { CursorPageResponse } from '@/api/types/catalog'

export const useCursorList = <T>(
  fetchPage: (lastIndex?: number) => Promise<CursorPageResponse<T>>,
) => {
  const items = shallowRef<T[]>([])
  const lastIndex = ref<number | null>(null)
  const hasMore = ref(false)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const load = async () => {
    loading.value = true
    error.value = null
    try {
      const page = await fetchPage(undefined)
      items.value = page.content
      lastIndex.value = page.lastIndex
      hasMore.value = page.hasMore
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Đã xảy ra lỗi'
    } finally {
      loading.value = false
    }
  }

  const loadMore = async () => {
    if (!hasMore.value || loading.value) return
    loading.value = true
    try {
      const page = await fetchPage(lastIndex.value ?? undefined)
      items.value = [...items.value, ...page.content]
      lastIndex.value = page.lastIndex
      hasMore.value = page.hasMore
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Đã xảy ra lỗi'
    } finally {
      loading.value = false
    }
  }

  return { items, lastIndex, hasMore, loading, error, load, loadMore }
}
