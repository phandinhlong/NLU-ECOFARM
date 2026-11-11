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
  username: string
  email: string
  password: string
  fullName: string
}

export interface RegisterResponse {
  user: User
}
