import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/api/types/auth'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<User | null>(null)

  const isAuthenticated = computed(() => currentUser.value !== null)

  const setUser = (user: User | null) => {
    currentUser.value = user
  }

  const clearUser = () => {
    currentUser.value = null
  }

  return {
    currentUser,
    isAuthenticated,
    setUser,
    clearUser,
  }
})
