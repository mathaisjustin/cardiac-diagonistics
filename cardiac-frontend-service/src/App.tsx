import AppRoutes from './routes/AppRoutes'
import AuthSessionManager from './features/auth/AuthSessionManager'
import { ToastProvider } from './components/Toast/ToastContext'

function App() {
  return (
    <ToastProvider>
      <AuthSessionManager />
      <AppRoutes />
    </ToastProvider>
  )
}

export default App
