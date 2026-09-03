interface AuthButtonProps {
  children: React.ReactNode
  type?: 'button' | 'submit'
}

const AuthButton = ({
  children,
  type = 'submit',
}: AuthButtonProps) => {
  return (
    <button
      type={type}
      className="h-14 w-full bg-[#ed3217] px-5 text-base font-bold text-white transition hover:bg-[#d92d15]"
    >
      {children}
    </button>
  )
}

export default AuthButton