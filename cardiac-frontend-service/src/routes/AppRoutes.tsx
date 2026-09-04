import { Route, Routes } from 'react-router-dom'

import PublicLayout from '../layouts/PublicLayout'
import LoginLayout from '../layouts/LoginLayout'
import RegisterLayout from '../layouts/RegisterLayout'

import LandingPage from '../pages/LandingPage/LandingPage'
import LoginPage from '../pages/LoginPage/LoginPage'
import RegisterPage from '../pages/RegisterPage/RegisterPage'

import RegistryPage from '../pages/RegistryPage/RegistryPage'
import AnalysisPage from '../pages/AnalysisPage/AnalysisPage'
import BookmarksPage from '../pages/BookmarksPage/BookmarksPage'
import ProfilePage from '../pages/ProfilePage/ProfilePage'

import RequireAuth from '../components/RequireAuth'

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

      {/* Registry - handles guest vs authenticated internally, no guard */}
      <Route path="/registry" element={<RegistryPage />} />

      {/* Analysis - registered users only */}
      <Route
        path="/analysis"
        element={
          <RequireAuth>
            <AnalysisPage />
          </RequireAuth>
        }
      />

      {/* Bookmarks - registered users only */}
      <Route
        path="/bookmarks"
        element={
          <RequireAuth>
            <BookmarksPage />
          </RequireAuth>
        }
      />

      {/* Profile - registered users only */}
      <Route
        path="/profile"
        element={
          <RequireAuth>
            <ProfilePage />
          </RequireAuth>
        }
      />
    </Routes>
  )
}

export default AppRoutes
