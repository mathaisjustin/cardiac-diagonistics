import { motion } from 'framer-motion'

interface RecordCardProps {
  gender: string
  age: number
  diagnosis: string
  bloodPressure?: string
  onBookmark?: () => void
  onSelect?: () => void
  delay?: number
}

const RecordCard = ({
  gender,
  age,
  diagnosis,
  bloodPressure,
  onBookmark,
  onSelect,
  delay = 0,
}: RecordCardProps) => {
  return (
    <motion.article
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.25, delay }}
      whileHover={onSelect ? { y: -3 } : undefined}
      onClick={onSelect}
      className={`border border-gray-500 bg-[#f8f6f6] px-4 py-4 ${
        onSelect ? 'cursor-pointer transition-shadow hover:border-gray-900 hover:shadow-md' : ''
      }`}
    >
      {/* Patient */}
      <h3 className="text-xl font-extrabold text-gray-900">
        {gender} · {age}
      </h3>

      {/* Diagnosis and blood pressure */}
      <p className="mt-1 text-xs text-gray-700">
        {bloodPressure ? `${bloodPressure} · ${diagnosis}` : diagnosis}
      </p>

      {/* Bookmark */}
      <div className="mt-4">
        <motion.button
          type="button"
          whileTap={{ scale: 0.94 }}
          onClick={(event) => {
            event.stopPropagation()
            onBookmark?.()
          }}
          className="flex items-center gap-2 border border-gray-500 bg-transparent px-3 py-1.5 text-xs font-bold text-gray-900 transition-colors hover:bg-gray-100"
        >
          <span className="text-sm">☆</span>
          <span>Bookmark</span>
        </motion.button>
      </div>
    </motion.article>
  )
}

export default RecordCard
