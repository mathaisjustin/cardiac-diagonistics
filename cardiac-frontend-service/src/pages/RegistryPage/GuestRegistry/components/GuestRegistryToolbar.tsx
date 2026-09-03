import RegistrySearch from '../../components/RegistrySearch'

const GuestRegistryToolbar = () => {
  return (
    <section className="border-b border-gray-300 bg-[#f5f3f3] px-8 py-5">
      <div className="flex items-center gap-4">
        <RegistrySearch />

        <button
          type="button"
          className="h-12 min-w-[120px] border border-gray-500 bg-white px-4 text-sm font-semibold text-gray-900"
        >
          Pain type ▾
        </button>

        <button
          type="button"
          className="h-12 min-w-[110px] border border-gray-500 bg-white px-4 text-sm font-semibold text-gray-900"
        >
          Gender ▾
        </button>

        <button
          type="button"
          className="h-12 min-w-[100px] border border-gray-500 bg-white px-4 text-sm font-semibold text-gray-900"
        >
          Age ▾
        </button>
      </div>
    </section>
  )
}

export default GuestRegistryToolbar