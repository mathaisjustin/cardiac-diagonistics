// Decodes a JWT's payload to read its `exp` claim - no verification, purely
// for UI purposes (e.g. showing "time left in session"). Never trust this
// for anything security-relevant; the backend is the source of truth there.
export const getJwtExpiryMs = (token: string): number | null => {
  try {
    const [, payload] = token.split('.')
    const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')))
    return typeof decoded.exp === 'number' ? decoded.exp * 1000 : null
  } catch {
    return null
  }
}
