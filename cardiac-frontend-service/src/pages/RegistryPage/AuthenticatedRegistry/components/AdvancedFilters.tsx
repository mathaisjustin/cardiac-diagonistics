import { useState } from 'react'
import { motion } from 'framer-motion'

import type { RegistryFilters } from '../../components/registryTypes'

interface AdvancedFiltersProps {
  onSearch: (filters: RegistryFilters) => void
  onReset: () => void
}

const MIN_AGE = 18
const MAX_AGE = 90

const MIN_BP = 90
const MAX_BP = 200

const PAIN_TYPES = [
  'Typical Angina',
  'Atypical Angina',
  'Non-anginal Pain',
  'Asymptomatic',
]

const AdvancedFilters = ({
  onSearch,
  onReset,
}: AdvancedFiltersProps) => {
  const [painType, setPainType] = useState<string | null>('Typical Angina')

  const [minAge, setMinAge] = useState(50)
  const [maxAge, setMaxAge] = useState(59)

  const [minBp, setMinBp] = useState(MIN_BP)
  const [maxBp, setMaxBp] = useState(MAX_BP)

  const [gender, setGender] = useState<
    'Male' | 'Female' | 'Any'
  >('Male')

  const handleMinAgeChange = (
    event: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const value = Number(event.target.value)

    if (value <= maxAge) {
      setMinAge(value)
    }
  }

  const handleMaxAgeChange = (
    event: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const value = Number(event.target.value)

    if (value >= minAge) {
      setMaxAge(value)
    }
  }

  const handleMinBpChange = (
    event: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const value = Number(event.target.value)

    if (value <= maxBp) {
      setMinBp(value)
    }
  }

  const handleMaxBpChange = (
    event: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const value = Number(event.target.value)

    if (value >= minBp) {
      setMaxBp(value)
    }
  }

  const handleSearch = () => {
    onSearch({
      painType,
      minAge,
      maxAge,
      bpMin: minBp,
      bpMax: maxBp,
      gender,
    })
  }

  const handleReset = () => {
    setPainType(null)
    setMinAge(MIN_AGE)
    setMaxAge(MAX_AGE)
    setMinBp(MIN_BP)
    setMaxBp(MAX_BP)
    setGender('Any')

    onReset()
  }

  return (
    <div className="space-y-6">
      {/* Pain Type */}
      <div>
        <h3 className="mb-3 text-xs font-semibold uppercase tracking-wide text-gray-500">
          Pain Type
        </h3>

        <div className="space-y-2">
          {PAIN_TYPES.map((option) => (
            <label
              key={option}
              className="flex cursor-pointer items-center gap-3 text-sm text-gray-900"
            >
              <input
                type="radio"
                name="painType"
                checked={painType === option}
                onChange={() => setPainType(option)}
                className="h-5 w-5 cursor-pointer accent-[#ed3217]"
              />

              <span>{option}</span>
            </label>
          ))}

          <label className="flex cursor-pointer items-center gap-3 text-sm text-gray-900">
            <input
              type="radio"
              name="painType"
              checked={painType === null}
              onChange={() => setPainType(null)}
              className="h-5 w-5 cursor-pointer accent-[#ed3217]"
            />

            <span>Any</span>
          </label>
        </div>
      </div>

      {/* Age */}
      <div>
        <div className="mb-3 flex items-center justify-between">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-gray-500">
            Age
          </h3>

          <span className="text-xs font-semibold uppercase tracking-wide text-gray-600">
            {minAge}–{maxAge}
          </span>
        </div>

        <div className="relative h-6">
          {/* Track */}
          <div className="absolute left-0 right-0 top-2.5 h-[3px] bg-gray-300" />

          {/* Selected range */}
          <motion.div
            className="absolute top-2.5 h-[3px] bg-[#ed3217]"
            animate={{
              left: `${
                ((minAge - MIN_AGE) /
                  (MAX_AGE - MIN_AGE)) *
                100
              }%`,
              right: `${
                100 -
                ((maxAge - MIN_AGE) /
                  (MAX_AGE - MIN_AGE)) *
                  100
              }%`,
            }}
            transition={{ duration: 0.15 }}
          />

          {/* Minimum slider */}
          <input
            type="range"
            min={MIN_AGE}
            max={MAX_AGE}
            value={minAge}
            onChange={handleMinAgeChange}
            className="pointer-events-auto absolute left-0 top-0 h-6 w-full cursor-pointer appearance-none bg-transparent accent-[#211f1f]"
          />

          {/* Maximum slider */}
          <input
            type="range"
            min={MIN_AGE}
            max={MAX_AGE}
            value={maxAge}
            onChange={handleMaxAgeChange}
            className="pointer-events-auto absolute left-0 top-0 h-6 w-full cursor-pointer appearance-none bg-transparent accent-[#211f1f]"
          />
        </div>
      </div>

      {/* Blood Pressure */}
      <div>
        <div className="mb-3 flex items-center justify-between">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-gray-500">
            Blood Pressure
          </h3>

          <span className="text-xs font-semibold uppercase tracking-wide text-gray-600">
            {minBp}–{maxBp}
          </span>
        </div>

        <div className="relative h-6">
          {/* Track */}
          <div className="absolute left-0 right-0 top-2.5 h-[3px] bg-gray-300" />

          {/* Selected range */}
          <motion.div
            className="absolute top-2.5 h-[3px] bg-[#ed3217]"
            animate={{
              left: `${
                ((minBp - MIN_BP) / (MAX_BP - MIN_BP)) * 100
              }%`,
              right: `${
                100 - ((maxBp - MIN_BP) / (MAX_BP - MIN_BP)) * 100
              }%`,
            }}
            transition={{ duration: 0.15 }}
          />

          {/* Minimum slider */}
          <input
            type="range"
            min={MIN_BP}
            max={MAX_BP}
            value={minBp}
            onChange={handleMinBpChange}
            className="pointer-events-auto absolute left-0 top-0 h-6 w-full cursor-pointer appearance-none bg-transparent accent-[#211f1f]"
          />

          {/* Maximum slider */}
          <input
            type="range"
            min={MIN_BP}
            max={MAX_BP}
            value={maxBp}
            onChange={handleMaxBpChange}
            className="pointer-events-auto absolute left-0 top-0 h-6 w-full cursor-pointer appearance-none bg-transparent accent-[#211f1f]"
          />
        </div>
      </div>

      {/* Gender */}
      <div>
        <h3 className="mb-3 text-xs font-semibold uppercase tracking-wide text-gray-500">
          Gender
        </h3>

        <div className="flex">
          {(['Male', 'Female', 'Any'] as const).map(
            (option, index) => (
              <button
                key={option}
                type="button"
                onClick={() => setGender(option)}
                className={`relative h-10 flex-1 overflow-hidden border-gray-500 text-sm font-semibold ${
                  index > 0 ? 'border-l-0' : ''
                } ${
                  gender === option
                    ? 'text-white'
                    : 'bg-[#f5f3f3] text-gray-900 hover:bg-gray-100'
                } border`}
              >
                {gender === option && (
                  <motion.span
                    layoutId="advanced-filters-gender-active"
                    transition={{ type: 'spring', stiffness: 500, damping: 35 }}
                    className="absolute inset-0 bg-[#211f1f]"
                  />
                )}
                <span className="relative">{option}</span>
              </button>
            ),
          )}
        </div>
      </div>

      {/* Actions */}
      <div className="pt-1">
        <motion.button
          whileTap={{ scale: 0.98 }}
          type="button"
          onClick={handleSearch}
          className="h-11 w-full bg-[#ed3217] px-4 text-sm font-bold text-white transition-colors hover:bg-[#d92d15]"
        >
          Search
        </motion.button>

        <button
          type="button"
          onClick={handleReset}
          className="mt-4 text-sm text-gray-700 hover:text-gray-900 hover:underline"
        >
          Reset filters
        </button>
      </div>
    </div>
  )
}

export default AdvancedFilters
