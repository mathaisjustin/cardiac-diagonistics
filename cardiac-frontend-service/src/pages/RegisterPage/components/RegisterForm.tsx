import { useNavigate } from 'react-router-dom'

import AuthButton from '../../../components/Auth/AuthButton'
import AuthInput from '../../../components/Auth/AuthInput'

const RegisterForm = () => {
  const navigate = useNavigate()

  return (
    <form className="grid grid-cols-2 gap-x-8 gap-y-7">
      {/* First Name */}
      <AuthInput
        label="First Name"
        name="firstName"
        type="text"
        placeholder="Jane"
      />

      {/* Last Name */}
      <AuthInput
        label="Last Name"
        name="lastName"
        type="text"
        placeholder="Required"
      />

      {/* Email */}
      <AuthInput
        label="Email"
        name="email"
        type="email"
        placeholder="jane@example.com"
      />

      {/* Phone */}
      <AuthInput
        label="Phone"
        name="phone"
        type="tel"
        placeholder="555-0100"
      />

      {/* Department */}
      <AuthInput
        label="Department"
        name="department"
        type="text"
        placeholder="Cardiology"
      />

      {/* Password */}
      <div>
        <AuthInput
          label="Password"
          name="password"
          type="password"
          placeholder="••••••••••"
        />

        <p className="mt-2 text-sm text-gray-500">
          8–72 characters, at least one letter and one number.
        </p>
      </div>

      {/* Information */}
      <div className="col-span-2">
        <div className="border border-gray-200 bg-gray-100 px-4 py-4 text-sm text-gray-600">
          Registering doesn't sign you in — accounts and sessions are
          separate steps.
        </div>
      </div>

      {/* Buttons */}
      <div className="col-span-2 flex items-center gap-3">
        <div className="w-52">
          <AuthButton>
            Create account
          </AuthButton>
        </div>

        <button
          type="button"
          onClick={() => navigate('/login')}
          className="h-14 border border-gray-500 bg-transparent px-8 text-base font-bold text-gray-900 transition hover:bg-gray-100"
        >
          Cancel
        </button>
      </div>
    </form>
  )
}

export default RegisterForm