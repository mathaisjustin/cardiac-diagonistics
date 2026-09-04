import { motion } from 'framer-motion'

import type { DiagnosisStats } from '../../../api/diagnosisApi'

interface StatisticsSectionProps {
  stats: DiagnosisStats | null
  error: string | null
}

const StatisticsSection = ({ stats, error }: StatisticsSectionProps) => {
  const statistics = [
    {
      value: stats ? stats.totalRecords.toLocaleString() : '…',
      label: 'Records',
    },
    {
      value: stats ? stats.meanAge.toFixed(1) : '…',
      label: 'Mean age',
    },
    {
      value: stats ? `${stats.surgeryShare}%` : '…',
      label: 'Surgery share',
    },
    {
      value: 'LIVE',
      label: 'Sourced per request',
    },
  ]

  return (
    <section className="border-b border-gray-300 bg-[#f3f1f1]">
      {error && (
        <p className="px-8 py-2 text-xs font-semibold text-[#ed3217]">{error}</p>
      )}

      <div className="grid grid-cols-4">
        {statistics.map((statistic, index) => (
          <motion.div
            key={statistic.label}
            initial={{ opacity: 0, y: 14 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.35, delay: 0.05 * index }}
            className="border-r border-gray-300 px-8 py-7 last:border-r-0"
          >
            <motion.p
              key={statistic.value}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 0.3 }}
              className={`text-4xl font-extrabold ${
                statistic.value === 'LIVE'
                  ? 'text-[#ed3217]'
                  : 'text-gray-900'
              }`}
            >
              {statistic.value}
            </motion.p>

            <p className="mt-2 text-xs font-bold uppercase tracking-[0.12em] text-gray-500">
              {statistic.label}
            </p>
          </motion.div>
        ))}
      </div>
    </section>
  )
}

export default StatisticsSection
