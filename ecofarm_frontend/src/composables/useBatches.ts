import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import type { Ref } from 'vue'
import { batchService } from '@/api/services/batch.service'
import type { BatchCreateInput, BatchUpdateInput } from '@/api/types/batch'

export const useBatchQuery = (id: Ref<number>) =>
  useQuery({
    queryKey: ['batch', id],
    queryFn: () => batchService.getBatch(id.value),
  })

export const useMyBatchesQuery = () =>
  useQuery({
    queryKey: ['batches', 'mine'],
    queryFn: () => batchService.listBatches({ mine: true }),
    staleTime: 0,
    refetchOnMount: 'always',
  })

export const useBatchLookupQuery = () =>
  useQuery({
    queryKey: ['batch-lookups'],
    queryFn: async () => {
      const [greenhouses, varieties] = await Promise.all([
        batchService.listGreenhouses(),
        batchService.listVarieties(),
      ])
      return { greenhouses, varieties }
    },
    staleTime: Infinity,
  })

export const useCreateBatchMutation = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (input: BatchCreateInput) => batchService.createBatch(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['batches'] })
    },
  })
}

export const useUpdateBatchMutation = (id: Ref<number>) => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: BatchUpdateInput) => batchService.updateBatch(id.value, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['batch', id] })
      queryClient.invalidateQueries({ queryKey: ['batches'] })
    },
  })
}

export const useDeleteBatchMutation = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => batchService.deleteBatch(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['batches'] }),
  })
}
