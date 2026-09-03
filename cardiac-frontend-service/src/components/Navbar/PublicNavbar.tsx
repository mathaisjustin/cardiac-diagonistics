import { Link } from 'react-router-dom'

const PublicNavbar = () => {
  return (
    <nav className="w-full bg-[#211f1f]">
      <div className="flex h-[88px] items-center justify-between px-10">
        {/* Logo */}
        <Link
          to="/"
          className="text-2xl font-extrabold tracking-wide text-white"
        >
          REGISTRY
        </Link>

        {/* Navigation */}
        <div className="flex h-full items-center gap-4">

          <Link
            to="/registry"
            className="border border-gray-600 px-5 py-3 text-base font-bold text-white transition-colors hover:bg-gray-800"
          >
            Registry
          </Link>

          <Link
            to="/login"
            className="bg-red-600 px-5 py-3 text-base font-bold text-white transition-colors hover:bg-red-700"
          >
            Log in
          </Link>
        </div>
      </div>
    </nav>
  )
}

export default PublicNavbar