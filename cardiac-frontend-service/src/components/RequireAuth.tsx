import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'

import { useAppSelector } from '../app/hooks'

interface RequireAuthProps {
  children: ReactNode
}

// Guards a protected route: bounces a logged-out visitor to the registry
// (not /login directly) carrying enough router state for GuestRegistry to
// pop the sign-in prompt itself, naming the page that needed an account.
// This also covers the session-expired case for free - if a refresh fails
// while the user is sitting on a protected page, `isAuthenticated` flips to
// false and this re-renders as the same redirect.
const RequireAuth = ({ children }: RequireAuthProps) => {
  const isAuthenticated = useAppSelector((state) => Boolean(state.auth.accessToken))
  const location = useLocation()

  if (!isAuthenticated) {
    return (
      <Navigate
        to="/registry"
        replace
        state={{ authRequiredFrom: location.pathname }}
      />
    )
  }

  return <>{children}</>
}

export default RequireAuth
