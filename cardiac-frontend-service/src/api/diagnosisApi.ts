import { apiRequest } from './httpClient'
import { store } from '../app/store'

export interface DiagnosisSample {
  gender: string
  age: number
  pain_type: string
}

export interface DiagnosisStats {
  totalRecords: number
  meanAge: number
  surgeryShare: number
  sample: DiagnosisSample[]
}

export const getPublicStats = () => apiRequest<DiagnosisStats>('/api/diagnosis/stats')

export interface DiagnosisListItem {
  id: string
  gender: string
  age: number
  pain_type: string
}

export const getAllDiagnoses = () => apiRequest<DiagnosisListItem[]>('/api/diagnosis')

// Shape the backend actually returns from GET /diagnosis/{id} depends on whether
// the caller is authenticated: guests get DiagnosisPublicDetail (no `treatment`),
// logged-in users get the full Diagnosis (with `treatment`). `treatment` is
// therefore optional here by design, not an oversight.
export interface DiagnosisDetail {
  id: string
  gender: string
  age: number
  bp: number
  cholesterol: number
  diabetic: string
  smoking_status: string
  pain_type: string
  treatment?: string
}

export const getDiagnosisById = (id: string) => {
  const token = store.getState().auth.accessToken
  return apiRequest<DiagnosisDetail>(`/api/diagnosis/${id}`, { token })
}

// Registered users only. Mirrors GET /diagnosis/search on the backend, which
// requires at least one filter and returns the full Diagnosis shape (with
// `treatment`) - same shape as DiagnosisDetail above.
export interface DiagnosisSearchParams {
  gender?: 'Male' | 'Female'
  painType?: string
  ageMin?: number
  ageMax?: number
  bpMin?: number
  bpMax?: number
}

export const searchDiagnoses = (params: DiagnosisSearchParams) => {
  const token = store.getState().auth.accessToken
  const query = new URLSearchParams()

  if (params.gender) query.set('gender', params.gender)
  if (params.painType) query.set('painType', params.painType)
  if (params.ageMin !== undefined) query.set('ageMin', String(params.ageMin))
  if (params.ageMax !== undefined) query.set('ageMax', String(params.ageMax))
  if (params.bpMin !== undefined) query.set('bpMin', String(params.bpMin))
  if (params.bpMax !== undefined) query.set('bpMax', String(params.bpMax))

  return apiRequest<DiagnosisDetail[]>(`/api/diagnosis/search?${query}`, { token })
}

// Registered users only. Treatment breakdown grouped by one characteristic,
// always computed against the full dataset server-side (ignores any search
// filters) - "recomputed each request", not cached.
export type AnalysisCharacteristic = 'age' | 'gender' | 'painType'

export interface AnalysisGroup {
  value: string
  count: number
  treatmentCounts: Record<string, number>
  treatmentPercentages: Record<string, number>
  dominantTreatment: string
}

export interface AnalysisResult {
  characteristic: string
  totalRecords: number
  overallTreatmentCounts: Record<string, number>
  overallTreatmentPercentages: Record<string, number>
  breakdown: AnalysisGroup[]
}

export const getAnalysis = (by: AnalysisCharacteristic) => {
  const token = store.getState().auth.accessToken
  return apiRequest<AnalysisResult>(`/api/diagnosis/analysis?by=${by}`, { token })
}

export const bookmarkDiagnosis = (id: string) => {
  const token = store.getState().auth.accessToken
  return apiRequest<{ message: string; diagnosisId: string }>(
    `/api/diagnosis/${id}/bookmark`,
    { method: 'POST', token },
  )
}
