interface RegistrySearchProps {
  value: string
  onChange: (value: string) => void
}

const RegistrySearch = ({ value, onChange }: RegistrySearchProps) => {
  return (
    <div className="w-full">
      <input
        type="text"
        placeholder="Search records..."
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="h-12 w-full border border-gray-500 bg-white px-4 text-sm text-gray-900 outline-none placeholder:text-gray-400 focus:border-gray-900"
      />
    </div>
  )
}

export default RegistrySearch
