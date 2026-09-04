interface UndoBannerProps {
  positionLabel: string
  onUndo: () => void
}

const UndoBanner = ({ positionLabel, onUndo }: UndoBannerProps) => {
  return (
    <div className="border border-gray-400 bg-[#e9e7e7] px-6 py-4 text-sm text-gray-700">
      Removed record {positionLabel}.{' '}
      <button
        type="button"
        onClick={onUndo}
        className="font-bold text-[#ed3217] hover:underline"
      >
        Undo
      </button>
    </div>
  )
}

export default UndoBanner
