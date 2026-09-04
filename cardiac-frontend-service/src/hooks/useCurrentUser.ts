import { useEffect, useState } from 'react'

import { useAppSelector } from '../app/hooks'
import { getProfile } from '../api/profileApi'

// Local to whichever navbar instance calls it - deliberately not cached in
// Redux to avoid a profileApi <-> store <-> authSlice import cycle. It's one
// cheap GET per page mount, not worth that risk.
export const useCurrentUser = () => {
  const accessToken = useAppSelector((state) => state.auth.accessToken)
  const [firstName, setFirstName] = useState<string | null>(null)
  const [lastName, setLastName] = useState<string | null>(null)

  useEffect(() => {
    if (!accessToken) {
      setFirstName(null)
      setLastName(null)
      return
    }

    let cancelled = false

    getProfile()
      .then((data) => {
        if (cancelled) return
        setFirstName(data.firstName)
        setLastName(data.lastName)
      })
      .catch(() => {
        // Navbar just falls back to a placeholder - not worth surfacing.
      })

    return () => {
      cancelled = true
    }
  }, [accessToken])

  return { firstName, lastName }
}
