import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import { Link } from 'react-router-dom'

import {
  getDiagnosisById,
  type DiagnosisDetail,
} from '../../../api/diagnosisApi'

interface DiagnosisDetailPanelProps {
  recordId: string
  positionLabel: string
  onClose: () => void
  onSaveRecord: (record: DiagnosisDetail) => void
}

const DiagnosisDetailPanel = ({
  recordId,
  positionLabel,
  onClose,
  onSaveRecord,
}: DiagnosisDetailPanelProps) => {
  const [detail, setDetail] = useState<DiagnosisDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    setLoading(true)
    setDetail(null)
    setError(null)

    getDiagnosisById(recordId)
      .then((data) => {
        if (!cancelled) {
          setDetail(data)
        }
      })
      .catch(() => {
        if (!cancelled) {
          setError('Unable to load this record.')
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [recordId])

  const diabeticPositive =
    detail?.diabetic.toLowerCase() === 'yes'

  const isSmoker = Boolean(
    detail?.smoking_status &&
      detail.smoking_status.toLowerCase() !== 'never',
  )

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.2 }}
      className="fixed inset-0 z-50 flex justify-end bg-black/45 backdrop-blur-sm"
      onClick={onClose}
    >
      <motion.aside
        initial={{ x: '100%' }}
        animate={{ x: 0 }}
        exit={{ x: '100%' }}
        transition={{ duration: 0.3, ease: 'easeOut' }}
        className="flex h-full w-full max-w-[520px] flex-col border-l border-gray-800 bg-[#f5f3f3] shadow-2xl"
        onClick={(event) => event.stopPropagation()}
      >
        {/* Header */}
        <header className="bg-[#211f1f] px-8 py-7 text-white">
          <div className="flex items-start justify-between">
            <p className="text-xs font-bold uppercase tracking-[0.18em] text-[#ff5b3d]">
              Record {positionLabel}
            </p>

            <button
              type="button"
              onClick={onClose}
              aria-label="Close"
              className="flex h-8 w-8 items-center justify-center text-2xl leading-none text-gray-400 transition hover:text-white"
            >
              ×
            </button>
          </div>

          {detail && (
            <div className="mt-8">
              <h2 className="text-4xl font-extrabold leading-none tracking-tight">
                {detail.gender} · {detail.age}
              </h2>

              <p className="mt-3 text-base text-gray-300">
                {detail.pain_type}
              </p>
            </div>
          )}
        </header>

        {/* Body */}
        <div className="flex-1 overflow-y-auto px-8 py-8">
          {loading && (
            <div className="flex min-h-[200px] items-center justify-center">
              <p className="text-sm text-gray-500">
                Loading record…
              </p>
            </div>
          )}

          {error && (
            <div className="border border-[#f3b7ac] bg-[#fbe4de] px-4 py-4">
              <p className="text-sm font-medium text-[#ed3217]">
                {error}
              </p>
            </div>
          )}

          {detail && (
            <div className="space-y-8">
              {/* Measurements */}
              <section>
                <div className="grid grid-cols-2 gap-4">
                  <div className="border border-gray-400 bg-white px-5 py-5">
                    <p className="text-xs font-bold uppercase tracking-[0.12em] text-gray-500">
                      Blood Pressure
                    </p>

                    <p className="mt-3 text-3xl font-extrabold tracking-tight text-gray-900">
                      {detail.bp}
                    </p>
                  </div>

                  <div className="border border-gray-400 bg-white px-5 py-5">
                    <p className="text-xs font-bold uppercase tracking-[0.12em] text-gray-500">
                      Cholesterol
                    </p>

                    <p className="mt-3 text-3xl font-extrabold tracking-tight text-gray-900">
                      {detail.cholesterol}
                    </p>
                  </div>
                </div>
              </section>

              {/* Risk Flags */}
              <section>
                <div className="flex items-center justify-between">
                  <h3 className="text-xs font-bold uppercase tracking-[0.12em] text-gray-500">
                    Risk Flags
                  </h3>

                  <span className="text-xs text-gray-400">
                    Clinical indicators
                  </span>
                </div>

                <div className="mt-4 grid grid-cols-2 gap-3">
                  <div
                    className={`border px-4 py-3 ${
                      diabeticPositive
                        ? 'border-[#f3b7ac] bg-[#fbe4de]'
                        : 'border-gray-400 bg-white'
                    }`}
                  >
                    <p className="text-[10px] font-bold uppercase tracking-wide text-gray-500">
                      Diabetes
                    </p>

                    <p
                      className={`mt-1 text-sm font-bold ${
                        diabeticPositive
                          ? 'text-[#ed3217]'
                          : 'text-gray-900'
                      }`}
                    >
                      {diabeticPositive
                        ? 'Diabetic'
                        : 'Non-diabetic'}
                    </p>
                  </div>

                  <div className="border border-gray-400 bg-white px-4 py-3">
                    <p className="text-[10px] font-bold uppercase tracking-wide text-gray-500">
                      Smoking
                    </p>

                    <p className="mt-1 text-sm font-bold text-gray-900">
                      {isSmoker
                        ? `${detail.smoking_status} smoker`
                        : 'Non-smoker'}
                    </p>
                  </div>
                </div>
              </section>

              {/* Treatment */}
              <section>
                <h3 className="text-xs font-bold uppercase tracking-[0.12em] text-gray-500">
                  Treatment
                </h3>

                {detail.treatment ? (
                  <span className="mt-3 inline-block bg-[#ed3217] px-3 py-1.5 text-sm font-bold text-white">
                    {detail.treatment}
                  </span>
                ) : (
                  <div className="mt-3 border border-gray-400 bg-white px-4 py-4">
                    <p className="text-sm text-gray-600">
                      Treatment details are only visible to signed-in users.
                    </p>

                    <Link
                      to="/login"
                      className="mt-2 inline-block text-sm font-bold text-[#ed3217] hover:underline"
                    >
                      Log in to view treatment →
                    </Link>
                  </div>
                )}
              </section>

              {/* Diagnosis */}
              <section className="border-t border-gray-300 pt-6">
                <h3 className="text-xs font-bold uppercase tracking-[0.12em] text-gray-500">
                  Diagnosis
                </h3>

                <p className="mt-2 text-base font-semibold text-gray-900">
                  {detail.pain_type}
                </p>
              </section>
            </div>
          )}
        </div>

        {/* Footer */}
        {detail && (
          <footer className="border-t border-gray-400 bg-[#f3f1f1] px-8 py-5">
            <button
              type="button"
              onClick={() => onSaveRecord(detail)}
              className="h-12 w-full bg-[#ed3217] text-sm font-bold text-white transition hover:bg-[#d92d15]"
            >
              Save record
            </button>
          </footer>
        )}
      </motion.aside>
    </motion.div>
  )
}

export default DiagnosisDetailPanel