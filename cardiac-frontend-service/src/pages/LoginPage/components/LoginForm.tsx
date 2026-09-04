import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import AuthButton from '../../../components/Auth/AuthButton'
import AuthInput from '../../../components/Auth/AuthInput'
import { useAppDispatch, useAppSelector } from '../../../app/hooks'
import { loginUser } from '../../../features/auth/authSlice'

const LoginForm = () => {
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const { status, error } = useAppSelector((state) => state.auth)

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const result = await dispatch(loginUser({ email, password }))

    if (loginUser.fulfilled.match(result)) {
      navigate('/registry')
    }
  }

  return (
    <form className="w-full space-y-6" onSubmit={handleSubmit}>
      <AuthInput
        label="Email"
        name="email"
        type="email"
        placeholder="jane@example.com"
        value={email}
        onChange={(event) => setEmail(event.target.value)}
        required
      />

      <AuthInput
        label="Password"
        name="password"
        type="password"
        placeholder="••••••••"
        value={password}
        onChange={(event) => setPassword(event.target.value)}
        required
      />

      {error && <p className="text-sm text-[#ed3217]">{error}</p>}

      <AuthButton disabled={status === 'loading'}>
        {status === 'loading' ? 'Logging in…' : 'Log in'}
      </AuthButton>
    </form>
  )
}

export default LoginForm
