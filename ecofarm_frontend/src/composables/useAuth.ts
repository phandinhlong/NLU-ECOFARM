import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { authService } from '@/api/services'
import { useUserStore } from '@/stores'
import { useRouter } from 'vue-router'
import type { LoginRequest, RegisterRequest } from '@/api/types/auth'

export const useLoginMutation = () => {
  const userStore = useUserStore()
  const router = useRouter()

  return useMutation({
    mutationFn: (data: LoginRequest) => authService.login(data),
    onSuccess: (response) => {
      userStore.setUser(response.data.data.user)
      router.push('/dashboard')
    },
  })
}

export const useRegisterMutation = () => {
  const router = useRouter()

  return useMutation({
    mutationFn: (data: RegisterRequest) => authService.register(data),
    onSuccess: () => {
      router.push('/login')
    },
  })
}

export const useLogoutMutation = () => {
  const userStore = useUserStore()
  const router = useRouter()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => authService.logout(),
    onSuccess: () => {
      userStore.clearUser()
      queryClient.clear()
      router.push('/login')
    },
  })
}

export const useCurrentUser = () => {
  const userStore = useUserStore()

  return useQuery({
    queryKey: ['currentUser'],
    queryFn: async () => {
      const response = await authService.getCurrentUser()
      return response.data.data.user
    },
    staleTime: Infinity,
    enabled: !userStore.isAuthenticated,
  })
}
