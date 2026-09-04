import { useEffect, useMemo, useState } from 'react'
import { AnimatePresence } from 'framer-motion'

import AuthenticatedNavbar from '../../components/Navbar/AuthenticatedNavbar'
import { useToast } from '../../components/Toast/ToastContext'

import BookmarksHeader, { type BookmarksView } from './components/BookmarksHeader'
import BookmarkCardsGrid from './components/BookmarkCardsGrid'
import BookmarkTableList, { type SortOrder } from './components/BookmarkTableList'
import BookmarkDetailPane from './components/BookmarkDetailPane'
import UndoBanner from './components/UndoBanner'

import DiagnosisDetailPanel from '../RegistryPage/components/DiagnosisDetailPanel'

import { getBookmarks, deleteBookmark, type Bookmark } from '../../api/bookmarksApi'
import { bookmarkDiagnosis } from '../../api/diagnosisApi'

interface RemovedBookmark {
  bookmark: Bookmark
  positionLabel: string
}

const BookmarksPage = () => {
  const { showToast } = useToast()

  const [bookmarks, setBookmarks] = useState<Bookmark[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [view, setView] = useState<BookmarksView>('cards')
  const [sortOrder, setSortOrder] = useState<SortOrder>('newest')
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [removed, setRemoved] = useState<RemovedBookmark | null>(null)
  const [openDiagnosisId, setOpenDiagnosisId] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    getBookmarks()
      .then((data) => {
        if (!cancelled) setBookmarks(data)
      })
      .catch(() => {
        if (!cancelled) setError('Unable to load your saved records.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const sortedBookmarks = useMemo(() => {
    const sorted = [...bookmarks].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
    )
    return sortOrder === 'newest' ? sorted : sorted.reverse()
  }, [bookmarks, sortOrder])

  const positionLabels = useMemo(() => {
    const map = new Map<string, string>()
    sortedBookmarks.forEach((bookmark, index) => {
      map.set(bookmark.id, String(index + 1).padStart(3, '0'))
    })
    return map
  }, [sortedBookmarks])

  useEffect(() => {
    if (sortedBookmarks.length === 0) return

    const stillExists = sortedBookmarks.some((item) => item.id === selectedId)

    if (!selectedId || !stillExists) {
      setSelectedId(sortedBookmarks[0].id)
    }
  }, [selectedId, sortedBookmarks])

  const handleRemove = (bookmark: Bookmark) => {
    setRemoved({
      bookmark,
      positionLabel: positionLabels.get(bookmark.id) ?? '',
    })
    setBookmarks((current) => current.filter((item) => item.id !== bookmark.id))

    deleteBookmark(bookmark.id).catch(() => {
      setError('Unable to remove that record. Please try again.')
      setBookmarks((current) => [...current, bookmark])
      setRemoved(null)
    })
  }

  const handleUndo = () => {
    if (!removed) return

    const { bookmark } = removed
    setBookmarks((current) => [bookmark, ...current])
    setRemoved(null)

    bookmarkDiagnosis(bookmark.diagnosisId).catch(() => {
      // Best effort - the record stays visible locally either way.
    })
  }

  const selectedBookmark = sortedBookmarks.find((item) => item.id === selectedId) ?? null

  return (
    <div className="min-h-screen bg-[#f5f3f3]">
      <AuthenticatedNavbar />

      <BookmarksHeader view={view} onViewChange={setView} />

      <main className="px-10 pb-10">
        {loading && <p className="text-sm text-gray-500">Loading…</p>}

        {error && <p className="mb-4 text-sm text-[#ed3217]">{error}</p>}

        {!loading && !error && sortedBookmarks.length === 0 && !removed && (
          <p className="py-16 text-center text-sm text-gray-500">
            You haven't saved any records yet.
          </p>
        )}

        {!loading && sortedBookmarks.length > 0 && view === 'cards' && (
          <BookmarkCardsGrid bookmarks={sortedBookmarks} onRemove={handleRemove} />
        )}

        {!loading && sortedBookmarks.length > 0 && view === 'table' && (
          <div className="flex border border-gray-400 bg-white">
            <BookmarkTableList
              bookmarks={sortedBookmarks}
              positionLabels={positionLabels}
              selectedId={selectedId}
              onSelect={(bookmark) => setSelectedId(bookmark.id)}
              onRemove={handleRemove}
              sortOrder={sortOrder}
              onSortOrderChange={setSortOrder}
            />

            {selectedBookmark && (
              <BookmarkDetailPane
                bookmark={selectedBookmark}
                positionLabel={positionLabels.get(selectedBookmark.id) ?? ''}
                onOpenLiveRecord={() => setOpenDiagnosisId(selectedBookmark.diagnosisId)}
                onRemove={() => handleRemove(selectedBookmark)}
              />
            )}
          </div>
        )}

        {removed && (
          <div className="mt-5">
            <UndoBanner positionLabel={removed.positionLabel} onUndo={handleUndo} />
          </div>
        )}
      </main>

      <AnimatePresence>
        {openDiagnosisId && (
          <DiagnosisDetailPanel
            recordId={openDiagnosisId}
            positionLabel={positionLabels.get(selectedId ?? '') ?? ''}
            onClose={() => setOpenDiagnosisId(null)}
            onSaveRecord={() => {
              setOpenDiagnosisId(null)
              showToast('Record bookmarked')
            }}
          />
        )}
      </AnimatePresence>
    </div>
  )
}

export default BookmarksPage
