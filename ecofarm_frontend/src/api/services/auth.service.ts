import axios from '@/api/clients/axios'
import type { ApiResponse } from '@/api/types/common'
import type { ChangePasswordRequest, ForgotPasswordRequest, LoginRequest, ProfileResponse, RegisterRequest } from '@/api/types/auth'
import { API_V1 } from '@/api/constants'

export const authService = {
  login: (data: LoginRequest) =>
    axios.post<ApiResponse<void>>(`${API_V1}/auths/login`, data),

  getProfile: () =>
    axios.get<ApiResponse<ProfileResponse>>(`${API_V1}/auths/profile`),

  register: (data: RegisterRequest) =>
    axios.post<ApiResponse<void>>(`${API_V1}/auths/sign-up`, data),

  logout: () =>
    axios.post<ApiResponse<void>>(`${API_V1}/auths/logout`),

  forgotPassword: (data: ForgotPasswordRequest) =>
    axios.post<ApiResponse<void>>(`${API_V1}/auths/forgot-password`, data),

  changePassword: (data: ChangePasswordRequest) =>
    new Promise<ApiResponse<void>>((resolve) =>
      setTimeout(() => resolve({ isSuccess: true, data: undefined as unknown as void }), 600)
    ),
}
