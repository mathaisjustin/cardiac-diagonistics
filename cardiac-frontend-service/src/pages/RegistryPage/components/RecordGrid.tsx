import type { RegistryRecord } from './registryTypes'
import RecordCard from './RecordCard'

interface RecordGridProps {
  records: RegistryRecord[]
  onBookmark?: (record: RegistryRecord, index: number) => void
  onSelect?: (record: RegistryRecord, index: number) => void
}

const RecordGrid = ({ records, onBookmark, onSelect }: RecordGridProps) => {
  return (
    <div className="grid grid-cols-3 gap-3">
      {records.map((record, index) => (
        <RecordCard
          key={record.id}
          gender={record.gender}
          age={record.age}
          diagnosis={record.diagnosis}
          bloodPressure={record.bloodPressure}
          delay={Math.min(index, 8) * 0.03}
          onBookmark={onBookmark ? () => onBookmark(record, index) : undefined}
          onSelect={onSelect ? () => onSelect(record, index) : undefined}
        />
      ))}
    </div>
  )
}

export default RecordGrid
