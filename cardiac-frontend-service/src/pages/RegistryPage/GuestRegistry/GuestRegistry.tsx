import GuestRegistryNavbar from '../../../components/Navbar/GuestRegistryNavbar'

import GuestRegistryToolbar from './components/GuestRegistryToolbar'
import AccountPrompt from './components/AccountPrompt'

import RecordGrid from '../components/RecordGrid'
import LoadMoreButton from '../components/LoadMoreButton'

import { mockRecords } from '../components/mockRecords'

const GuestRegistry = () => {
  return (
    <div className="min-h-screen bg-[#f5f3f3]">
      <GuestRegistryNavbar />

      <main>
        <GuestRegistryToolbar />

        <section className="px-8 py-8">
          <div className="mb-6 flex items-center justify-between">
            <h1 className="text-lg font-extrabold uppercase tracking-wide text-gray-900">
              Cardiac Diagnosis Registry
            </h1>

            <p className="text-sm text-gray-500">
              Showing 6 of 1,024
            </p>
          </div>

          <RecordGrid records={mockRecords} />

          <LoadMoreButton />
        </section>

        <AccountPrompt />
      </main>
    </div>
  )
}

export default GuestRegistry