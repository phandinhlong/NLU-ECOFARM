import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue } from 'vue'
import { qcService } from '@/api/services/qc.service'
import type { HarvestRequest, RecordQcRequest, ReviewQcRequest } from '@/api/types/qc'

export const useLatestQcQuery = (batchId: MaybeRefOrGetter<number | undefined | null>) =>
  useQuery({
    queryKey: ['batches', batchId, 'qc'],
    queryFn: () => {
      const id = toValue(batchId)
      if (!id) return null
      return qcService.getLatestQc(id)
    },
    enabled: () => !!toValue(batchId),
  })

export const useRecordQcMutation = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ batchId, data }: { batchId: number; data: RecordQcRequest }) =>
      qcService.recordQc(batchId, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['batches', variables.batchId, 'qc'] })
      queryClient.invalidateQueries({ queryKey: ['batch', variables.batchId] })
      queryClient.invalidateQueries({ queryKey: ['batches'] })
    },
  })
}

export const useReviewQcMutation = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ batchId, data }: { batchId: number; data: ReviewQcRequest }) =>
      qcService.reviewQc(batchId, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['batches', variables.batchId, 'qc'] })
      queryClient.invalidateQueries({ queryKey: ['batch', variables.batchId] })
      queryClient.invalidateQueries({ queryKey: ['batches'] })
    },
  })
}

export const useHarvestMutation = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ batchId, data }: { batchId: number; data: HarvestRequest }) =>
      qcService.harvestBatch(batchId, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['batch', variables.batchId] })
      queryClient.invalidateQueries({ queryKey: ['batches'] })
    },
  })
}
