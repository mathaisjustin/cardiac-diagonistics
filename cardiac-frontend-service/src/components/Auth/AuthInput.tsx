interface AuthInputProps {
  label: string
  type?: string
  placeholder?: string
  name: string
}

const AuthInput = ({
  label,
  type = 'text',
  placeholder,
  name,
}: AuthInputProps) => {
  return (
    <div>
      <label
        htmlFor={name}
        className="mb-2 block text-sm font-bold uppercase tracking-[0.12em] text-gray-600"
      >
        {label}
      </label>

      <input
        id={name}
        name={name}
        type={type}
        placeholder={placeholder}
        className="h-14 w-full border border-gray-500 bg-[#faf9f9] px-5 text-base text-gray-900 outline-none transition focus:border-[#ed3217]"
      />
    </div>
  )
}

export default AuthInput