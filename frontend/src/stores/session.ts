import { create } from 'zustand'
import type { User } from '@/types/api'

/**
 * Auth session. The access token lives ONLY in memory (never localStorage);
 * the refresh token is an HttpOnly cookie managed by the backend.
 */
interface SessionState {
  user: User | null
  accessToken: string | null
  expiresAt: number | null
  /** true once the initial silent refresh attempt has finished */
  initialized: boolean
  setSession: (user: User, accessToken: string, expiresInSeconds: number) => void
  clear: () => void
  setInitialized: () => void
}

export const useSession = create<SessionState>((set) => ({
  user: null,
  accessToken: null,
  expiresAt: null,
  initialized: false,
  setSession: (user, accessToken, expiresInSeconds) =>
    set({ user, accessToken, expiresAt: Date.now() + expiresInSeconds * 1000 }),
  clear: () => set({ user: null, accessToken: null, expiresAt: null }),
  setInitialized: () => set({ initialized: true }),
}))
