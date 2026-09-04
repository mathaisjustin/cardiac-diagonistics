const Footer = () => {
  return (
    <footer className="border-t border-gray-200 bg-white px-10 py-5">
      <div className="flex items-center justify-between text-sm text-gray-500">
        <p>© 2026 Cardiac Diagnostics</p>

        <div className="flex items-center gap-6">
          <a href="#about" className="hover:text-gray-900">
            About
          </a>

          <a href="#contact" className="hover:text-gray-900">
            Contact
          </a>

          <a href="#privacy" className="hover:text-gray-900">
            Privacy
          </a>

          <a href="#terms" className="hover:text-gray-900">
            Terms
          </a>
        </div>
      </div>
    </footer>
  )
}

export default Footer