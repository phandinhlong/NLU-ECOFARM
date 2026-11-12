export interface LoginRequest {
  email: string
  password: string
}

export interface ProfileResponse {
  id: number
  email: string
  fullName: string
  role: string[]
}

export type User = ProfileResponse

export interface RegisterRequest {
  fullName: string
  email: string
  password: string
  confirmPassword: string
}
