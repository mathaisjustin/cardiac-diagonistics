import { useEffect, useState } from 'react'

import HeroSection from './components/HeroSection'
import RecordPreview from './components/RecordPreview'
import StatisticsSection from './components/StatisticsSection'
import { getPublicStats, type DiagnosisStats } from '../../api/diagnosisApi'

const LandingPage = () => {
  const [stats, setStats] = useState<DiagnosisStats | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    getPublicStats()
      .then((data) => {
        if (!cancelled) setStats(data)
      })
      .catch(() => {
        if (!cancelled) setError('Unable to load live registry data.')
      })

    return () => {
      cancelled = true
    }
  }, [])

  return (
    <main>
      <HeroSection totalRecords={stats?.totalRecords} />

      <StatisticsSection stats={stats} error={error} />

      <RecordPreview sample={stats?.sample} />
    </main>
  )
}

export default LandingPage
