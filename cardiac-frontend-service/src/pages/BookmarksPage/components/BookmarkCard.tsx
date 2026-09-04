import type { Bookmark } from '../../../api/bookmarksApi'
import TreatmentBadge from './TreatmentBadge'

const formatSavedDate = (iso: string) =>
  new Intl.DateTimeFormat('en-US', { day: 'numeric', month: 'short' }).format(
    new Date(iso),
  )

interface BookmarkCardProps {
  bookmark: Bookmark
  positionLabel: string
  onRemove: () => void
}

const BookmarkCard = ({ bookmark, positionLabel, onRemove }: BookmarkCardProps) => {
  return (
    <article className="border border-gray-500 bg-[#f8f6f6] px-5 py-5">
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold tracking-[0.15em] text-gray-500">
          {positionLabel}
        </span>

        <span className="text-xl text-[#ed3217]">★</span>
      </div>

      <h3 className="mt-6 text-2xl font-extrabold text-gray-900">
        {bookmark.gender} · {bookmark.age}
      </h3>

      <p className="mt-1 text-sm text-gray-600">
        {bookmark.painType} · {bookmark.bp}
      </p>

      <div className="mt-5 flex items-center justify-between">
        <TreatmentBadge treatment={bookmark.treatment} />

        <button
          type="button"
          onClick={onRemove}
          className="text-sm font-bold text-[#ed3217] hover:underline"
        >
          Remove
        </button>
      </div>

      <p className="mt-4 text-xs text-gray-500">
        Saved {formatSavedDate(bookmark.createdAt)}
      </p>
    </article>
  )
}

export default BookmarkCard
