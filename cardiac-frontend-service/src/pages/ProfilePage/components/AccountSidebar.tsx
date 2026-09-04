interface AccountSidebarProps {
  savedRecords: number | null
  memberSince: string
  sessionMinutesLeft: number | null
  onLogout: () => void
}

const AccountSidebar = ({
  savedRecords,
  memberSince,
  sessionMinutesLeft,
  onLogout,
}: AccountSidebarProps) => {
  return (
    <aside className="w-full max-w-xs shrink-0 border border-gray-400 bg-white px-6 py-6">
      <h2 className="text-xs font-bold uppercase tracking-[0.15em] text-gray-500">
        Account
      </h2>

      <dl className="mt-4 divide-y divide-gray-200">
        <div className="flex items-center justify-between py-3">
          <dt className="text-sm text-[#ed3217]">Saved records</dt>
          <dd className="text-sm font-bold text-gray-900">
            {savedRecords ?? '…'}
          </dd>
        </div>

        <div className="flex items-center justify-between py-3">
          <dt className="text-sm text-[#ed3217]">Member since</dt>
          <dd className="text-sm font-bold text-gray-900">{memberSince}</dd>
        </div>

        <div className="flex items-center justify-between py-3">
          <dt className="text-sm text-gray-700">Session</dt>
          <dd className="text-sm font-bold text-gray-900">
            {sessionMinutesLeft !== null ? `${sessionMinutesLeft} min left` : '…'}
          </dd>
        </div>
      </dl>

      <button
        type="button"
        onClick={onLogout}
        className="mt-5 h-11 w-full border border-[#ed3217] bg-white text-sm font-bold text-[#ed3217] transition hover:bg-[#fbe4de]"
      >
        Log out
      </button>

      <p className="mt-3 text-xs text-gray-500">
        Logging out just drops the token from this browser.
      </p>
    </aside>
  )
}

export default AccountSidebar
