import type { ReactNode } from 'react'
import { motion } from 'framer-motion'
import { useNavigate } from 'react-router-dom'

interface SignInPromptModalProps {
  heading?: string
  body: ReactNode
  onClose: () => void
}

const SignInPromptModal = ({
  heading = 'Saved records live in your account',
  body,
  onClose,
}: SignInPromptModalProps) => {
  const navigate = useNavigate()

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.2 }}
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/45 px-4 backdrop-blur-sm"
      onClick={onClose}
    >
      <motion.div
        initial={{ opacity: 0, y: 18, scale: 0.97 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        exit={{ opacity: 0, y: 12, scale: 0.97 }}
        transition={{ duration: 0.25, ease: 'easeOut' }}
        className="w-full max-w-[506px] border border-gray-900 bg-[#f5f3f3]"
        onClick={(event) => event.stopPropagation()}
      >
        {/* Header */}
        <div className="border-b border-gray-900 px-7 py-6">
          <p className="text-xs font-bold uppercase tracking-[0.15em] text-[#ed3217]">
            Sign in required
          </p>

          <h2 className="mt-3 max-w-[390px] text-[27px] font-extrabold leading-[1.05] text-gray-900">
            {heading}
          </h2>
        </div>

        {/* Body */}
        <div className="px-7 py-6">
          <p className="max-w-[430px] text-sm leading-6 text-gray-700">{body}</p>

          {/* Actions */}
          <div className="mt-6 flex items-center gap-3">
            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.97 }}
              type="button"
              onClick={() => navigate('/login')}
              className="h-[47px] w-[172px] bg-[#ed3217] text-left px-4 text-sm font-bold text-white transition-colors hover:bg-[#d92d15]"
            >
              Log in
            </motion.button>

            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.97 }}
              type="button"
              onClick={() => navigate('/register')}
              className="h-[47px] w-[175px] border text-left border-gray-500 bg-transparent px-4 text-sm font-bold text-gray-900 transition-colors hover:bg-gray-100"
            >
              Register
            </motion.button>

            <button
              type="button"
              onClick={onClose}
              className="ml-auto text-sm font-semibold text-gray-600 transition hover:text-gray-900 hover:underline"
            >
              Not now
            </button>
          </div>
        </div>
      </motion.div>
    </motion.div>
  )
}

export default SignInPromptModal
