import { motion } from 'framer-motion'

interface PaginationProps {
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
}

const getPageNumbers = (currentPage: number, totalPages: number): (number | 'ellipsis')[] => {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, i) => i + 1)
  }

  const pages = new Set<number>([1, totalPages, currentPage])

  if (currentPage - 1 > 1) pages.add(currentPage - 1)
  if (currentPage + 1 < totalPages) pages.add(currentPage + 1)

  const sorted = [...pages].sort((a, b) => a - b)
  const result: (number | 'ellipsis')[] = []

  sorted.forEach((page, index) => {
    if (index > 0 && page - sorted[index - 1] > 1) {
      result.push('ellipsis')
    }
    result.push(page)
  })

  return result
}

const Pagination = ({ currentPage, totalPages, onPageChange }: PaginationProps) => {
  if (totalPages <= 1) {
    return null
  }

  const pageNumbers = getPageNumbers(currentPage, totalPages)

  return (
    <nav className="flex items-center justify-center gap-2 py-6">
      <motion.button
        whileTap={{ scale: 0.94 }}
        type="button"
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 1}
        className="h-10 border border-gray-500 bg-white px-4 text-sm font-semibold text-gray-900 transition-colors hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-40"
      >
        Previous
      </motion.button>

      {pageNumbers.map((page, index) =>
        page === 'ellipsis' ? (
          <span
            key={`ellipsis-${index}`}
            className="px-2 text-sm text-gray-500"
          >
            …
          </span>
        ) : (
          <motion.button
            key={page}
            whileTap={{ scale: 0.94 }}
            type="button"
            onClick={() => onPageChange(page)}
            aria-current={page === currentPage ? 'page' : undefined}
            className={`relative h-10 w-10 overflow-hidden border text-sm font-semibold transition-colors ${
              page === currentPage
                ? 'border-[#ed3217] text-white'
                : 'border-gray-500 bg-white text-gray-900 hover:bg-gray-100'
            }`}
          >
            {page === currentPage && (
              <motion.span
                layoutId="pagination-active"
                transition={{ type: 'spring', stiffness: 500, damping: 35 }}
                className="absolute inset-0 bg-[#ed3217]"
              />
            )}
            <span className="relative">{page}</span>
          </motion.button>
        ),
      )}

      <motion.button
        whileTap={{ scale: 0.94 }}
        type="button"
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
        className="h-10 border border-gray-500 bg-white px-4 text-sm font-semibold text-gray-900 transition-colors hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-40"
      >
        Next
      </motion.button>
    </nav>
  )
}

export default Pagination
