import { useInfiniteQuery } from '@tanstack/vue-query'
import type { Ref } from 'vue'
import { userService } from '@/api/services/user.service'

export const useEngineerSearchQuery = (params: Ref<{ keyword?: string }>) =>
  useInfiniteQuery({
    queryKey: ['users', 'engineers', params],
    queryFn: ({ pageParam }) =>
      userService.findUsers({
        role: 'ENGINEER',
        keyword: params.value.keyword || undefined,
        lastIndex: pageParam ?? undefined,
      }),
    initialPageParam: null as number | null,
    getNextPageParam: (last) => (last.hasMore ? last.lastIndex : null),
  })
