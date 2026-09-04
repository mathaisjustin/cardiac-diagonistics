import { useEffect, useState } from 'react'
import { AnimatePresence } from 'framer-motion'

import AuthenticatedNavbar from '../../../components/Navbar/AuthenticatedNavbar'
import { useToast } from '../../../components/Toast/ToastContext'

import RegistrySidebar from './components/RegistrySidebar'

import RecordGrid from '../components/RecordGrid'
import Pagination from '../components/Pagination'
import DiagnosisDetailPanel from '../components/DiagnosisDetailPanel'

import {
  getAllDiagnoses,
  searchDiagnoses,
  bookmarkDiagnosis,
} from '../../../api/diagnosisApi'

import type { RegistryFilters, RegistryRecord } from '../components/registryTypes'

const PAGE_SIZE = 12

const AuthenticatedRegistry = () => {
  const { showToast } = useToast()

  const [records, setRecords] = useState<RegistryRecord[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [hasSearched, setHasSearched] = useState(false)
  const [currentPage, setCurrentPage] = useState(1)

  const [selectedRecord, setSelectedRecord] = useState<{
    id: string
    positionLabel: string
  } | null>(null)

  const [saveError, setSaveError] = useState<string | null>(null)

  const loadDefaultRecords = () => {
    setLoading(true)
    setError(null)

    getAllDiagnoses()
      .then((data) =>
        setRecords(
          data.map((record) => ({
            id: record.id,
            gender: record.gender,
            age: record.age,
            diagnosis: record.pain_type,
          })),
        ),
      )
      .catch(() => setError('Unable to load registry data.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadDefaultRecords()
  }, [])

  const handleSearch = (filters: RegistryFilters) => {
    setLoading(true)
    setError(null)

    searchDiagnoses({
      gender: filters.gender === 'Any' ? undefined : filters.gender,
      painType: filters.painType ?? undefined,
      ageMin: filters.minAge,
      ageMax: filters.maxAge,
      bpMin: filters.bpMin,
      bpMax: filters.bpMax,
    })
      .then((data) =>
        setRecords(
          data.map((record) => ({
            id: record.id,
            gender: record.gender,
            age: record.age,
            diagnosis: record.pain_type,
            bloodPressure: String(record.bp),
          })),
        ),
      )
      .catch(() => setError('Search failed. Please try again.'))
      .finally(() => {
        setLoading(false)
        setHasSearched(true)
        setCurrentPage(1)
      })
  }

  const handleReset = () => {
    setHasSearched(false)
    setCurrentPage(1)
    loadDefaultRecords()
  }

  const totalPages = Math.max(1, Math.ceil(records.length / PAGE_SIZE))
  const pageStart = (currentPage - 1) * PAGE_SIZE
  const pageRecords = records.slice(pageStart, pageStart + PAGE_SIZE)

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

          {loading && <p className="mb-4 text-sm text-gray-500">Loading…</p>}

          {error && <p className="mb-4 text-sm text-[#ed3217]">{error}</p>}

          {saveError && (
            <p className="mb-4 text-sm text-[#ed3217]">{saveError}</p>
          )}

          {!loading && !error && records.length === 0 && (
            <p className="py-10 text-center text-sm text-gray-500">
              No records match these filters.
            </p>
          )}

          <RecordGrid
            records={pageRecords}
            onSelect={(record, index) =>
              setSelectedRecord({
                id: record.id,
                positionLabel: String(pageStart + index + 1).padStart(3, '0'),
              })
            }
            onBookmark={(record) => {
              setSaveError(null)
              bookmarkDiagnosis(record.id)
                .then(() => showToast('Record bookmarked'))
                .catch(() => setSaveError('Unable to save this record. Please try again.'))
            }}
          />

          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
          />
        </main>
      </div>

      <AnimatePresence>
        {selectedRecord && (
          <DiagnosisDetailPanel
            recordId={selectedRecord.id}
            positionLabel={selectedRecord.positionLabel}
            onClose={() => setSelectedRecord(null)}
            onSaveRecord={(detail) => {
              setSaveError(null)
              bookmarkDiagnosis(detail.id)
                .then(() => {
                  setSelectedRecord(null)
                  showToast('Record bookmarked')
                })
                .catch(() => setSaveError('Unable to save this record. Please try again.'))
            }}
          />
        )}
      </AnimatePresence>
    </div>
  )
}

export default AuthenticatedRegistry
