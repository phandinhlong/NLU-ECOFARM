import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import type { Ref } from 'vue'
import { qualityService } from '@/api/services/quality.service'
import type { HarvestRequest, QcRequest } from '@/api/types/quality'

// Reading QC also re-validates stale passes on the server, so this query is the source of truth
// for whether a batch still holds a valid QC pass.
export const useQcListQuery = (batchId: Ref<number>) =>
  useQuery({
    queryKey: ['qc', batchId],
    queryFn: () => qualityService.listQc(batchId.value),
  })

export const useHarvestMutation = (batchId: Ref<number>) => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: HarvestRequest) => qualityService.harvest(batchId.value, body),
    onSuccess: () => {
      // Harvest changes batch state and adds a journal event; refresh both views.
      queryClient.invalidateQueries({ queryKey: ['qc', batchId] })
      queryClient.invalidateQueries({ queryKey: ['cultivation-events', batchId] })
      queryClient.invalidateQueries({ queryKey: ['batch', batchId] })
    },
  })
}

export const useRecordQcMutation = (batchId: Ref<number>) => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: QcRequest) => qualityService.recordQc(batchId.value, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['qc', batchId] })
      queryClient.invalidateQueries({ queryKey: ['batch', batchId] })
    },
  })
}
