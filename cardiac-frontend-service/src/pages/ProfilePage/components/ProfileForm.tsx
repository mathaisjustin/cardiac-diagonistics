import AuthInput from '../../../components/Auth/AuthInput'

export interface ProfileFormValues {
  firstName: string
  lastName: string
  contact: string
}

interface ProfileFormProps {
  values: ProfileFormValues
  email: string
  dirty: boolean
  saving: boolean
  error: string | null
  onChange: (field: keyof ProfileFormValues, value: string) => void
  onSave: () => void
  onDiscard: () => void
}

const ProfileForm = ({
  values,
  email,
  dirty,
  saving,
  error,
  onChange,
  onSave,
  onDiscard,
}: ProfileFormProps) => {
  return (
    <div>
      <h1 className="text-4xl font-extrabold text-gray-900">Profile</h1>

      <p className="mt-1 text-sm text-gray-500">
        Your details, kept by the profile service.
      </p>

      <div className="mt-6 border-t-2 border-gray-900" />

      <form
        className="mt-8 grid grid-cols-1 gap-x-8 gap-y-6 sm:grid-cols-2"
        onSubmit={(event) => {
          event.preventDefault()
          onSave()
        }}
      >
        <AuthInput
          label="First Name"
          name="firstName"
          value={values.firstName}
          onChange={(event) => onChange('firstName', event.target.value)}
          required
        />

        <AuthInput
          label="Last Name"
          name="lastName"
          value={values.lastName}
          onChange={(event) => onChange('lastName', event.target.value)}
          required
        />

        <AuthInput
          label="Phone"
          name="contact"
          type="tel"
          value={values.contact}
          onChange={(event) => onChange('contact', event.target.value)}
          required
        />

        <div>
          <label
            htmlFor="email"
            className="mb-2 block text-sm font-bold uppercase tracking-[0.12em] text-gray-600"
          >
            Email
          </label>

          <input
            id="email"
            value={email}
            disabled
            className="h-14 w-full cursor-not-allowed border border-gray-400 bg-gray-100 px-5 text-base text-gray-500 outline-none"
          />

          <p className="mt-2 text-sm text-gray-500">
            Email is your sign-in and can't be changed here.
          </p>
        </div>

        {error && <p className="sm:col-span-2 text-sm text-[#ed3217]">{error}</p>}

        <div className="flex items-center gap-3 sm:col-span-2">
          <button
            type="submit"
            disabled={!dirty || saving}
            className="h-12 bg-[#ed3217] px-8 text-sm font-bold text-white transition hover:bg-[#d92d15] disabled:cursor-not-allowed disabled:opacity-60"
          >
            {saving ? 'Saving…' : 'Save changes'}
          </button>

          <button
            type="button"
            onClick={onDiscard}
            disabled={!dirty || saving}
            className="h-12 border border-gray-500 bg-transparent px-8 text-sm font-bold text-gray-900 transition hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-60"
          >
            Discard
          </button>
        </div>
      </form>
    </div>
  )
}

export default ProfileForm
