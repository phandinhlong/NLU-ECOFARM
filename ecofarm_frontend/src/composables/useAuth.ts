import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { authService } from '@/api/services'
import { useUserStore } from '@/stores'
import { useRoute, useRouter } from 'vue-router'
import type { LoginRequest, ProfileResponse, RegisterRequest } from '@/api/types/auth'

export const useLoginMutation = () => {
  const router = useRouter()
  const route = useRoute()
  const userStore = useUserStore()

  return useMutation<ProfileResponse, Error, LoginRequest>({
    mutationFn: async (data: LoginRequest) => {
      await authService.login(data)
      const profile = await authService.getProfile()
      userStore.setUser(profile.data.data)
      return profile.data.data
    },
    onSuccess: () => {
      const redirect = route.query.redirect
      router.push(typeof redirect === 'string' ? redirect : '/dashboard')
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
