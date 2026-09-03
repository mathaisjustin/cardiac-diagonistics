import GuestRegistry from './GuestRegistry/GuestRegistry'
import AuthenticatedRegistry from './AuthenticatedRegistry/AuthenticatedRegistry'

const RegistryPage = () => {
  const loggedIn = true // Replace with actual authentication logic

  if (loggedIn) {
    return <AuthenticatedRegistry />
  }

  return <GuestRegistry />
}

export default RegistryPage