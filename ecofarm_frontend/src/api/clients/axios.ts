import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/api/types/common'
import { API_V1 } from '@/api/constants'
import router from '@/router'

type RetryConfig = InternalAxiosRequestConfig & { _retry?: boolean }

let refreshPromise: Promise<unknown> | null = null

const isAuthUrl = (url?: string) =>
  url?.includes('/auths/login') || url?.includes('/auths/refresh-token')

const redirectToLogin = () => {
  const currentPath = router.currentRoute.value.fullPath
  router.push({
    name: 'login',
    query: { redirect: currentPath !== '/login' ? currentPath : undefined },
  })
}

const applyInterceptors = (instance: AxiosInstance) => {
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

      if (data.isSuccess === false) {
        return Promise.reject(new Error(data.data || 'Request failed'))
      }

      return response
    },
    async (error) => {
      const config = error.config as RetryConfig | undefined
      const isRefreshable =
        error.response?.status === 401 && config && !config._retry && !isAuthUrl(config.url)

      if (isRefreshable) {
        config._retry = true

        try {
          refreshPromise ??= instance
            .post(`${API_V1}/auths/refresh-token`)
            .finally(() => {
              refreshPromise = null
            })
          await refreshPromise
          return instance(config)
        } catch {
          redirectToLogin()
        }
      } else if (error.response?.status === 401 && !isAuthUrl(config?.url)) {
        redirectToLogin()
      }

      const message =
        error.response?.data?.message ||
        error.response?.data ||
        error.message ||
        'An unexpected error occurred'

      return Promise.reject(new Error(message))
    }
  )

  return instance
}

const backendURL = import.meta.env.VITE_BACKEND;

export const createClient = () =>
  applyInterceptors(
    axios.create({
      // Ở dev, request đi qua proxy của Vite (same-origin) nên dùng URL tương đối,
      // không phụ thuộc port dev server và không vướng CORS. Bản build production
      // vẫn gọi thẳng backend qua VITE_BACKEND.
      baseURL: import.meta.env.DEV ? '' : `${backendURL}`,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
      withCredentials: true,
    })
  )

const instance = createClient()

export default instance
