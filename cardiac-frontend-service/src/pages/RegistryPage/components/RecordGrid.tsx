import type { RegistryRecord } from './registryTypes'
import RecordCard from './RecordCard'

interface RecordGridProps {
  records: RegistryRecord[]
}

const RecordGrid = ({ records }: RecordGridProps) => {
  return (
    <div className="grid grid-cols-3 gap-3">
      {records.map((record) => (
        <RecordCard
          key={record.id}
          gender={record.gender}
          age={record.age}
          diagnosis={record.diagnosis}
          bloodPressure={record.bloodPressure}
        />
      ))}
    </div>
  )
}

export default RecordGrid