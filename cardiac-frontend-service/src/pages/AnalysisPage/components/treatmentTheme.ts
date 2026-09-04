// The dataset only ever produces these four treatments - mapped to fixed
// colors/short labels so the legend and bars stay stable between requests.
// Anything unrecognized still renders (falls back to gray + its raw name)
// rather than silently dropping data.
export const TREATMENT_COLORS: Record<string, string> = {
  Medication: '#211f1f',
  Angioplasty: '#f2a341',
  'Coronary Artery Bypass Graft (CABG)': '#ed3217',
  'Lifestyle Changes': '#b9b6b6',
}

export const TREATMENT_LABELS: Record<string, string> = {
  Medication: 'Medication',
  Angioplasty: 'Angioplasty',
  'Coronary Artery Bypass Graft (CABG)': 'Surgery',
  'Lifestyle Changes': 'Lifestyle',
}

const FALLBACK_COLOR = '#94a3b8'

export const colorForTreatment = (treatment: string) =>
  TREATMENT_COLORS[treatment] ?? FALLBACK_COLOR

export const labelForTreatment = (treatment: string) =>
  TREATMENT_LABELS[treatment] ?? treatment

// Fixed draw order so bars/legend line up the same way across every group.
export const TREATMENT_ORDER = [
  'Medication',
  'Coronary Artery Bypass Graft (CABG)',
  'Angioplasty',
  'Lifestyle Changes',
]

export const orderTreatments = (treatments: string[]) =>
  [...treatments].sort(
    (a, b) => TREATMENT_ORDER.indexOf(a) - TREATMENT_ORDER.indexOf(b),
  )
