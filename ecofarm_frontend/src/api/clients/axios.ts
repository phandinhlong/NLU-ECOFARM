import axios from 'axios'
import type { ApiResponse } from '@/api/types/common'
import router from '@/router'

const instance = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
})

instance.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  (response) => {
    const data = response.data as ApiResponse

    if (data.success === false) {
      return Promise.reject(new Error(data.message || 'Request failed'))
    }

    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      const currentPath = router.currentRoute.value.fullPath
      router.push({
        name: 'login',
        query: { redirect: currentPath !== '/login' ? currentPath : undefined },
      })
    }

    const message =
      error.response?.data?.message ||
      error.message ||
      'An unexpected error occurred'

    return Promise.reject(new Error(message))
  }
)

export default instance
