import type { Bookmark } from '../../../api/bookmarksApi'
import BookmarkCard from './BookmarkCard'

interface BookmarkCardsGridProps {
  bookmarks: Bookmark[]
  onRemove: (bookmark: Bookmark) => void
}

const BookmarkCardsGrid = ({ bookmarks, onRemove }: BookmarkCardsGridProps) => {
  return (
    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
      {bookmarks.map((bookmark, index) => (
        <BookmarkCard
          key={bookmark.id}
          bookmark={bookmark}
          positionLabel={String(index + 1).padStart(3, '0')}
          onRemove={() => onRemove(bookmark)}
        />
      ))}
    </div>
  )
}

export default BookmarkCardsGrid
