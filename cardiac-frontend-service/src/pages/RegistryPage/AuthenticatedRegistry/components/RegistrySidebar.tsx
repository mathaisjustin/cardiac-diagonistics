import type { RegistryFilters } from '../../components/registryTypes'

import AdvancedFilters from './AdvancedFilters'

interface RegistrySidebarProps {
  onSearch: (filters: RegistryFilters) => void
  onReset: () => void
}

const RegistrySidebar = ({
  onSearch,
  onReset,
}: RegistrySidebarProps) => {
  return (
    <aside className="w-[330px] shrink-0 border-r border-gray-700 bg-[#e9e7e7] px-7 py-7">
      <h2 className="text-sm font-extrabold uppercase text-gray-900">
        Advanced Search
      </h2>

      <div className="mt-6">
        <AdvancedFilters
          onSearch={onSearch}
          onReset={onReset}
        />
      </div>
    </aside>
  )
}

export default RegistrySidebar