import { Outlet } from 'react-router-dom'

const RegisterLayout = () => {
  return (
    <main className="min-h-screen w-full bg-[#f5f3f3]">
      <div className="min-h-screen w-full bg-[#f8f7f7]">
        <div className="mx-auto flex min-h-screen max-w-[1400px] items-center px-12 py-10">
          <div className="w-full">
            <Outlet />
          </div>
        </div>
      </div>
    </main>
  )
}

export default RegisterLayout