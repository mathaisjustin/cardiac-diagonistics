import type { Bookmark } from '../../../api/bookmarksApi'
import TreatmentBadge from './TreatmentBadge'

const formatSavedDate = (iso: string) =>
  new Intl.DateTimeFormat('en-US', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })
    .format(new Date(iso))
    .toUpperCase()

interface BookmarkDetailPaneProps {
  bookmark: Bookmark
  positionLabel: string
  onOpenLiveRecord: () => void
  onRemove: () => void
}

const BookmarkDetailPane = ({
  bookmark,
  positionLabel,
  onOpenLiveRecord,
  onRemove,
}: BookmarkDetailPaneProps) => {
  return (
    <div className="flex-1 px-10 py-8">
      <p className="text-xs font-bold uppercase tracking-[0.15em] text-[#ed3217]">
        Saved record {positionLabel} · {formatSavedDate(bookmark.createdAt)}
      </p>

      <h2 className="mt-3 text-4xl font-extrabold text-gray-900">
        {bookmark.gender} · {bookmark.age}
      </h2>

      <p className="mt-1 text-base text-gray-600">{bookmark.painType}</p>

      <div className="mt-6 grid grid-cols-2 border border-gray-400">
        <div className="border-r border-gray-400 px-5 py-4">
          <p className="text-xs font-semibold uppercase tracking-wide text-gray-500">
            BP
          </p>
          <p className="mt-2 text-2xl font-extrabold text-gray-900">
            {bookmark.bp}
          </p>
        </div>

        <div className="px-5 py-4">
          <p className="text-xs font-semibold uppercase tracking-wide text-gray-500">
            Treatment
          </p>
          <div className="mt-2">
            <TreatmentBadge treatment={bookmark.treatment} />
          </div>
        </div>
      </div>

      <div className="mt-6 border border-gray-300 bg-[#f3f1f1] px-5 py-4">
        <p className="text-sm text-gray-600">
          Bookmarks keep only the list fields — open the live record for
          cholesterol, diabetic and smoker status.
        </p>
      </div>

      <div className="mt-6 flex items-center gap-3">
        <button
          type="button"
          onClick={onOpenLiveRecord}
          className="h-12 border border-gray-500 bg-white px-6 text-sm font-bold text-gray-900 transition hover:bg-gray-100"
        >
          Open live record
        </button>

        <button
          type="button"
          onClick={onRemove}
          className="h-12 border border-[#ed3217] bg-white px-6 text-sm font-bold text-[#ed3217] transition hover:bg-[#fbe4de]"
        >
          Remove
        </button>
      </div>
    </div>
  )
}

export default BookmarkDetailPane
