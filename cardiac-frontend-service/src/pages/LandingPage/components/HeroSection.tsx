import { motion } from 'framer-motion'

interface HeroSectionProps {
  totalRecords?: number
}

const HeroSection = ({ totalRecords }: HeroSectionProps) => {
  const recordsLabel =
    totalRecords !== undefined ? totalRecords.toLocaleString() : '…'

  return (
    <section className="bg-[#ed3217] px-10 py-20 text-white">
      <motion.p
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="text-sm font-bold uppercase tracking-[0.15em]"
      >
        Cardiac Diagnosis Registry
      </motion.p>

      <motion.h1
        initial={{ opacity: 0, y: 24 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.1 }}
        className="mt-8 max-w-4xl text-7xl font-extrabold leading-[0.9] tracking-[-0.05em]"
      >
        Read the data
        <br />
        before you read
        <br />
        the conclusion.
      </motion.h1>

      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, delay: 0.35 }}
        className="mt-10 flex gap-4"
      >
        <motion.button
          whileHover={{ scale: 1.03 }}
          whileTap={{ scale: 0.97 }}
          className="bg-white px-7 py-4 text-base font-bold text-gray-900 transition-colors hover:bg-gray-100"
        >
          Browse {recordsLabel} records
        </motion.button>
      </motion.div>
    </section>
  )
}

export default HeroSection
