import { Link } from 'react-router-dom'

const AccountPrompt = () => {
  return (
    <section className="mx-8 mb-8 border border-gray-500 bg-[#fff4f0] px-6 py-5">
      <div className="flex items-center justify-between gap-8">
        <div>
          <h3 className="text-base font-bold text-gray-900">
            Advanced search and treatment analysis need an account
          </h3>

          <p className="mt-1 text-sm text-gray-600">
            Filtering here happens in your browser. Register to search
            the full dataset across pain type, age, BP and gender.
          </p>
        </div>

        <Link
          to="/register"
          className="shrink-0 bg-[#ed3217] px-6 py-3 text-sm font-bold text-white transition hover:bg-[#d92d15]"
        >
          Create account
        </Link>
      </div>
    </section>
  )
}

export default AccountPrompt