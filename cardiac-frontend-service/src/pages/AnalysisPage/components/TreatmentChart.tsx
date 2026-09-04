import { motion } from 'framer-motion'

import type { AnalysisGroup } from '../../../api/diagnosisApi'
import { colorForTreatment, orderTreatments } from './treatmentTheme'

interface TreatmentChartProps {
  breakdown: AnalysisGroup[]
  treatments: string[]
}

const CHART_HEIGHT = 380

const TreatmentChart = ({ breakdown, treatments }: TreatmentChartProps) => {
  const orderedTreatments = orderTreatments(treatments)

  // Scale bars against the tallest percentage actually present, not against
  // a flat 0-100 - otherwise every bar sits in the bottom third of the chart
  // since no single group/treatment pair usually exceeds ~40%.
  const maxPercent = Math.max(
    1,
    ...breakdown.flatMap((group) => Object.values(group.treatmentPercentages)),
  )

  return (
    <div className="w-full overflow-x-auto">
      <div
        className="flex w-full min-w-max items-end justify-between gap-10 border-b-2 border-gray-900 pb-0"
        style={{ height: CHART_HEIGHT }}
      >
        {breakdown.map((group, groupIndex) => (
          <div key={group.value} className="flex h-full flex-1 items-end justify-center gap-2.5">
            {orderedTreatments.map((treatment, treatmentIndex) => {
              const pct = group.treatmentPercentages[treatment] ?? 0
              const heightPct = Math.max((pct / maxPercent) * 100, pct > 0 ? 2 : 0)

              return (
                <motion.div
                  key={treatment}
                  title={`${treatment}: ${pct}%`}
                  className="w-14"
                  initial={{ height: 0 }}
                  animate={{ height: `${heightPct}%` }}
                  transition={{
                    duration: 0.5,
                    ease: 'easeOut',
                    delay: groupIndex * 0.06 + treatmentIndex * 0.04,
                  }}
                  style={{ backgroundColor: colorForTreatment(treatment) }}
                />
              )
            })}
          </div>
        ))}
      </div>

      <div className="flex w-full min-w-max justify-between gap-10 pt-4">
        {breakdown.map((group, groupIndex) => (
          <motion.div
            key={group.value}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.3, delay: groupIndex * 0.06 + 0.2 }}
            className="flex-1 text-center"
          >
            <p className="text-base font-bold text-gray-900">{group.value}</p>
            <p className="text-sm text-gray-500">n={group.count}</p>
          </motion.div>
        ))}
      </div>
    </div>
  )
}

export default TreatmentChart
