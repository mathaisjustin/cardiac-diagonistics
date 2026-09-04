import { motion } from 'framer-motion'

import type { AnalysisGroup } from '../../../api/diagnosisApi'
import { labelForTreatment } from './treatmentTheme'

interface AnalysisInsightsProps {
  breakdown: AnalysisGroup[]
}

const SURGERY_TREATMENT = 'Coronary Artery Bypass Graft (CABG)'

interface Insight {
  lead: string
  rest: string
}

const buildInsights = (breakdown: AnalysisGroup[]): Insight[] => {
  if (breakdown.length === 0) return []

  const insights: Insight[] = []

  // Strongest skew: the group/treatment pair with the single highest percentage.
  let skew: { group: AnalysisGroup; treatment: string; pct: number } | null = null

  for (const group of breakdown) {
    for (const [treatment, pct] of Object.entries(group.treatmentPercentages)) {
      if (!skew || pct > skew.pct) {
        skew = { group, treatment, pct }
      }
    }
  }

  if (skew) {
    const label = labelForTreatment(skew.treatment)
    insights.push({
      lead: `${skew.group.value} skews to ${label}.`,
      rest: `${skew.pct}% of that group ends in ${label.toLowerCase()} - the strongest lean in the dataset.`,
    })
  }

  // Least surgery: the group with the lowest CABG share, if surgery happens at all.
  const withSurgery = breakdown.filter(
    (group) => group.treatmentPercentages[SURGERY_TREATMENT] !== undefined,
  )

  if (withSurgery.length > 0) {
    const least = withSurgery.reduce((min, group) =>
      group.treatmentPercentages[SURGERY_TREATMENT] <
      min.treatmentPercentages[SURGERY_TREATMENT]
        ? group
        : min,
    )

    const pct = least.treatmentPercentages[SURGERY_TREATMENT]

    if (least.value !== skew?.group.value || skew.treatment !== SURGERY_TREATMENT) {
      insights.push({
        lead: `${least.value} sees the least surgery.`,
        rest: `Only ${pct}% of that group ends in surgery.`,
      })
    }
  }

  return insights.slice(0, 2)
}

const AnalysisInsights = ({ breakdown }: AnalysisInsightsProps) => {
  const insights = buildInsights(breakdown)

  if (insights.length === 0) return null

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      {insights.map((insight, index) => (
        <motion.div
          key={insight.lead}
          initial={{ opacity: 0, x: -12 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.3, delay: 0.4 + index * 0.1 }}
          className="border-l-4 border-[#ed3217] bg-gray-100 px-5 py-4"
        >
          <p className="text-sm text-gray-800">
            <span className="font-bold text-gray-900">{insight.lead}</span>{' '}
            {insight.rest}
          </p>
        </motion.div>
      ))}
    </div>
  )
}

export default AnalysisInsights
