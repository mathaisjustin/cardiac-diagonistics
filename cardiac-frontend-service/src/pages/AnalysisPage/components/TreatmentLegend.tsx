import { motion } from 'framer-motion'

import { colorForTreatment, labelForTreatment, orderTreatments } from './treatmentTheme'

interface TreatmentLegendProps {
  treatments: string[]
}

const TreatmentLegend = ({ treatments }: TreatmentLegendProps) => {
  return (
    <div className="flex flex-wrap items-center gap-5">
      {orderTreatments(treatments).map((treatment, index) => (
        <motion.div
          key={treatment}
          initial={{ opacity: 0, y: -6 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.25, delay: index * 0.05 }}
          className="flex items-center gap-2"
        >
          <span
            className="h-3 w-3"
            style={{ backgroundColor: colorForTreatment(treatment) }}
          />
          <span className="text-sm font-semibold text-gray-700">
            {labelForTreatment(treatment)}
          </span>
        </motion.div>
      ))}
    </div>
  )
}

export default TreatmentLegend
