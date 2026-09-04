import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'
import { AnimatePresence, motion } from 'framer-motion'

type ToastVariant = 'success' | 'error'

interface Toast {
  id: number
  message: string
  variant: ToastVariant
}

interface ToastContextValue {
  showToast: (message: string, variant?: ToastVariant) => void
}

const ToastContext = createContext<ToastContextValue | null>(null)

const AUTO_DISMISS_MS = 3200

let nextId = 0

export const ToastProvider = ({ children }: { children: ReactNode }) => {
  const [toasts, setToasts] = useState<Toast[]>([])

  const dismiss = useCallback((id: number) => {
    setToasts((current) => current.filter((toast) => toast.id !== id))
  }, [])

  const showToast = useCallback(
    (message: string, variant: ToastVariant = 'success') => {
      const id = nextId++
      setToasts((current) => [...current, { id, message, variant }])
      setTimeout(() => dismiss(id), AUTO_DISMISS_MS)
    },
    [dismiss],
  )

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}

      <div className="fixed bottom-6 right-6 z-[100] flex flex-col items-end gap-2">
        <AnimatePresence>
          {toasts.map((toast) => (
            <motion.div
              key={toast.id}
              layout
              initial={{ opacity: 0, y: 16, scale: 0.95 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, x: 40, transition: { duration: 0.2 } }}
              transition={{ duration: 0.25, ease: 'easeOut' }}
              onClick={() => dismiss(toast.id)}
              className={`flex max-w-sm cursor-pointer items-center gap-3 border px-4 py-3 text-sm font-semibold shadow-lg ${
                toast.variant === 'success'
                  ? 'border-gray-900 bg-[#211f1f] text-white'
                  : 'border-[#ed3217] bg-[#fbe4de] text-[#ed3217]'
              }`}
            >
              <span
                className={`h-2 w-2 shrink-0 rounded-full ${
                  toast.variant === 'success' ? 'bg-[#3ddc84]' : 'bg-[#ed3217]'
                }`}
              />
              {toast.message}
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
    </ToastContext.Provider>
  )
}

export const useToast = () => {
  const context = useContext(ToastContext)

  if (!context) {
    throw new Error('useToast must be used within a ToastProvider')
  }

  return context
}
