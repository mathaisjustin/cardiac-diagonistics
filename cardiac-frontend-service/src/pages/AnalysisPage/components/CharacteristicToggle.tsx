import { motion } from 'framer-motion'

import type { AnalysisCharacteristic } from '../../../api/diagnosisApi'

const OPTIONS: { key: AnalysisCharacteristic; label: string }[] = [
  { key: 'age', label: 'Age' },
  { key: 'gender', label: 'Gender' },
  { key: 'painType', label: 'Pain type' },
]

interface CharacteristicToggleProps {
  value: AnalysisCharacteristic
  onChange: (value: AnalysisCharacteristic) => void
}

const CharacteristicToggle = ({ value, onChange }: CharacteristicToggleProps) => {
  return (
    <div className="flex gap-2">
      {OPTIONS.map((option) => (
        <button
          key={option.key}
          type="button"
          onClick={() => onChange(option.key)}
          className={`relative h-10 overflow-hidden border px-4 text-sm font-bold transition-colors ${
            value === option.key
              ? 'border-[#211f1f] text-white'
              : 'border-gray-500 bg-white text-gray-900 hover:bg-gray-100'
          }`}
        >
          {value === option.key && (
            <motion.span
              layoutId="characteristic-toggle-active"
              transition={{ type: 'spring', stiffness: 500, damping: 35 }}
              className="absolute inset-0 bg-[#211f1f]"
            />
          )}
          <span className="relative">{option.label}</span>
        </button>
      ))}
    </div>
  )
}

export default CharacteristicToggle
