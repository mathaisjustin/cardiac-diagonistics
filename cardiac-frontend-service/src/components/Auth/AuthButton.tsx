interface AuthButtonProps {
  children: React.ReactNode
  type?: 'button' | 'submit'
  disabled?: boolean
}

const AuthButton = ({
  children,
  type = 'submit',
  disabled = false,
}: AuthButtonProps) => {
  return (
    <button
      type={type}
      disabled={disabled}
      className="h-14 w-full bg-[#ed3217] px-5 text-base font-bold text-white transition hover:bg-[#d92d15] disabled:cursor-not-allowed disabled:opacity-60"
    >
      {children}
    </button>
  )
}

export default AuthButton
