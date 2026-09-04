import { useEffect } from 'react'

import { useAppDispatch, useAppSelector } from '../../app/hooks'
import { refreshTokens } from './authSlice'
import { getJwtExpiryMs } from '../../utils/jwt'

// Refresh this long before the access token actually expires, so a slow
// request or a slightly-off clock never races an expiry that already
// happened.
const REFRESH_BUFFER_MS = 60_000

// Renders nothing - just keeps the session alive in the background. Mounted
// once near the app root. Whenever the access token changes (on login, or
// after a refresh completes), this reschedules itself against the new
// expiry - so one successful refresh naturally leads to the next.
const AuthSessionManager = () => {
  const accessToken = useAppSelector((state) => state.auth.accessToken)
  const dispatch = useAppDispatch()

  useEffect(() => {
    if (!accessToken) return

    const expiryMs = getJwtExpiryMs(accessToken)
    if (expiryMs === null) return

    const delay = Math.max(0, expiryMs - Date.now() - REFRESH_BUFFER_MS)

    const timer = setTimeout(() => {
      dispatch(refreshTokens())
    }, delay)

    return () => clearTimeout(timer)
  }, [accessToken, dispatch])

  return null
}

export default AuthSessionManager
