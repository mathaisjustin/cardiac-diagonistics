import { useEffect, useState } from 'react'

import AuthenticatedNavbar from '../../components/Navbar/AuthenticatedNavbar'
import { useToast } from '../../components/Toast/ToastContext'
import { useAppDispatch, useAppSelector } from '../../app/hooks'
import { loggedOut } from '../../features/auth/authSlice'

import ProfileForm, { type ProfileFormValues } from './components/ProfileForm'
import AccountSidebar from './components/AccountSidebar'
import ChangePasswordForm from './components/ChangePasswordForm'

import { getProfile, updateProfile, type Profile } from '../../api/profileApi'
import { logout as logoutRequest } from '../../api/authApi'
import { getBookmarks } from '../../api/bookmarksApi'
import { ApiError } from '../../api/httpClient'
import { getJwtExpiryMs } from '../../utils/jwt'

const emptyValues: ProfileFormValues = { firstName: '', lastName: '', contact: '' }

const formatMemberSince = (iso: string) =>
  new Intl.DateTimeFormat('en-US', { month: 'short', year: 'numeric' }).format(
    new Date(iso),
  )

const ProfilePage = () => {
  const { showToast } = useToast()

  const accessToken = useAppSelector((state) => state.auth.accessToken)
  const refreshToken = useAppSelector((state) => state.auth.refreshToken)
  const dispatch = useAppDispatch()

  const [profile, setProfile] = useState<Profile | null>(null)
  const [values, setValues] = useState<ProfileFormValues>(emptyValues)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  const [savedRecordsCount, setSavedRecordsCount] = useState<number | null>(null)
  const [sessionMinutesLeft, setSessionMinutesLeft] = useState<number | null>(null)

  useEffect(() => {
    let cancelled = false

    getProfile()
      .then((data) => {
        if (cancelled) return
        setProfile(data)
        setValues({
          firstName: data.firstName,
          lastName: data.lastName,
          contact: data.contact,
        })
      })
      .catch(() => {
        if (!cancelled) setError('Unable to load your profile.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    getBookmarks()
      .then((data) => {
        if (!cancelled) setSavedRecordsCount(data.length)
      })
      .catch(() => {
        // Non-critical stat - fail silently.
      })

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    if (!accessToken) return

    const expiryMs = getJwtExpiryMs(accessToken)
    if (expiryMs === null) return

    const update = () => {
      const minutesLeft = Math.max(0, Math.round((expiryMs - Date.now()) / 60000))
      setSessionMinutesLeft(minutesLeft)
    }

    update()
    const interval = setInterval(update, 30_000)
    return () => clearInterval(interval)
  }, [accessToken])

  const dirty =
    profile !== null &&
    (values.firstName !== profile.firstName ||
      values.lastName !== profile.lastName ||
      values.contact !== profile.contact)

  const handleChange = (field: keyof ProfileFormValues, value: string) => {
    setValues((current) => ({ ...current, [field]: value }))
  }

  const handleDiscard = () => {
    if (!profile) return
    setValues({
      firstName: profile.firstName,
      lastName: profile.lastName,
      contact: profile.contact,
    })
    setError(null)
  }

  const handleSave = async () => {
    if (!profile) return

    setSaving(true)
    setError(null)

    try {
      const updated = await updateProfile({
        firstName: values.firstName,
        lastName: values.lastName,
        contact: values.contact,
        department: profile.department,
      })
      setProfile(updated)
      setValues({
        firstName: updated.firstName,
        lastName: updated.lastName,
        contact: updated.contact,
      })
      showToast('Profile updated')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to save changes.')
    } finally {
      setSaving(false)
    }
  }

  const handleLogout = () => {
    if (refreshToken) {
      logoutRequest(refreshToken).catch(() => {
        // Best effort - the client-side logout below always proceeds.
      })
    }
    // Just clear the token - the `isAuthenticated` guard above re-renders
    // this page as <Navigate to="/login"> on its own. A second, manual
    // navigate() call here races that guard's own redirect and loses.
    dispatch(loggedOut())
  }

  return (
    <div className="min-h-screen bg-[#f5f3f3]">
      <AuthenticatedNavbar />

      <main className="px-10 py-8">
        {loading && <p className="text-sm text-gray-500">Loading…</p>}

        {!loading && profile && (
          <div className="flex flex-col gap-8 lg:flex-row lg:items-start">
            <div className="flex-1">
              <ProfileForm
                values={values}
                email={profile.email}
                dirty={dirty}
                saving={saving}
                error={error}
                onChange={handleChange}
                onSave={handleSave}
                onDiscard={handleDiscard}
              />

              <ChangePasswordForm />
            </div>

            <AccountSidebar
              savedRecords={savedRecordsCount}
              memberSince={formatMemberSince(profile.createdAt)}
              sessionMinutesLeft={sessionMinutesLeft}
              onLogout={handleLogout}
            />
          </div>
        )}
      </main>
    </div>
  )
}

export default ProfilePage
