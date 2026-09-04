const SURGERY_TREATMENT = 'Coronary Artery Bypass Graft (CABG)'

const shortLabel = (treatment: string) =>
  treatment === SURGERY_TREATMENT ? 'Surgery' : treatment

interface TreatmentBadgeProps {
  treatment: string
}

const TreatmentBadge = ({ treatment }: TreatmentBadgeProps) => {
  const isSurgery = treatment === SURGERY_TREATMENT

  return (
    <span
      className={`inline-block border px-3 py-1.5 text-sm font-bold ${
        isSurgery
          ? 'border-[#ed3217] bg-[#ed3217] text-white'
          : 'border-gray-500 bg-white text-gray-900'
      }`}
    >
      {shortLabel(treatment)}
    </span>
  )
}

export default TreatmentBadge
