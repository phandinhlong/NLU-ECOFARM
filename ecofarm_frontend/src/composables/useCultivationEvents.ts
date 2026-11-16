import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import type { Ref } from 'vue'
import { logService } from '@/api/services/log.service'
import type {
  AmendRequest,
  CreateEventRequest,
  EventListParams,
  RejectRequest,
} from '@/api/types/log'

export const useCultivationEventsQuery = (batchId: Ref<number>, params: Ref<EventListParams>) =>
  useQuery({
    queryKey: ['cultivation-events', batchId, params],
    queryFn: () => logService.listEvents(batchId.value, params.value),
  })

export const useCreateEventMutation = (batchId: Ref<number>) => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: CreateEventRequest) => logService.createEvent(batchId.value, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cultivation-events', batchId] })
    },
  })
}

export const useUploadAttachmentsMutation = (batchId: Ref<number>) =>
  useMutation({
    mutationFn: (files: File[]) => logService.uploadAttachments(batchId.value, files),
  })

export const useApproveEventMutation = (batchId: Ref<number>) => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (eventId: number) => logService.approveEvent(eventId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cultivation-events', batchId] })
    },
  })
}

export const useRejectEventMutation = (batchId: Ref<number>) => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ eventId, body }: { eventId: number; body: RejectRequest }) =>
      logService.rejectEvent(eventId, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cultivation-events', batchId] })
    },
  })
}

export const useAmendEventMutation = (batchId: Ref<number>) => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ eventId, body }: { eventId: number; body: AmendRequest }) =>
      logService.amendEvent(eventId, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cultivation-events', batchId] })
    },
  })
}
