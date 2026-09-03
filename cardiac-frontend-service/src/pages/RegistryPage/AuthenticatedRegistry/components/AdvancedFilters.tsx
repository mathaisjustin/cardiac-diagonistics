import { useState } from 'react'

import type {
  RegistryFilters,
  RegistryRecord,
} from '../../components/registryTypes'

interface AdvancedFiltersProps {
  onSearch: (filters: RegistryFilters) => void
  onReset: () => void
}

const MIN_AGE = 18
const MAX_AGE = 90

const AdvancedFilters = ({
  onSearch,
  onReset,
}: AdvancedFiltersProps) => {
  const [painTypes, setPainTypes] = useState<
    RegistryRecord['diagnosis'][]
  >(['Typical Angina'])

  const [minAge, setMinAge] = useState(50)
  const [maxAge, setMaxAge] = useState(59)

  const [bloodPressure, setBloodPressure] = useState('')

  const [gender, setGender] = useState<
    'Male' | 'Female' | 'Any'
  >('Male')

  const togglePainType = (
    painType: RegistryRecord['diagnosis'],
  ) => {
    setPainTypes((current) => {
      if (current.includes(painType)) {
        return current.filter((item) => item !== painType)
      }

      return [...current, painType]
    })
  }

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

  const handleSearch = () => {
    onSearch({
      painTypes,
      minAge,
      maxAge,
      bloodPressure: bloodPressure.trim(),
      gender,
    })
  }

  const handleReset = () => {
    setPainTypes([])
    setMinAge(MIN_AGE)
    setMaxAge(MAX_AGE)
    setBloodPressure('')
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
          {[
            'Typical Angina',
            'Atypical Angina',
            'Non-anginal',
            'Asymptomatic',
          ].map((painType) => (
            <label
              key={painType}
              className="flex cursor-pointer items-center gap-3 text-sm text-gray-900"
            >
              <input
                type="checkbox"
                checked={painTypes.includes(
                  painType as RegistryRecord['diagnosis'],
                )}
                onChange={() =>
                  togglePainType(
                    painType as RegistryRecord['diagnosis'],
                  )
                }
                className="h-5 w-5 cursor-pointer accent-[#ed3217]"
              />

              <span>{painType}</span>
            </label>
          ))}
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
          <div
            className="absolute top-2.5 h-[3px] bg-[#ed3217]"
            style={{
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
        <h3 className="mb-3 text-xs font-semibold uppercase tracking-wide text-gray-500">
          Blood Pressure
        </h3>

        <input
          type="text"
          value={bloodPressure}
          onChange={(event) =>
            setBloodPressure(event.target.value)
          }
          placeholder="Any"
          className="h-10 w-full border border-gray-500 bg-[#f5f3f3] px-3 text-sm text-gray-900 outline-none placeholder:text-gray-400 focus:border-gray-900"
        />
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
                className={`h-10 flex-1 border-gray-500 text-sm font-semibold ${
                  index > 0 ? 'border-l-0' : ''
                } ${
                  gender === option
                    ? 'bg-[#211f1f] text-white'
                    : 'bg-[#f5f3f3] text-gray-900 hover:bg-gray-100'
                } border`}
              >
                {option}
              </button>
            ),
          )}
        </div>
      </div>

      {/* Actions */}
      <div className="pt-1">
        <button
          type="button"
          onClick={handleSearch}
          className="h-11 w-full bg-[#ed3217] px-4 text-sm font-bold text-white transition-colors hover:bg-[#d92d15]"
        >
          Search
        </button>

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