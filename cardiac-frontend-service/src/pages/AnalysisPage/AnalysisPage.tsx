import { useEffect, useState } from 'react'
import { AnimatePresence, motion } from 'framer-motion'

import AuthenticatedNavbar from '../../components/Navbar/AuthenticatedNavbar'

import CharacteristicToggle from './components/CharacteristicToggle'
import TreatmentLegend from './components/TreatmentLegend'
import TreatmentChart from './components/TreatmentChart'
import AnalysisInsights from './components/AnalysisInsights'

import {
  getAnalysis,
  type AnalysisCharacteristic,
  type AnalysisResult,
} from '../../api/diagnosisApi'

const CHARACTERISTIC_LABELS: Record<AnalysisCharacteristic, string> = {
  age: 'age',
  gender: 'gender',
  painType: 'pain type',
}

const AnalysisPage = () => {
  const [characteristic, setCharacteristic] = useState<AnalysisCharacteristic>('painType')
  const [result, setResult] = useState<AnalysisResult | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    getAnalysis(characteristic)
      .then((data) => {
        if (!cancelled) setResult(data)
      })
      .catch(() => {
        if (!cancelled) setError('Unable to load the analysis.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [characteristic])

  return (
    <div className="min-h-screen bg-[#f5f3f3]">
      <AuthenticatedNavbar />

      <main className="flex min-h-[calc(100vh-88px)] flex-col px-10 py-8">
        <div className="flex flex-1 flex-col bg-white">
          <div className="flex flex-wrap items-start justify-between gap-4 border-b border-gray-300 px-8 py-6">
            <div>
              <h1 className="text-3xl font-extrabold text-gray-900">
                Treatment by {CHARACTERISTIC_LABELS[characteristic]}
              </h1>

              <p className="mt-1 text-sm text-gray-500">
                Whole dataset, recomputed each request.
              </p>
            </div>

            <CharacteristicToggle value={characteristic} onChange={setCharacteristic} />
          </div>

          <div className="flex flex-1 flex-col px-8 py-6">
            {loading && (
              <motion.p
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                className="text-sm text-gray-500"
              >
                Loading…
              </motion.p>
            )}

            {error && <p className="text-sm text-[#ed3217]">{error}</p>}

            <AnimatePresence mode="wait">
              {result && !loading && !error && (
                <motion.div
                  key={characteristic}
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                  transition={{ duration: 0.15 }}
                  className="flex flex-1 flex-col"
                >
                  <TreatmentLegend
                    treatments={Object.keys(result.overallTreatmentCounts)}
                  />

                  <div className="flex flex-1 items-end py-8">
                    <TreatmentChart
                      breakdown={result.breakdown}
                      treatments={Object.keys(result.overallTreatmentCounts)}
                    />
                  </div>

                  <AnalysisInsights breakdown={result.breakdown} />
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </div>
      </main>
    </div>
  )
}

export default AnalysisPage
