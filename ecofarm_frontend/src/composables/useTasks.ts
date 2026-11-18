import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import type { Ref } from 'vue'
import { taskService } from '@/api/services/task.service'
import type { CompleteTaskInput, CreateTaskInput } from '@/api/types/task'

export const useTasksQuery = (batchId: Ref<number>) =>
  useQuery({
    queryKey: ['tasks', batchId],
    queryFn: () => taskService.listTasks(batchId.value),
    enabled: () => batchId.value > 0,
  })

export const useCreateTaskMutation = (batchId: Ref<number>) => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: CreateTaskInput) => taskService.createTask(batchId.value, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', batchId] })
      queryClient.invalidateQueries({ queryKey: ['batch', batchId] })
    },
  })
}

export const useCompleteTaskMutation = (batchId: Ref<number>) => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: { taskId: number; input: CompleteTaskInput }) =>
      taskService.completeTask(payload.taskId, payload.input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', batchId] })
      queryClient.invalidateQueries({ queryKey: ['batch', batchId] })
    },
  })
}

export const useCancelTaskMutation = (batchId: Ref<number>) => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: { taskId: number; reason: string }) =>
      taskService.cancelTask(payload.taskId, payload.reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', batchId] })
    },
  })
}
