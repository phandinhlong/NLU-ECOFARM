import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import type { Ref } from 'vue'
import { batchService } from '@/api/services/batch.service'
import type { BatchCreateInput, BatchListParams } from '@/api/types/batch'

export const useBatchesQuery = (params: Ref<BatchListParams>) =>
  useQuery({
    queryKey: ['batches', params],
    queryFn: () => batchService.listBatches(params.value),
  })

export const useBatchQuery = (id: Ref<number>) =>
  useQuery({
    queryKey: ['batch', id],
    queryFn: () => batchService.getBatch(id.value),
  })

export const useBatchLookupQuery = () =>
  useQuery({
    queryKey: ['batch-lookups'],
    queryFn: async () => {
      const [greenhouses, varieties, workers] = await Promise.all([
        batchService.listGreenhouses(),
        batchService.listVarieties(),
        batchService.listWorkers(),
      ])
      return { greenhouses, varieties, workers }
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

export const useAssignWorkersMutation = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ batchId, userIds }: { batchId: number; userIds: number[] }) =>
      batchService.assignWorkers(batchId, userIds),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['batch'] })
      queryClient.invalidateQueries({ queryKey: ['batches'] })
    },
  })
}
