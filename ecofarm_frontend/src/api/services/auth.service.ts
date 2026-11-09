import axios from '@/api/clients/axios'
import type { ApiResponse } from '@/api/types/common'
import type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '@/api/types/auth'

export const authService = {
  login: (data: LoginRequest) =>
    axios.post<ApiResponse<LoginResponse>>('/auth/login', data),

  register: (data: RegisterRequest) =>
    axios.post<ApiResponse<RegisterResponse>>('/auth/register', data),

  logout: () =>
    axios.post<ApiResponse<void>>('/auth/logout'),

  getCurrentUser: () =>
    axios.get<ApiResponse<LoginResponse>>('/auth/me'),
}
