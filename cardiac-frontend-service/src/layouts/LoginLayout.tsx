import { Link, Outlet } from 'react-router-dom'

const LoginLayout = () => {
  return (
    <main className="min-h-screen w-full bg-[#f8f7f7]">
      <div className="grid min-h-screen w-full grid-cols-2">
        {/* Left Panel */}
        <section className="flex min-h-screen flex-col justify-between bg-[#ed3217] p-12 text-white">
          <div>
            <Link
              to="/"
              className="text-lg font-extrabold uppercase tracking-wide"
            >
              Diagnosis.Registry
            </Link>
          </div>

          <div>
            <h1 className="max-w-3xl text-7xl font-extrabold leading-[0.9] tracking-[-0.05em]">
              Search the
              <br />
              whole set. Save
              <br />
              what matters.
            </h1>

            <p className="mt-8 max-w-2xl text-lg font-semibold leading-7">
              Advanced search, treatment analysis and bookmarks unlock
              the moment you sign in.
            </p>
          </div>

          <p className="text-base font-bold uppercase tracking-[0.12em]">
            Sessions last 60 minutes
          </p>
        </section>

        {/* Right Panel */}
        <section className="flex min-h-screen items-center bg-[#f8f7f7] px-16 py-12">
          <div className="w-full max-w-4xl">
            <Outlet />
          </div>
        </section>
      </div>
    </main>
  )
}

export default LoginLayout