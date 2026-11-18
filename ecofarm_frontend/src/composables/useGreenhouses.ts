import { ref, type Ref } from 'vue'
import { catalogService } from '@/api/services/catalog.service'
import type { GreenhouseInput, GreenhouseQuery } from '@/api/types/catalog'
import { useCursorList } from '@/composables/useCursorList'

export const useGreenhouseList = (params: Ref<GreenhouseQuery>) =>
  useCursorList((lastIndex) => catalogService.listGreenhouses({ ...params.value, lastIndex }))

export const useSaveGreenhouseMutation = () => {
  const isSaving = ref(false)
  const saveError = ref<string | null>(null)

  const save = async ({ id, data }: { id?: number; data: GreenhouseInput }) => {
    isSaving.value = true
    saveError.value = null
    try {
      return id == null
        ? await catalogService.createGreenhouse(data)
        : await catalogService.updateGreenhouse(id, data)
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

export const useDeleteGreenhouseMutation = () => {
  const isDeleting = ref(false)
  const deleteError = ref<string | null>(null)

  const remove = async (id: number) => {
    isDeleting.value = true
    deleteError.value = null
    try {
      return await catalogService.deleteGreenhouse(id)
    } catch (err) {
      deleteError.value = err instanceof Error ? err.message : 'Đã xảy ra lỗi'
      throw err
    } finally {
      isDeleting.value = false
    }
  }

  const resetDeleteError = () => {
    deleteError.value = null
  }

  return { remove, isDeleting, deleteError, resetDeleteError }
}
