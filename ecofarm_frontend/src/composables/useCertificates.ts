import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import type { Ref } from 'vue'
import { catalogService } from '@/api/services'
import type { CertificateInput, CertificateQuery } from '@/api/types/catalog'

export const useCertificatesQuery = (params: Ref<CertificateQuery>) =>
  useQuery({
    queryKey: ['certificates', params],
    queryFn: () => catalogService.listCertificates(params.value).then((r) => r.data.data),
  })

export const useCreateCertificateMutation = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: CertificateInput) =>
      catalogService.createCertificate(data).then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['certificates'] })
    },
  })
}
