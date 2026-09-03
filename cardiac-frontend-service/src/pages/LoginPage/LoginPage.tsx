import { Link } from 'react-router-dom'

import LoginForm from './components/LoginForm'

const LoginPage = () => {
  return (
    <div>
      <div>
        <h2 className="text-5xl font-extrabold tracking-tight text-gray-900">
          Log in
        </h2>

        <p className="mt-2 text-base text-gray-600">
          No account?{' '}
          <Link
            to="/register"
            className="font-semibold text-[#ed3217] hover:underline"
          >
            Register instead
          </Link>
        </p>
      </div>

      <div className="mt-10">
        <LoginForm />
      </div>
    </div>
  )
}

export default LoginPage