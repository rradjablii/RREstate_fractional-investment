import { useSession } from '@/stores/session'
import type { AuthResponse, LoginRequest, RegisterRequest, User } from '@/types/api'
import { api } from './client'

function store(res: AuthResponse) {
  useSession.getState().setSession(res.user, res.accessToken, res.expiresInSeconds)
  return res.user
}

export const authApi = {
  login: async (body: LoginRequest) => store(await api.post<AuthResponse>('/auth/login', body, { anonymous: true })),
  register: async (body: RegisterRequest) => store(await api.post<AuthResponse>('/auth/register', body, { anonymous: true })),
  logout: async () => {
    try { await api.post<void>('/auth/logout') } finally { useSession.getState().clear() }
  },
  me: () => api.get<User>('/users/me'),
}
