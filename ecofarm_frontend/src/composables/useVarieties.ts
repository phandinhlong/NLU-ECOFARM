import { ref, type Ref } from 'vue'
import { catalogService } from '@/api/services/catalog.service'
import type { VarietyInput, VarietyQuery } from '@/api/types/catalog'
import { useCursorList } from '@/composables/useCursorList'

export const useVarietyList = (params: Ref<VarietyQuery>) =>
  useCursorList((lastIndex) => catalogService.listVarieties({ ...params.value, lastIndex }))

export const useSaveVarietyMutation = () => {
  const isSaving = ref(false)
  const saveError = ref<string | null>(null)

  const save = async ({ id, data }: { id?: number; data: VarietyInput }) => {
    isSaving.value = true
    saveError.value = null
    try {
      return id == null
        ? await catalogService.createVariety(data)
        : await catalogService.updateVariety(id, data)
    } catch (err) {
      saveError.value = err instanceof Error ? err.message : 'Đã xảy ra lỗi'
      throw err
    } finally {
      isSaving.value = false
    }
  }

  const resetSaveError = () => {
    saveError.value = null
  }

  return { save, isSaving, saveError, resetSaveError }
}
