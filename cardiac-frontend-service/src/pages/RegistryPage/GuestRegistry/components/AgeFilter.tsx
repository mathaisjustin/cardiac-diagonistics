import { useEffect, useRef, useState } from 'react'
import { AnimatePresence, motion } from 'framer-motion'

export const AGE_BOUND_MIN = 18
export const AGE_BOUND_MAX = 100

interface AgeFilterProps {
  minAge: number
  maxAge: number
  onChange: (minAge: number, maxAge: number) => void
}

const AgeFilter = ({ minAge, maxAge, onChange }: AgeFilterProps) => {
  const [open, setOpen] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return

    const handleClickOutside = (event: MouseEvent) => {
      if (rootRef.current && !rootRef.current.contains(event.target as Node)) {
        setOpen(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [open])

  const isDefault = minAge === AGE_BOUND_MIN && maxAge === AGE_BOUND_MAX
  const label = isDefault ? 'Age' : `Age: ${minAge}–${maxAge}`

  return (
    <div ref={rootRef} className="relative">
      <button
        type="button"
        onClick={() => setOpen((current) => !current)}
        className="flex h-12 min-w-[100px] cursor-pointer items-center justify-between gap-2 border border-gray-500 bg-white px-4 text-sm font-semibold text-gray-900"
      >
        {label}
        <motion.span animate={{ rotate: open ? 180 : 0 }} transition={{ duration: 0.15 }}>
          ▾
        </motion.span>
      </button>

      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0, y: -6, scale: 0.98 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -6, scale: 0.98 }}
            transition={{ duration: 0.15 }}
            className="absolute z-10 mt-1 w-56 space-y-3 border border-gray-500 bg-white p-3 shadow-lg"
          >
            <label className="block text-xs font-semibold uppercase tracking-wide text-gray-500">
              Min: {minAge}
              <input
                type="range"
                min={AGE_BOUND_MIN}
                max={AGE_BOUND_MAX}
                value={minAge}
                onChange={(event) => {
                  const value = Number(event.target.value)
                  if (value <= maxAge) onChange(value, maxAge)
                }}
                className="mt-1 w-full accent-[#ed3217]"
              />
            </label>

            <label className="block text-xs font-semibold uppercase tracking-wide text-gray-500">
              Max: {maxAge}
              <input
                type="range"
                min={AGE_BOUND_MIN}
                max={AGE_BOUND_MAX}
                value={maxAge}
                onChange={(event) => {
                  const value = Number(event.target.value)
                  if (value >= minAge) onChange(minAge, value)
                }}
                className="mt-1 w-full accent-[#ed3217]"
              />
            </label>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}

export default AgeFilter
