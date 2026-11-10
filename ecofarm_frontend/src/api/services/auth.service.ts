import axios from '@/api/clients/axios'
import type { ApiResponse } from '@/api/types/common'
import type { LoginRequest, ProfileResponse, RegisterRequest, RegisterResponse } from '@/api/types/auth'
import { API_V1 } from '@/api/constants'

export const authService = {
  login: (data: LoginRequest) =>
    axios.post<ApiResponse<void>>(`${API_V1}/auth/login`, data),

  getProfile: () =>
    axios.get<ApiResponse<ProfileResponse>>(`${API_V1}/auth/profile`),

  register: (data: RegisterRequest) =>
    axios.post<ApiResponse<RegisterResponse>>(`${API_V1}/auth/register`, data),

  logout: () =>
    axios.post<ApiResponse<void>>(`${API_V1}/auth/logout`),
}
