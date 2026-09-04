import { useState } from 'react'

import type { Bookmark } from '../../../api/bookmarksApi'

export type SortOrder = 'newest' | 'oldest'

interface BookmarkTableListProps {
  bookmarks: Bookmark[]
  positionLabels: Map<string, string>
  selectedId: string | null
  onSelect: (bookmark: Bookmark) => void
  onRemove: (bookmark: Bookmark) => void
  sortOrder: SortOrder
  onSortOrderChange: (order: SortOrder) => void
}

const BookmarkTableList = ({
  bookmarks,
  positionLabels,
  selectedId,
  onSelect,
  onRemove,
  sortOrder,
  onSortOrderChange,
}: BookmarkTableListProps) => {
  const [sortMenuOpen, setSortMenuOpen] = useState(false)

  return (
    <aside className="flex w-full max-w-sm shrink-0 flex-col border-r border-gray-400">
      <div className="flex items-center justify-between border-b border-gray-300 px-6 py-5">
        <h2 className="text-xl font-extrabold text-gray-900">
          {bookmarks.length} saved
        </h2>

        <div className="relative">
          <button
            type="button"
            onClick={() => setSortMenuOpen((open) => !open)}
            className="text-sm font-semibold text-gray-700 hover:text-gray-900"
          >
            {sortOrder === 'newest' ? 'Newest first' : 'Oldest first'} ▾
          </button>

          {sortMenuOpen && (
            <div className="absolute right-0 z-10 mt-2 w-36 border border-gray-400 bg-white shadow-lg">
              {(['newest', 'oldest'] as const).map((option) => (
                <button
                  key={option}
                  type="button"
                  onClick={() => {
                    onSortOrderChange(option)
                    setSortMenuOpen(false)
                  }}
                  className={`block w-full px-3 py-2 text-left text-sm ${
                    sortOrder === option
                      ? 'bg-gray-100 font-semibold text-gray-900'
                      : 'text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  {option === 'newest' ? 'Newest first' : 'Oldest first'}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      <div className="flex-1 overflow-y-auto">
        {bookmarks.map((bookmark) => {
          const isSelected = bookmark.id === selectedId

          return (
            <button
              key={bookmark.id}
              type="button"
              onClick={() => onSelect(bookmark)}
              className={`flex w-full items-center gap-4 border-b px-6 py-4 text-left transition ${
                isSelected
                  ? 'border-[#ed3217] bg-[#ed3217] text-white'
                  : 'border-gray-200 bg-white text-gray-900 hover:bg-gray-50'
              }`}
            >
              <span
                className={`w-10 shrink-0 text-xs font-semibold ${
                  isSelected ? 'text-white/80' : 'text-gray-500'
                }`}
              >
                {positionLabels.get(bookmark.id)}
              </span>

              <span className="flex-1 truncate text-sm font-bold">
                {bookmark.painType}
              </span>

              <span
                className={`text-sm ${isSelected ? 'text-white/90' : 'text-gray-600'}`}
              >
                {bookmark.age}
              </span>

              <span
                className={`w-16 text-right text-sm ${
                  isSelected ? 'text-white/90' : 'text-gray-600'
                }`}
              >
                {bookmark.bp}
              </span>

              <span
                role="button"
                tabIndex={0}
                onClick={(event) => {
                  event.stopPropagation()
                  onRemove(bookmark)
                }}
                onKeyDown={(event) => {
                  if (event.key === 'Enter' || event.key === ' ') {
                    event.stopPropagation()
                    onRemove(bookmark)
                  }
                }}
                className={`shrink-0 text-sm font-bold hover:underline ${
                  isSelected ? 'text-white' : 'text-[#ed3217]'
                }`}
              >
                Remove
              </span>
            </button>
          )
        })}
      </div>

      <div className="border-t border-gray-300 bg-[#e9e7e7] px-6 py-4 text-xs text-gray-600">
        Snapshots are frozen at save time — if the source record changes, what
        you saved doesn't.
      </div>
    </aside>
  )
}

export default BookmarkTableList
