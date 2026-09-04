import { useEffect, useRef, useState } from 'react'
import { AnimatePresence, motion } from 'framer-motion'

const PAIN_TYPES = [
  'Typical Angina',
  'Atypical Angina',
  'Non-anginal Pain',
  'Asymptomatic',
]

interface PainTypeFilterProps {
  selected: string[]
  onChange: (values: string[]) => void
}

const PainTypeFilter = ({ selected, onChange }: PainTypeFilterProps) => {
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

  const toggle = (value: string) => {
    if (selected.includes(value)) {
      onChange(selected.filter((item) => item !== value))
    } else {
      onChange([...selected, value])
    }
  }

  const label = selected.length === 0 ? 'Pain type' : `Pain type (${selected.length})`

  return (
    <div ref={rootRef} className="relative">
      <button
        type="button"
        onClick={() => setOpen((current) => !current)}
        className="flex h-12 min-w-[120px] cursor-pointer items-center justify-between gap-2 border border-gray-500 bg-white px-4 text-sm font-semibold text-gray-900"
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
            className="absolute z-10 mt-1 w-56 space-y-2 border border-gray-500 bg-white p-3 shadow-lg"
          >
            {PAIN_TYPES.map((painType) => (
              <label
                key={painType}
                className="flex cursor-pointer items-center gap-2 text-sm text-gray-900"
              >
                <input
                  type="checkbox"
                  checked={selected.includes(painType)}
                  onChange={() => toggle(painType)}
                  className="h-4 w-4 accent-[#ed3217]"
                />
                {painType}
              </label>
            ))}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}

export default PainTypeFilter
