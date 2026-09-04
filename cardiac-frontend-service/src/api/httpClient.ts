const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:9090'

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  body?: unknown
  token?: string | null
}

const extractErrorMessage = async (response: Response): Promise<string> => {
  try {
    const data = await response.json()
    return data.message || data.error || `Request failed (${response.status})`
  } catch {
    return `Request failed (${response.status})`
  }
}

export const apiRequest = async <T>(
  path: string,
  { method = 'GET', body, token }: RequestOptions = {},
): Promise<T> => {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  if (!response.ok) {
    throw new ApiError(await extractErrorMessage(response), response.status)
  }

  const text = await response.text()

  if (!text) {
    return undefined as T
  }

  return JSON.parse(text) as T
}
