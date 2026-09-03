import AuthButton from '../../../components/Auth/AuthButton'
import AuthInput from '../../../components/Auth/AuthInput'

const LoginForm = () => {
  return (
    <form className="w-full space-y-6">
      <AuthInput
        label="Email"
        name="email"
        type="email"
        placeholder="jane@example.com"
      />

      <AuthInput
        label="Password"
        name="password"
        type="password"
        placeholder="••••••••"
      />

      <AuthButton>
        Log in
      </AuthButton>
    </form>
  )
}

export default LoginForm