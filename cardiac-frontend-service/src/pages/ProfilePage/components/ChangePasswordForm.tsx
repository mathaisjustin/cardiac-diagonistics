import { useState } from 'react'

import AuthInput from '../../../components/Auth/AuthInput'
import { useToast } from '../../../components/Toast/ToastContext'
import { changePassword } from '../../../api/authApi'
import { ApiError } from '../../../api/httpClient'

const ChangePasswordForm = () => {
  const { showToast } = useToast()

  const [oldPassword, setOldPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  const [status, setStatus] = useState<'idle' | 'saving' | 'success'>('idle')
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)

    if (newPassword !== confirmPassword) {
      setError('New password and confirmation do not match.')
      return
    }

    setStatus('saving')

    try {
      await changePassword({ oldPassword, newPassword })
      setStatus('success')
      setOldPassword('')
      setNewPassword('')
      setConfirmPassword('')
      showToast('Password changed')
    } catch (err) {
      setStatus('idle')
      setError(err instanceof ApiError ? err.message : 'Unable to change password.')
    }
  }

  return (
    <div className="mt-10 border-t-2 border-gray-900 pt-8">
      <h2 className="text-2xl font-extrabold text-gray-900">Change password</h2>

      <p className="mt-1 text-sm text-gray-500">
        Handled by the auth service - unrelated to your profile details above.
      </p>

      <form
        className="mt-6 grid grid-cols-1 gap-x-8 gap-y-6 sm:grid-cols-2"
        onSubmit={handleSubmit}
      >
        <div className="sm:col-span-2">
          <AuthInput
            label="Current Password"
            name="oldPassword"
            type="password"
            value={oldPassword}
            onChange={(event) => {
              setOldPassword(event.target.value)
              setStatus('idle')
            }}
            required
          />
        </div>

        <AuthInput
          label="New Password"
          name="newPassword"
          type="password"
          value={newPassword}
          onChange={(event) => {
            setNewPassword(event.target.value)
            setStatus('idle')
          }}
          required
        />

        <AuthInput
          label="Confirm New Password"
          name="confirmPassword"
          type="password"
          value={confirmPassword}
          onChange={(event) => {
            setConfirmPassword(event.target.value)
            setStatus('idle')
          }}
          required
        />

        {error && <p className="sm:col-span-2 text-sm text-[#ed3217]">{error}</p>}

        {status === 'success' && (
          <p className="sm:col-span-2 text-sm font-semibold text-green-700">
            Password changed successfully.
          </p>
        )}

        <div className="sm:col-span-2">
          <button
            type="submit"
            disabled={status === 'saving'}
            className="h-12 bg-[#211f1f] px-8 text-sm font-bold text-white transition hover:bg-black disabled:cursor-not-allowed disabled:opacity-60"
          >
            {status === 'saving' ? 'Updating…' : 'Update password'}
          </button>
        </div>
      </form>
    </div>
  )
}

export default ChangePasswordForm
