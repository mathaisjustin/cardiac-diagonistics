import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import AuthButton from '../../../components/Auth/AuthButton'
import AuthInput from '../../../components/Auth/AuthInput'
import { useAppDispatch, useAppSelector } from '../../../app/hooks'
import { registerStatusReset, registerUser } from '../../../features/auth/authSlice'

const initialFormState = {
  firstName: '',
  lastName: '',
  email: '',
  contactNumber: '',
  department: '',
  password: '',
}

const RegisterForm = () => {
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const { registerStatus, registerError } = useAppSelector((state) => state.auth)

  const [form, setForm] = useState(initialFormState)

  const handleChange =
    (field: keyof typeof initialFormState) =>
    (event: React.ChangeEvent<HTMLInputElement>) => {
      setForm((prev) => ({ ...prev, [field]: event.target.value }))
    }

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const result = await dispatch(registerUser(form))

    if (registerUser.fulfilled.match(result)) {
      dispatch(registerStatusReset())
      navigate('/login')
    }
  }

  return (
    <form className="grid grid-cols-2 gap-x-8 gap-y-7" onSubmit={handleSubmit}>
      {/* First Name */}
      <AuthInput
        label="First Name"
        name="firstName"
        type="text"
        placeholder="Jane"
        value={form.firstName}
        onChange={handleChange('firstName')}
        required
      />

      {/* Last Name */}
      <AuthInput
        label="Last Name"
        name="lastName"
        type="text"
        placeholder="Required"
        value={form.lastName}
        onChange={handleChange('lastName')}
        required
      />

      {/* Email */}
      <AuthInput
        label="Email"
        name="email"
        type="email"
        placeholder="jane@example.com"
        value={form.email}
        onChange={handleChange('email')}
        required
      />

      {/* Phone */}
      <AuthInput
        label="Phone"
        name="contactNumber"
        type="tel"
        placeholder="555-0100"
        value={form.contactNumber}
        onChange={handleChange('contactNumber')}
        required
      />

      {/* Department */}
      <AuthInput
        label="Department"
        name="department"
        type="text"
        placeholder="Cardiology"
        value={form.department}
        onChange={handleChange('department')}
        required
      />

      {/* Password */}
      <div>
        <AuthInput
          label="Password"
          name="password"
          type="password"
          placeholder="••••••••••"
          value={form.password}
          onChange={handleChange('password')}
          required
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

      {registerError && (
        <div className="col-span-2 text-sm text-[#ed3217]">{registerError}</div>
      )}

      {/* Buttons */}
      <div className="col-span-2 flex items-center gap-3">
        <div className="w-52">
          <AuthButton disabled={registerStatus === 'loading'}>
            {registerStatus === 'loading' ? 'Creating…' : 'Create account'}
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
