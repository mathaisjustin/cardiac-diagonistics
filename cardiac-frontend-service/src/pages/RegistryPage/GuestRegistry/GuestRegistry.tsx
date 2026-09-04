import { useEffect, useMemo, useState } from 'react'
import { AnimatePresence } from 'framer-motion'
import { useLocation, useNavigate } from 'react-router-dom'

import GuestRegistryNavbar from '../../../components/Navbar/GuestRegistryNavbar'

import GuestRegistryToolbar from './components/GuestRegistryToolbar'
import { AGE_BOUND_MIN, AGE_BOUND_MAX } from './components/AgeFilter'
import AccountPrompt from './components/AccountPrompt'
import SignInPromptModal from './components/SignInPromptModal'

import RecordGrid from '../components/RecordGrid'
import Pagination from '../components/Pagination'
import DiagnosisDetailPanel from '../components/DiagnosisDetailPanel'

import { getAllDiagnoses, type DiagnosisListItem } from '../../../api/diagnosisApi'

type Gender = 'Male' | 'Female' | 'Any'

interface AuthPrompt {
  heading?: string
  body: React.ReactNode
}

const PAGE_SIZE = 12

// Friendly names for the protected pages RequireAuth can bounce a guest
// back from - keyed by the pathname it hands over in router state.
const PROTECTED_PAGE_LABELS: Record<string, string> = {
  '/analysis': 'Analysis',
  '/bookmarks': 'Bookmarks',
  '/profile': 'Your profile',
}

