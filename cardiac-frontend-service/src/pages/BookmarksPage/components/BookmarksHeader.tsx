export type BookmarksView = 'cards' | 'table'

interface BookmarksHeaderProps {
  view: BookmarksView
  onViewChange: (view: BookmarksView) => void
}

const BookmarksHeader = ({ view, onViewChange }: BookmarksHeaderProps) => {
  return (
    <div className="flex items-center justify-between px-10 py-8">
      <h1 className="text-4xl font-extrabold text-gray-900">Saved records</h1>

      <div className="flex">
        <button
          type="button"
          onClick={() => onViewChange('cards')}
          className={`h-11 border border-gray-500 px-5 text-sm font-bold transition ${
            view === 'cards'
              ? 'bg-[#211f1f] text-white'
              : 'bg-white text-gray-900 hover:bg-gray-100'
          }`}
        >
          Cards
        </button>

        <button
          type="button"
          onClick={() => onViewChange('table')}
          className={`h-11 border border-l-0 border-gray-500 px-5 text-sm font-bold transition ${
            view === 'table'
              ? 'bg-[#211f1f] text-white'
              : 'bg-white text-gray-900 hover:bg-gray-100'
          }`}
        >
          Table
        </button>
      </div>
    </div>
  )
}

export default BookmarksHeader
