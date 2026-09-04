import { apiRequest } from './httpClient'
import { store } from '../app/store'

// Mirrors bookmark-service's BookmarkResponseDto - a frozen snapshot taken at
// save time (gender/age/bp/painType/treatment), not a live join back to the
// diagnosis record.
export interface Bookmark {
  id: string
  diagnosisId: string
  gender: string
  age: number
  bp: string
  painType: string
  treatment: string
  createdAt: string
}

export const getBookmarks = () => {
  const token = store.getState().auth.accessToken
  return apiRequest<Bookmark[]>('/api/bookmarks', { token })
}

export const deleteBookmark = (id: string) => {
  const token = store.getState().auth.accessToken
  return apiRequest<void>(`/api/bookmarks/${id}`, { method: 'DELETE', token })
}
