interface RecordCardProps {
  id: string
  gender: string
  age: number
  diagnosis: string
  bloodPressure: string
  treatment: 'Medication' | 'Surgery'
}

const RecordCard = ({
  id,
  gender,
  age,
  diagnosis,
  bloodPressure,
  treatment,
}: RecordCardProps) => {
  return (
    <article className="border border-gray-500 bg-[#faf8f8] p-6">
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold tracking-[0.15em] text-gray-500">
          {id}
        </span>

        <span className="text-xl text-gray-400">☆</span>
      </div>

      <h3 className="mt-7 text-3xl font-extrabold text-gray-900">
        {gender} · {age}
      </h3>

      <p className="mt-1 text-base text-gray-600">
        {diagnosis}
      </p>

      <div className="my-5 border-t border-gray-300" />

      <div className="flex items-center justify-between">
        <span className="text-sm font-medium uppercase tracking-wide text-gray-500">
          BP
        </span>

        <span className="text-sm text-gray-900">
          {bloodPressure}
        </span>
      </div>

      <div className="mt-5">
        <span
          className={`inline-block border px-3 py-1 text-sm font-bold ${
            treatment === 'Surgery'
              ? 'border-[#ed3217] bg-[#ed3217] text-white'
              : 'border-gray-500 bg-transparent text-gray-900'
          }`}
        >
          {treatment}
        </span>
      </div>
    </article>
  )
}

export default RecordCard