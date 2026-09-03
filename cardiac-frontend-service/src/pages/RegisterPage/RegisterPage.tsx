import RegisterForm from './components/RegisterForm'

const RegisterPage = () => {
  return (
    <div>
      {/* Header */}
      <div>
        <p className="text-sm font-bold uppercase tracking-[0.15em] text-[#ed3217]">
          Create Account
        </p>

        <h1 className="mt-4 text-5xl font-extrabold tracking-tight text-gray-900">
          Register
        </h1>

        <p className="mt-2 text-base text-gray-600">
          You'll be sent to log in once the account exists.
        </p>
      </div>

      {/* Divider */}
      <div className="my-9 border-t-2 border-gray-800" />

      {/* Form */}
      <RegisterForm />
    </div>
  )
}

export default RegisterPage