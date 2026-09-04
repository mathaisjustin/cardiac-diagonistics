import { Outlet } from 'react-router-dom'

import Footer from '../components/Footer/Footer'
import Navbar from '../components/Navbar/Navbar'

const PublicLayout = () => {
  return (
    <div className="min-h-screen">
      <Navbar />

      <Outlet />

      <Footer />
    </div>
  )
}

export default PublicLayout