export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  user: User
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  fullName: string
}

export interface RegisterResponse {
  user: User
}

export interface User {
  id: string
  username: string
  email: string
  fullName: string
  role: string
  createdAt: string
}
