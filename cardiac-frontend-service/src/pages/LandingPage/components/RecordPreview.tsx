import RecordCard from './RecordCard'

const RecordPreview = () => {
  const records = [
    {
      id: '001',
      gender: 'Male',
      age: 45,
      diagnosis: 'Typical Angina',
      bloodPressure: '130/85',
      treatment: 'Medication' as const,
    },
    {
      id: '002',
      gender: 'Female',
      age: 62,
      diagnosis: 'Atypical Angina',
      bloodPressure: '145/92',
      treatment: 'Surgery' as const,
    },
    {
      id: '003',
      gender: 'Male',
      age: 54,
      diagnosis: 'Non-anginal',
      bloodPressure: '118/76',
      treatment: 'Medication' as const,
    },
  ]

  return (
    <section className="bg-[#f5f3f3] px-10 py-10">
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-base font-extrabold uppercase tracking-wide text-gray-900">
          A Slice of the Set
        </h2>

        <a
          href="#records"
          className="text-sm font-bold text-[#ed3217] hover:underline"
        >
          See all records →
        </a>
      </div>

      <div className="grid grid-cols-3 gap-5">
        {records.map((record) => (
          <RecordCard
            key={record.id}
            {...record}
          />
        ))}
      </div>
    </section>
  )
}

export default RecordPreview