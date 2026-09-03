const HeroSection = () => {
  return (
    <section className="bg-[#ed3217] px-10 py-20 text-white">
      <p className="text-sm font-bold uppercase tracking-[0.15em]">
        Cardiac Diagnosis Registry
      </p>

      <h1 className="mt-8 max-w-4xl text-7xl font-extrabold leading-[0.9] tracking-[-0.05em]">
        Read the data
        <br />
        before you read
        <br />
        the conclusion.
      </h1>

      <div className="mt-10 flex gap-4">
        <button className="bg-white px-7 py-4 text-base font-bold text-gray-900 transition hover:bg-gray-100">
          Browse 1,024 records
        </button>

        <button className="border border-white px-7 py-4 text-base font-bold text-white transition hover:bg-white/10">
          How the data is sourced
        </button>
      </div>
    </section>
  )
}

export default HeroSection