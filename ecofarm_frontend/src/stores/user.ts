import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/api/types/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)

  const setUser = (value: User | null) => {
    user.value = value
  }


  const clearUser = () => {
    user.value = null
  }

  return {
    user,
    setUser,
    clearUser,
  }
})
