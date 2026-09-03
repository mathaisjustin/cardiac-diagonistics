interface RecordCardProps {
  gender: string
  age: number
  diagnosis: string
  bloodPressure: string
}

const RecordCard = ({
  gender,
  age,
  diagnosis,
  bloodPressure,
}: RecordCardProps) => {
  return (
    <article className="border border-gray-500 bg-[#f8f6f6] px-4 py-4">
      {/* Patient */}
      <h3 className="text-xl font-extrabold text-gray-900">
        {gender} · {age}
      </h3>

      {/* Diagnosis and blood pressure */}
      <p className="mt-1 text-xs text-gray-700">
        {bloodPressure} · {diagnosis}
      </p>

      {/* Bookmark */}
      <div className="mt-4">
        <button
          type="button"
          className="flex items-center gap-2 border border-gray-500 bg-transparent px-3 py-1.5 text-xs font-bold text-gray-900 transition-colors hover:bg-gray-100"
        >
          <span className="text-sm">☆</span>
          <span>Bookmark</span>
        </button>
      </div>
    </article>
  )
}

export default RecordCard