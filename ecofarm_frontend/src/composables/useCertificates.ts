import { ref, type Ref } from 'vue'
import { catalogService } from '@/api/services/catalog.service'
import type { CertificateInput, CertificateQuery } from '@/api/types/catalog'
import { useCursorList } from '@/composables/useCursorList'

export const useCertificateList = (params: Ref<CertificateQuery>) =>
  useCursorList((lastIndex) => catalogService.listCertificates({ ...params.value, lastIndex }))

export const useSaveCertificateMutation = () => {
  const isSaving = ref(false)
  const saveError = ref<string | null>(null)

  const save = async ({ id, data }: { id?: number; data: CertificateInput }) => {
    isSaving.value = true
    saveError.value = null
    try {
      return id == null
        ? await catalogService.createCertificate(data)
        : await catalogService.updateCertificate(id, data)
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
