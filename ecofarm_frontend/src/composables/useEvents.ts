import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import type { Ref } from 'vue'
import { eventService } from '@/api/services/event.service'
import type { CreateEventInput } from '@/api/types/event'

export const useEventsQuery = (batchId: Ref<number>) =>
  useQuery({
    queryKey: ['events', batchId],
    queryFn: () => eventService.listEvents(batchId.value),
    enabled: () => batchId.value > 0,
  })

export const useCreateEventMutation = (batchId: Ref<number>) => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: CreateEventInput) => eventService.createEvent(batchId.value, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events', batchId] })
      queryClient.invalidateQueries({ queryKey: ['batch', batchId] })
    },
  })
}
