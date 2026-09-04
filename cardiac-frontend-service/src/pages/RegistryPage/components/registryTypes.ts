export interface RegistryRecord {
  id: string
  gender: string
  age: number
  diagnosis: string
  bloodPressure?: string
}

export interface RegistryFilters {
  painType: string | null
  minAge: number
  maxAge: number
  bpMin: number
  bpMax: number
  gender: 'Male' | 'Female' | 'Any'
}
