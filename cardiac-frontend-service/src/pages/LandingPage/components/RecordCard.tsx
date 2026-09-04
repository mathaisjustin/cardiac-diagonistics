import { motion } from 'framer-motion'

interface RecordCardProps {
  index: number
  gender: string
  age: number
  diagnosis: string
}

const RecordCard = ({ index, gender, age, diagnosis }: RecordCardProps) => {
  return (
    <motion.article
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35, delay: 0.08 * index }}
      whileHover={{ y: -4 }}
      className="border border-gray-500 bg-[#faf8f8] p-6 transition-shadow hover:shadow-lg"
    >
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold tracking-[0.15em] text-gray-500">
          {String(index).padStart(3, '0')}
        </span>

        <span className="text-xl text-gray-400">☆</span>
      </div>

      <h3 className="mt-7 text-3xl font-extrabold text-gray-900">
        {gender} · {age}
      </h3>

      <p className="mt-1 text-base text-gray-600">
        {diagnosis}
      </p>
    </motion.article>
  )
}

export default RecordCard
