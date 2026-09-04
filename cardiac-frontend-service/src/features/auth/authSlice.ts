import { createAsyncThunk, createSlice, type PayloadAction } from '@reduxjs/toolkit'

import * as authApi from '../../api/authApi'
import { ApiError } from '../../api/httpClient'

interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  status: 'idle' | 'loading' | 'failed'
  error: string | null
  registerStatus: 'idle' | 'loading' | 'succeeded' | 'failed'
  registerError: string | null
}

const ACCESS_TOKEN_KEY = 'cardiac.accessToken'
const REFRESH_TOKEN_KEY = 'cardiac.refreshToken'

const initialState: AuthState = {
  accessToken: localStorage.getItem(ACCESS_TOKEN_KEY),
  refreshToken: localStorage.getItem(REFRESH_TOKEN_KEY),
  status: 'idle',
  error: null,
  registerStatus: 'idle',
  registerError: null,
}

const errorMessage = (error: unknown) =>
  error instanceof ApiError ? error.message : 'Something went wrong. Please try again.'

const persistTokens = (state: AuthState, tokens: authApi.AuthResponse) => {
  state.accessToken = tokens.accessToken
  state.refreshToken = tokens.refreshToken
  localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken)
  localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken)
}

const clearTokens = (state: AuthState) => {
  state.accessToken = null
  state.refreshToken = null
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

export const loginUser = createAsyncThunk(
  'auth/login',
  async (payload: authApi.LoginRequest, { rejectWithValue }) => {
    try {
      return await authApi.login(payload)
    } catch (error) {
      return rejectWithValue(errorMessage(error))
    }
  },
)

export const registerUser = createAsyncThunk(
  'auth/register',
  async (payload: authApi.RegisterRequest, { rejectWithValue }) => {
    try {
      await authApi.register(payload)
    } catch (error) {
      return rejectWithValue(errorMessage(error))
    }
  },
)

// Proactively called shortly before the access token expires (see
// AuthSessionManager) to keep the session alive without the user noticing.
// Also doubles as the recovery path if a protected page finds the access
// token already expired - same thunk, same outcome either way.
export const refreshTokens = createAsyncThunk(
  'auth/refreshTokens',
  async (_: void, { getState, rejectWithValue }) => {
    const state = getState() as { auth: AuthState }
    const refreshToken = state.auth.refreshToken

    if (!refreshToken) {
      return rejectWithValue('No refresh token available')
    }

    try {
      return await authApi.refresh(refreshToken)
    } catch (error) {
      return rejectWithValue(errorMessage(error))
    }
  },
)

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    loggedOut: (state) => {
      clearTokens(state)
    },
    registerStatusReset: (state) => {
      state.registerStatus = 'idle'
      state.registerError = null
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginUser.pending, (state) => {
        state.status = 'loading'
        state.error = null
      })
      .addCase(
        loginUser.fulfilled,
        (state, action: PayloadAction<authApi.AuthResponse>) => {
          state.status = 'idle'
          persistTokens(state, action.payload)
        },
      )
      .addCase(loginUser.rejected, (state, action) => {
        state.status = 'failed'
        state.error = (action.payload as string) ?? 'Login failed'
      })
      .addCase(registerUser.pending, (state) => {
        state.registerStatus = 'loading'
        state.registerError = null
      })
      .addCase(registerUser.fulfilled, (state) => {
        state.registerStatus = 'succeeded'
      })
      .addCase(registerUser.rejected, (state, action) => {
        state.registerStatus = 'failed'
        state.registerError = (action.payload as string) ?? 'Registration failed'
      })
      .addCase(
        refreshTokens.fulfilled,
        (state, action: PayloadAction<authApi.AuthResponse>) => {
          persistTokens(state, action.payload)
        },
      )
      .addCase(refreshTokens.rejected, (state) => {
        // The refresh token is gone/expired/invalid - there's no session
        // left to keep alive, so drop it the same way an explicit logout
        // would. Every protected route's guard reacts to this automatically.
        clearTokens(state)
      })
  },
})

export const { loggedOut, registerStatusReset } = authSlice.actions
export default authSlice.reducer
