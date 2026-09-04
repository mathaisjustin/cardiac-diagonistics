import { useEffect, useRef, useState } from 'react'
import { AnimatePresence, motion } from 'framer-motion'

type Gender = 'Male' | 'Female' | 'Any'

interface GenderFilterProps {
  value: Gender
  onChange: (value: Gender) => void
}

const GenderFilter = ({ value, onChange }: GenderFilterProps) => {
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

  const label = value === 'Any' ? 'Gender' : `Gender: ${value}`

  return (
    <div ref={rootRef} className="relative">
      <button
        type="button"
        onClick={() => setOpen((current) => !current)}
        className="flex h-12 min-w-[110px] cursor-pointer items-center justify-between gap-2 border border-gray-500 bg-white px-4 text-sm font-semibold text-gray-900"
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
            className="absolute z-10 mt-1 w-40 space-y-1 border border-gray-500 bg-white p-2 shadow-lg"
          >
            {(['Any', 'Male', 'Female'] as const).map((option) => (
              <button
                key={option}
                type="button"
                onClick={() => {
                  onChange(option)
                  setOpen(false)
                }}
                className={`block w-full px-2 py-1.5 text-left text-sm transition-colors ${
                  value === option
                    ? 'bg-[#211f1f] text-white'
                    : 'text-gray-900 hover:bg-gray-100'
                }`}
              >
                {option}
              </button>
            ))}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}

export default GenderFilter
