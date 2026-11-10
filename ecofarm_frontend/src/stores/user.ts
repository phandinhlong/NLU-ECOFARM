import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/api/types/auth'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<User | null>(null)
  const accessToken = ref<string | null>(localStorage.getItem('accessToken'))

  const isAuthenticated = computed(() => currentUser.value !== null || accessToken.value !== null)

  const setUser = (user: User | null) => {
    currentUser.value = user
  }

  const setToken = (token: string | null) => {
    accessToken.value = token
    if (token) {
      localStorage.setItem('accessToken', token)
    } else {
      localStorage.removeItem('accessToken')
    }
  }

  const clearUser = () => {
    currentUser.value = null
    setToken(null)
  }

  return {
    currentUser,
    accessToken,
    isAuthenticated,
    setUser,
    setToken,
    clearUser,
  }
})
