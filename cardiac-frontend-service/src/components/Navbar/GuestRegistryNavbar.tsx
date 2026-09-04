import { Link } from 'react-router-dom'

const GuestRegistryNavbar = () => {
  return (
    <nav className="w-full bg-[#f3f1f1] border-b border-gray-300">
      <div className="flex h-[88px] items-center justify-between px-10">
        {/* Logo */}
        <Link
          to="/"
          className="text-2xl font-extrabold tracking-wide text-gray-900"
        >
          DIGNOSIS.REGISTRY
        </Link>

        {/* Navigation */}
        <div className="flex items-center gap-5">
          <Link
            to="/registry"
            className="text-base font-bold text-gray-900"
          >
            Registry
          </Link>

          <Link
            to="/login"
            className="bg-[#ed3217] px-5 py-3 text-base font-bold text-white transition-colors hover:bg-[#d92d15]"
          >
            Log in
          </Link>
        </div>
      </div>
    </nav>
  )
}

export default GuestRegistryNavbar