const GuestRegistry = () => {
  const [records, setRecords] = useState<DiagnosisListItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [search, setSearch] = useState('')
  const [painTypes, setPainTypes] = useState<string[]>([])
  const [gender, setGender] = useState<Gender>('Any')
  const [minAge, setMinAge] = useState(AGE_BOUND_MIN)
  const [maxAge, setMaxAge] = useState(AGE_BOUND_MAX)

  const [currentPage, setCurrentPage] = useState(1)
  const [authPrompt, setAuthPrompt] = useState<AuthPrompt | null>(null)

  const [selectedRecord, setSelectedRecord] = useState<{
    id: string
    positionLabel: string
  } | null>(null)

  const location = useLocation()
  const navigate = useNavigate()

  // A protected route (Analysis/Bookmarks/Profile) bounced a logged-out
  // visitor here - open the same sign-in prompt, naming what they wanted.
  // Then strip the state so a refresh/back-nav doesn't reopen it forever.
  useEffect(() => {
    const state = location.state as { authRequiredFrom?: string } | null
    const from = state?.authRequiredFrom

    if (!from) return

    const label = PROTECTED_PAGE_LABELS[from] ?? 'That page'

    setAuthPrompt({
      heading: 'Sign in to continue',
      body: `${label} needs an account. Browsing and filtering here stay open to everyone.`,
    })

    navigate(location.pathname, { replace: true, state: null })
    // Only ever react to a freshly-arrived redirect, not our own cleanup.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.state])

  useEffect(() => {
    let cancelled = false

    getAllDiagnoses()
      .then((data) => {
        if (!cancelled) setRecords(data)
      })
      .catch(() => {
        if (!cancelled) setError('Unable to load registry data.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const filteredRecords = useMemo(() => {
    const term = search.trim().toLowerCase()

    return records.filter((record) => {
      const matchesTerm =
        !term ||
        record.gender.toLowerCase().includes(term) ||
        record.pain_type.toLowerCase().includes(term)

      const matchesPainType =
        painTypes.length === 0 || painTypes.includes(record.pain_type)

      const matchesGender = gender === 'Any' || record.gender === gender

      const matchesAge = record.age >= minAge && record.age <= maxAge

      return matchesTerm && matchesPainType && matchesGender && matchesAge
    })
  }, [records, search, painTypes, gender, minAge, maxAge])

  const totalPages = Math.max(1, Math.ceil(filteredRecords.length / PAGE_SIZE))
  const pageStart = (currentPage - 1) * PAGE_SIZE
  const pageRecords = filteredRecords.slice(pageStart, pageStart + PAGE_SIZE)

  const hasActiveFilters =
    search.trim() !== '' ||
    painTypes.length > 0 ||
    gender !== 'Any' ||
    minAge !== AGE_BOUND_MIN ||
    maxAge !== AGE_BOUND_MAX

  const resetPage = () => setCurrentPage(1)

  const handleReset = () => {
    setSearch('')
    setPainTypes([])
    setGender('Any')
    setMinAge(AGE_BOUND_MIN)
    setMaxAge(AGE_BOUND_MAX)
    resetPage()
  }

  return (
    <div className="min-h-screen bg-[#f5f3f3]">
      <GuestRegistryNavbar />

      <main>
        <GuestRegistryToolbar
          search={search}
          onSearchChange={(value) => {
            setSearch(value)
            resetPage()
          }}
          painTypes={painTypes}
          onPainTypesChange={(values) => {
            setPainTypes(values)
            resetPage()
          }}
          gender={gender}
          onGenderChange={(value) => {
            setGender(value)
            resetPage()
          }}
          minAge={minAge}
          maxAge={maxAge}
          onAgeChange={(min, max) => {
            setMinAge(min)
            setMaxAge(max)
            resetPage()
          }}
          onReset={handleReset}
          hasActiveFilters={hasActiveFilters}
        />

        <section className="px-8 py-8">
          <div className="mb-6 flex items-center justify-between">
            <h1 className="text-lg font-extrabold uppercase tracking-wide text-gray-900">
              Cardiac Diagnosis Registry
            </h1>

            <p className="text-sm text-gray-500">
              {loading
                ? 'Loading…'
                : `Showing ${pageRecords.length ? pageStart + 1 : 0}–${
                    pageStart + pageRecords.length
                  } of ${filteredRecords.length}`}
            </p>
          </div>

          {error && <p className="mb-4 text-sm text-[#ed3217]">{error}</p>}

          {!loading && !error && filteredRecords.length === 0 && (
            <p className="py-10 text-center text-sm text-gray-500">
              No records match these filters.
            </p>
          )}

          <RecordGrid
            records={pageRecords.map((record) => ({
              id: record.id,
              gender: record.gender,
              age: record.age,
              diagnosis: record.pain_type,
            }))}
            onBookmark={(_record, index) => {
              const positionLabel = String(pageStart + index + 1).padStart(3, '0')
              setAuthPrompt({
                body: (
                  <>
                    Record {positionLabel} will be waiting once you're in. Browsing
                    and filtering stay open to everyone.
                  </>
                ),
              })
            }}
            onSelect={(record, index) =>
              setSelectedRecord({
                id: record.id,
                positionLabel: String(pageStart + index + 1).padStart(3, '0'),
              })
            }
          />

          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
          />
        </section>

        <AccountPrompt />
      </main>

      <AnimatePresence>
        {selectedRecord && (
          <DiagnosisDetailPanel
            recordId={selectedRecord.id}
            positionLabel={selectedRecord.positionLabel}
            onClose={() => setSelectedRecord(null)}
            onSaveRecord={() => {
              const positionLabel = selectedRecord.positionLabel
              setSelectedRecord(null)
              setAuthPrompt({
                body: (
                  <>
                    Record {positionLabel} will be waiting once you're in. Browsing
                    and filtering stay open to everyone.
                  </>
                ),
              })
            }}
          />
        )}
      </AnimatePresence>

      <AnimatePresence>
        {authPrompt && (
          <SignInPromptModal
            heading={authPrompt.heading}
            body={authPrompt.body}
            onClose={() => setAuthPrompt(null)}
          />
        )}
      </AnimatePresence>
    </div>
  )
}

export default GuestRegistry
