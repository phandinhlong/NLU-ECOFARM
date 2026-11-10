import axios from '@/api/clients/axios'
import type { ApiResponse } from '@/api/types/common'
import type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '@/api/types/auth'
import { API_V1 } from '@/api/constants'

export const authService = {
  login: (data: LoginRequest) =>
    axios.post<ApiResponse<LoginResponse>>(`${API_V1}/auth/login`, data),

  register: (data: RegisterRequest) =>
    axios.post<ApiResponse<RegisterResponse>>(`${API_V1}/auth/register`, data),

  logout: () =>
    axios.post<ApiResponse<void>>(`${API_V1}/auth/logout`),

  getCurrentUser: () =>
    axios.get<ApiResponse<LoginResponse>>(`${API_V1}/auth/me`),
}
