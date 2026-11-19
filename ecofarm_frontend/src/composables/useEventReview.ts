import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import type { Ref } from 'vue'
import { eventService } from '@/api/services/event.service'
import type { AmendEventInput, EventReviewStatus, ReassignApproverInput, RejectEventInput } from '@/api/types/event'

export interface ReviewQueueParams {
  greenhouseId?: number | null
  reviewStatus?: EventReviewStatus | null
}

export const useReviewQueueQuery = (params: Ref<ReviewQueueParams>) =>
  useInfiniteQuery({
    queryKey: ['event-review', params],
    queryFn: ({ pageParam }) =>
      eventService.findReviewQueue({
        lastIndex: pageParam ?? undefined,
        greenhouseId: params.value.greenhouseId ?? undefined,
        reviewStatus: params.value.reviewStatus ?? undefined,
      }),
    initialPageParam: null as number | null,
    getNextPageParam: (last) => (last.hasMore ? last.lastIndex : null),
  })

export const useReviewCountQuery = (params: Ref<ReviewQueueParams>) =>
  useQuery({
    queryKey: ['event-review', 'count', params],
    queryFn: () =>
      eventService.countReviewQueue({
        greenhouseId: params.value.greenhouseId ?? undefined,
        reviewStatus: params.value.reviewStatus ?? undefined,
      }),
  })

export const useEventQuery = (id: Ref<number | null>) =>
  useQuery({
    queryKey: ['event-review', 'event', id],
    queryFn: () => eventService.findEvent(id.value as number),
    enabled: () => !!id.value,
  })

const invalidate = (queryClient: ReturnType<typeof useQueryClient>) => {
  queryClient.invalidateQueries({ queryKey: ['events'] })
  queryClient.invalidateQueries({ queryKey: ['event-review'] })
  queryClient.invalidateQueries({ queryKey: ['batch'] })
}

export const useApproveEventMutation = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => eventService.approve(id),
    onSuccess: () => invalidate(queryClient),
  })
}

export const useRejectEventMutation = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, input }: { id: number; input: RejectEventInput }) =>
      eventService.reject(id, input),
    onSuccess: () => invalidate(queryClient),
  })
}

export const useReassignApproverMutation = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, input }: { id: number; input: ReassignApproverInput }) =>
      eventService.reassignApprover(id, input),
    onSuccess: () => invalidate(queryClient),
  })
}

export const useAmendEventMutation = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, input }: { id: number; input: AmendEventInput }) =>
      eventService.amend(id, input),
    onSuccess: () => invalidate(queryClient),
  })
}
