import RegistrySearch from '../../components/RegistrySearch'
import PainTypeFilter from './PainTypeFilter'
import GenderFilter from './GenderFilter'
import AgeFilter from './AgeFilter'

type Gender = 'Male' | 'Female' | 'Any'

interface GuestRegistryToolbarProps {
  search: string
  onSearchChange: (value: string) => void
  painTypes: string[]
  onPainTypesChange: (values: string[]) => void
  gender: Gender
  onGenderChange: (value: Gender) => void
  minAge: number
  maxAge: number
  onAgeChange: (minAge: number, maxAge: number) => void
  onReset: () => void
  hasActiveFilters: boolean
}

const GuestRegistryToolbar = ({
  search,
  onSearchChange,
  painTypes,
  onPainTypesChange,
  gender,
  onGenderChange,
  minAge,
  maxAge,
  onAgeChange,
  onReset,
  hasActiveFilters,
}: GuestRegistryToolbarProps) => {
  return (
    <section className="border-b border-gray-300 bg-[#f5f3f3] px-8 py-5">
      <div className="flex items-center gap-4">
        <RegistrySearch value={search} onChange={onSearchChange} />

        <PainTypeFilter selected={painTypes} onChange={onPainTypesChange} />

        <GenderFilter value={gender} onChange={onGenderChange} />

        <AgeFilter minAge={minAge} maxAge={maxAge} onChange={onAgeChange} />

        {hasActiveFilters && (
          <button
            type="button"
            onClick={onReset}
            className="whitespace-nowrap text-sm font-semibold text-gray-600 hover:text-gray-900 hover:underline"
          >
            Reset filters
          </button>
        )}
      </div>
    </section>
  )
}

export default GuestRegistryToolbar
