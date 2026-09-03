import { useState } from 'react'

import AuthenticatedNavbar from '../../../components/Navbar/AuthenticatedNavbar'

import RegistrySidebar from './components/RegistrySidebar'

import RecordGrid from '../components/RecordGrid'
import LoadMoreButton from '../components/LoadMoreButton'

import { mockRecords } from '../components/mockRecords'

import type {
  RegistryFilters,
  RegistryRecord,
} from '../components/registryTypes'

const AuthenticatedRegistry = () => {
  const [records, setRecords] =
    useState<RegistryRecord[]>(mockRecords)

  const [hasSearched, setHasSearched] = useState(false)

  const handleSearch = (filters: RegistryFilters) => {
    const filteredRecords = mockRecords.filter((record) => {
      // Pain type
      const matchesPainType =
        filters.painTypes.length === 0 ||
        filters.painTypes.includes(record.diagnosis)

      // Age
      const matchesAge =
        record.age >= filters.minAge &&
        record.age <= filters.maxAge

      // Gender
      const matchesGender =
        filters.gender === 'Any' ||
        record.gender === filters.gender

      // Blood pressure
      const matchesBloodPressure =
        filters.bloodPressure === '' ||
        record.bloodPressure
          .toLowerCase()
          .includes(filters.bloodPressure.toLowerCase())

      return (
        matchesPainType &&
        matchesAge &&
        matchesGender &&
        matchesBloodPressure
      )
    })

    setRecords(filteredRecords)
    setHasSearched(true)
  }

  const handleReset = () => {
    setRecords(mockRecords)
    setHasSearched(false)
  }

  return (
    <div className="min-h-screen bg-[#f5f3f3]">
      <AuthenticatedNavbar />

      <div className="flex min-h-[calc(100vh-88px)]">
        <RegistrySidebar
          onSearch={handleSearch}
          onReset={handleReset}
        />

        <main className="min-w-0 flex-1 px-8 py-7">
          <div className="mb-5">
            {hasSearched ? (
              <h1 className="text-2xl font-extrabold text-gray-900">
                {records.length} results
              </h1>
            ) : (
              <h1 className="text-2xl font-extrabold text-gray-900">
                Cardiac Diagnosis Registry
              </h1>
            )}
          </div>

          <RecordGrid records={records} />

          <LoadMoreButton />
        </main>
      </div>
    </div>
  )
}

export default AuthenticatedRegistry