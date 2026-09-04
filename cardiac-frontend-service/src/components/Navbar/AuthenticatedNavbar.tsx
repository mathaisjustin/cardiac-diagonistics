import { Link } from 'react-router-dom'

import { useCurrentUser } from '../../hooks/useCurrentUser'

const AuthenticatedNavbar = () => {
  const { firstName, lastName } = useCurrentUser()

  const initials =
    firstName && lastName
      ? `${firstName[0]}${lastName[0]}`.toUpperCase()
      : '…'

  return (
    <nav className="w-full border-b border-gray-300 bg-[#f3f1f1]">
      <div className="flex h-[88px] items-center justify-between px-10">
        <Link
          to="/"
          className="text-2xl font-extrabold tracking-wide text-gray-900"
        >
          DIAGNOSIS.REGISTRY
        </Link>

        <div className="flex items-center gap-3">
          <Link
            to="/registry"
            className="border border-gray-500 bg-white px-5 py-3 text-base font-bold text-gray-900 transition-colors hover:bg-gray-100"
          >
            Registry
          </Link>

          <Link
            to="/analysis"
            className="border border-gray-500 bg-white px-5 py-3 text-base font-bold text-gray-900 transition-colors hover:bg-gray-100"
          >
            Analysis
          </Link>

          <Link
            to="/bookmarks"
            className="flex items-center gap-2 border border-gray-500 bg-white px-5 py-3 text-base font-bold text-gray-900 transition-colors hover:bg-gray-100"
          >
            <span>Bookmarks</span>
          </Link>

          <Link
            to="/profile"
            className="flex h-[48px] w-[48px] items-center justify-center bg-[#ed3217] text-sm font-extrabold text-white transition-colors hover:bg-[#d92d15]"
          >
            {initials}
          </Link>
        </div>
      </div>
    </nav>
  )
}

export default AuthenticatedNavbar
