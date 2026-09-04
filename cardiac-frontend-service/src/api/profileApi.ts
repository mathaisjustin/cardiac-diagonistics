import { apiRequest } from './httpClient'
import { store } from '../app/store'

export interface Profile {
  email: string
  firstName: string
  lastName: string
  contact: string
  department: string
  createdAt: string
}

export interface UpdateProfileRequest {
  firstName: string
  lastName: string
  contact: string
  department: string
}

export const getProfile = () => {
  const token = store.getState().auth.accessToken
  return apiRequest<Profile>('/api/profile', { token })
}

export const updateProfile = (payload: UpdateProfileRequest) => {
  const token = store.getState().auth.accessToken
  return apiRequest<Profile>('/api/profile', { method: 'PUT', body: payload, token })
}
