import { Route, Routes } from 'react-router-dom'

import PublicLayout from '../layouts/PublicLayout'
import LoginLayout from '../layouts/LoginLayout'
import RegisterLayout from '../layouts/RegisterLayout'

import LandingPage from '../pages/LandingPage/LandingPage'
import LoginPage from '../pages/LoginPage/LoginPage'
import RegisterPage from '../pages/RegisterPage/RegisterPage'

import RegistryPage from '../pages/RegistryPage/RegistryPage'

const AppRoutes = () => {
  return (
    <Routes>
      {/* Landing Page */}
      <Route element={<PublicLayout />}>
        <Route path="/" element={<LandingPage />} />
      </Route>

      {/* Login */}
      <Route element={<LoginLayout />}>
        <Route path="/login" element={<LoginPage />} />
      </Route>

      {/* Registration */}
      <Route element={<RegisterLayout />}>
        <Route path="/register" element={<RegisterPage />} />
      </Route>

      {/* Registry */}
      <Route path="/registry" element={<RegistryPage />} />
    </Routes>
  )
}

export default AppRoutes