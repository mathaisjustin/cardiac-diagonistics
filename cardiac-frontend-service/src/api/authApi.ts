import { apiRequest } from './httpClient'
import { store } from '../app/store'

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName: string
  contactNumber: string
  department: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
}

export const login = (payload: LoginRequest) =>
  apiRequest<AuthResponse>('/api/auth/login', { method: 'POST', body: payload })

export const register = (payload: RegisterRequest) =>
  apiRequest<void>('/api/auth/register', { method: 'POST', body: payload })

export const refresh = (refreshToken: string) =>
  apiRequest<AuthResponse>('/api/auth/refresh', {
    method: 'POST',
    body: { refreshToken },
  })

export const logout = (refreshToken: string) =>
  apiRequest<void>('/api/auth/logout', {
    method: 'POST',
    body: { refreshToken },
  })

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

export const changePassword = (payload: ChangePasswordRequest) => {
  const token = store.getState().auth.accessToken
  return apiRequest<{ message: string }>('/api/auth/change-password', {
    method: 'POST',
    body: payload,
    token,
  })
}
