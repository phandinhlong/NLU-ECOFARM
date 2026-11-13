import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import type { Ref } from 'vue'
import { catalogService } from '@/api/services/catalog.service'
import type { CatalogQuery, GreenhouseInput } from '@/api/types/catalog'

export const useGreenhousesQuery = (params: Ref<CatalogQuery>) =>
  useQuery({
    queryKey: ['greenhouses', params],
    queryFn: () => catalogService.listGreenhouses(params.value).then((r) => r.data.data),
  })

export const useSaveGreenhouseMutation = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, data }: { id?: number; data: GreenhouseInput }) =>
      id == null
        ? catalogService.createGreenhouse(data).then((r) => r.data.data)
        : catalogService.updateGreenhouse(id, data).then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['greenhouses'] })
    },
  })
}
