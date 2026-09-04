import { Link } from 'react-router-dom'

import { useAppSelector } from '../../app/hooks'
import { useCurrentUser } from '../../hooks/useCurrentUser'

const PublicNavbar = () => {
  const isAuthenticated = useAppSelector((state) => Boolean(state.auth.accessToken))
  const { firstName, lastName } = useCurrentUser()

  const initials =
    firstName && lastName
      ? `${firstName[0]}${lastName[0]}`.toUpperCase()
      : '…'

  return (
    <nav className="w-full bg-[#211f1f]">
      <div className="flex h-[88px] items-center justify-between px-10">
        {/* Logo */}
        <Link
          to="/"
          className="text-2xl font-extrabold tracking-wide text-white"
        >
          DIAGNOSIS.REGISTRY
        </Link>

        {/* Navigation */}
        <div className="flex h-full items-center gap-4">

          <Link
            to="/registry"
            className="border border-gray-600 px-5 py-3 text-base font-bold text-white transition-colors hover:bg-gray-800"
          >
            Registry
          </Link>

          {isAuthenticated ? (
            <Link
              to="/profile"
              className="flex h-[48px] w-[48px] items-center justify-center bg-[#ed3217] text-sm font-extrabold text-white transition-colors hover:bg-[#d92d15]"
            >
              {initials}
            </Link>
          ) : (
            <Link
              to="/login"
              className="bg-red-600 px-5 py-3 text-base font-bold text-white transition-colors hover:bg-red-700"
            >
              Log in
            </Link>
          )}
        </div>
      </div>
    </nav>
  )
}

export default PublicNavbar
