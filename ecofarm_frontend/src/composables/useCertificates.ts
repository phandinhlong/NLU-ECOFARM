import { ref, type Ref } from 'vue'
import { catalogService } from '@/api/services/catalog.service'
import type { CertificateInput, CertificateQuery } from '@/api/types/catalog'
import { useCursorList } from '@/composables/useCursorList'

export const useCertificateList = (params: Ref<CertificateQuery>) =>
  useCursorList((lastIndex) => catalogService.listCertificates({ ...params.value, lastIndex }))

export const useCreateCertificateMutation = () => {
  const isSaving = ref(false)
  const saveError = ref<string | null>(null)

  const create = async (data: CertificateInput) => {
    isSaving.value = true
    saveError.value = null
    try {
      return await catalogService.createCertificate(data)
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

  return { create, isSaving, saveError, resetSaveError }
}
