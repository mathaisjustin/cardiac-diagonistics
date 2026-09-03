export interface RegistryRecord {
  id: number
  gender: 'Male' | 'Female'
  age: number
  diagnosis:
    | 'Typical Angina'
    | 'Atypical Angina'
    | 'Non-anginal'
    | 'Asymptomatic'
  bloodPressure: string
}

export interface RegistryFilters {
  painTypes: RegistryRecord['diagnosis'][]
  minAge: number
  maxAge: number
  bloodPressure: string
  gender: 'Male' | 'Female' | 'Any'
}