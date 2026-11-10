import axios from 'axios'
import type { AxiosInstance } from 'axios'
import type { ApiResponse } from '@/api/types/common'
import router from '@/router'
import {API_V1} from "@/api/constants.ts";

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
    (error) => {
      if (error.response?.status === 401) {
        const currentPath = router.currentRoute.value.fullPath
        router.push({
          name: 'login',
          query: { redirect: currentPath !== '/login' ? currentPath : undefined },
        })
      }

      const message =
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
      baseURL: `${backendURL}`,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
      withCredentials: true,
    })
  )

const instance = createClient()

export default instance
