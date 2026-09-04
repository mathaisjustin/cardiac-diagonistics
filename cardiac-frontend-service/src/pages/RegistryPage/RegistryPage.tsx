import GuestRegistry from './GuestRegistry/GuestRegistry'
import AuthenticatedRegistry from './AuthenticatedRegistry/AuthenticatedRegistry'
import { useAppSelector } from '../../app/hooks'

const RegistryPage = () => {
  const loggedIn = useAppSelector((state) => Boolean(state.auth.accessToken))

  if (loggedIn) {
    return <AuthenticatedRegistry />
  }

  return <GuestRegistry />
}

export default RegistryPage
