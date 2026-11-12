import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import type { Ref } from 'vue'
import { catalogService } from '@/api/services'
import type { CatalogQuery, VarietyInput } from '@/api/types/catalog'

export const useVarietiesQuery = (params: Ref<CatalogQuery>) =>
  useQuery({
    queryKey: ['varieties', params],
    queryFn: () => catalogService.listVarieties(params.value).then((r) => r.data.data),
  })

export const useSaveVarietyMutation = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, data }: { id?: number; data: VarietyInput }) =>
      id == null
        ? catalogService.createVariety(data).then((r) => r.data.data)
        : catalogService.updateVariety(id, data).then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['varieties'] })
    },
  })
}
