import RecordCard from './RecordCard'
import type { DiagnosisSample } from '../../../api/diagnosisApi'

interface RecordPreviewProps {
  sample?: DiagnosisSample[]
}

const RecordPreview = ({ sample }: RecordPreviewProps) => {
  return (
    <section className="bg-[#f5f3f3] px-10 py-10">
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-base font-extrabold uppercase tracking-wide text-gray-900">
          A Slice of the Set
        </h2>

        <a
          href="/registry"
          className="text-sm font-bold text-[#ed3217] hover:underline"
        >
          See all records →
        </a>
      </div>

      <div className="grid grid-cols-3 gap-5">
        {sample?.map((record, index) => (
          <RecordCard
            key={`${record.gender}-${record.age}-${index}`}
            index={index + 1}
            gender={record.gender}
            age={record.age}
            diagnosis={record.pain_type}
          />
        ))}
      </div>
    </section>
  )
}

export default RecordPreview
