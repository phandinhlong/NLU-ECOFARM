import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import type { Ref } from 'vue'
import { shipmentService } from '@/api/services/shipment.service'
import type { ShipmentCreateInput, ShipmentReceiveInput } from '@/api/types/shipment'

export const useEligibleBatchesQuery = (enabled: () => boolean = () => true) =>
  useQuery({
    queryKey: ['shipments', 'eligible-batches'],
    queryFn: () => shipmentService.listEligibleBatches(),
    enabled,
  })

export const useShipmentsByStatusQuery = (status: Ref<string>) =>
  useQuery({
    queryKey: ['shipments', { status }],
    queryFn: () => shipmentService.listShipments(status.value ? { status: status.value } : undefined),
  })

export const useShipmentsByBatchQuery = (batchId: Ref<number | null>) =>
  useQuery({
    queryKey: ['shipments', 'batch', batchId],
    queryFn: () => shipmentService.listShipmentsByBatch(batchId.value as number),
    enabled: () => batchId.value != null,
  })

export const useCreateShipmentMutation = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ batchId, input }: { batchId: number; input: ShipmentCreateInput }) =>
      shipmentService.createShipment(batchId, input),
    onSuccess: (_data, { batchId }) => {
      queryClient.invalidateQueries({ queryKey: ['shipments'] })
      queryClient.invalidateQueries({ queryKey: ['batches'] })
      queryClient.invalidateQueries({ queryKey: ['batch', batchId] })
    },
  })
}

export const useReceiveShipmentMutation = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ shipmentId, input }: { shipmentId: number; input: ShipmentReceiveInput }) =>
      shipmentService.receiveShipment(shipmentId, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['shipments'] })
    },
  })
}
