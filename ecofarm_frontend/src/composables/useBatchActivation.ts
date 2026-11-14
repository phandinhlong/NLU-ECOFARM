import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import type { Ref } from 'vue'
import { batchService } from '@/api/services/batch.service'
import type { ActivateBatchRequest, ExtraQrRequest, ExportQrParams } from '@/api/types/batch'

export const useAnchorStatusQuery = (batchId: Ref<number>) =>
  useQuery({
    queryKey: ['anchor-status', batchId],
    queryFn: () => batchService.getAnchorStatus(batchId.value).then((r) => r.data),
    refetchInterval: (query) => {
      const status = query.state.data?.batchStatus
      return status === 'ANCHORING' ? 3000 : false
    },
  })

export const useProofBundleQuery = (batchId: Ref<number>) =>
  useQuery({
    queryKey: ['proof-bundle', batchId],
    queryFn: () => batchService.getProofBundle(batchId.value).then((r) => r.data),
  })

export const usePublicTraceQuery = (code: Ref<string>) =>
  useQuery({
    queryKey: ['public-trace', code],
    queryFn: () => batchService.getPublicTrace(code.value).then((r) => r.data),
    enabled: () => !!code.value,
  })

export const useVerifyTraceMutation = () =>
  useMutation({
    mutationFn: (code: string) => batchService.verifyTrace(code).then((r) => r.data),
  })

export const useActivateBatchMutation = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ batchId, data }: { batchId: number; data: ActivateBatchRequest }) =>
      batchService.activateBatch(batchId, data).then((r) => r.data.data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['anchor-status', variables.batchId] })
      queryClient.invalidateQueries({ queryKey: ['proof-bundle', variables.batchId] })
    },
  })
}

export const useExportQrMutation = () => {
  return useMutation({
    mutationFn: ({ batchId, params }: { batchId: number; params?: ExportQrParams }) =>
      batchService.exportQrLabels(batchId, params).then((r) => r.data),
  })
}

export const useExtraQrMutation = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ batchId, data }: { batchId: number; data: ExtraQrRequest }) =>
      batchService.generateExtraQrCodes(batchId, data).then((r) => r.data.data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['anchor-status', variables.batchId] })
    },
  })
}
