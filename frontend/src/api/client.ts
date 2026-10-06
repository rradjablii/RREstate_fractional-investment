import { config } from '@/lib/config'
import { useSession } from '@/stores/session'
import type { AuthResponse, ErrorResponse } from '@/types/api'
import { ApiError } from './errors'

type Query = Record<string, string | number | boolean | undefined | null>

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  query?: Query
  body?: unknown
  headers?: Record<string, string>
  /** skip auth header and 401-refresh handling (login/register/refresh) */
  anonymous?: boolean
}

function buildUrl(path: string, query?: Query) {
  const url = `${config.apiBaseUrl}${config.apiPrefix}${path}`
  if (!query) return url
  const params = new URLSearchParams()
  for (const [k, v] of Object.entries(query)) if (v !== undefined && v !== null && v !== '') params.set(k, String(v))
  const qs = params.toString()
  return qs ? `${url}?${qs}` : url
}

async function toApiError(res: Response): Promise<ApiError> {
  let body: Partial<ErrorResponse> = {}
  try { body = await res.json() } catch { /* non-JSON error body */ }
  return new ApiError({
    status: res.status,
    errorCode: body.errorCode ?? `HTTP_${res.status}`,
    message: body.message ?? (res.status >= 500 ? 'The server had a problem. Please try again shortly.' : 'The request could not be completed.'),
    correlationId: body.correlationId ?? res.headers.get('X-Request-Id') ?? undefined,
    validationErrors: body.validationErrors,
  })
}

async function send(path: string, opts: RequestOptions, token: string | null) {
  const headers: Record<string, string> = { Accept: 'application/json', ...opts.headers }
  if (opts.body !== undefined) headers['Content-Type'] = 'application/json'
  if (token && !opts.anonymous) headers.Authorization = `Bearer ${token}`
  try {
    return await fetch(buildUrl(path, opts.query), {
      method: opts.method ?? 'GET',
      headers,
      body: opts.body !== undefined ? JSON.stringify(opts.body) : undefined,
      credentials: 'include', // refresh-token cookie
    })
  } catch {
    throw new ApiError({ status: 0, errorCode: 'NETWORK_ERROR', message: 'Unable to reach the server. Check your connection and try again.' })
  }
}

let refreshInFlight: Promise<boolean> | null = null

/** Silent refresh using the HttpOnly cookie. Single-flight. Returns success. */
export function refreshSession(): Promise<boolean> {
  refreshInFlight ??= (async () => {
    try {
      const res = await send('/auth/refresh', { method: 'POST', anonymous: true }, null)
      if (!res.ok) { useSession.getState().clear(); return false }
      const data = (await res.json()) as AuthResponse
      useSession.getState().setSession(data.user, data.accessToken, data.expiresInSeconds)
      return true
    } catch {
      return false
    } finally {
      refreshInFlight = null
    }
  })()
  return refreshInFlight
}

export async function request<T>(path: string, opts: RequestOptions = {}): Promise<T> {
  let res = await send(path, opts, useSession.getState().accessToken)
  if (res.status === 401 && !opts.anonymous && (await refreshSession())) {
    res = await send(path, opts, useSession.getState().accessToken)
  }
  if (!res.ok) {
    if (res.status === 401 && !opts.anonymous) useSession.getState().clear()
    throw await toApiError(res)
  }
  if (res.status === 204) return undefined as T
  return (await res.json()) as T
}

export const api = {
  get: <T>(path: string, query?: Query) => request<T>(path, { query }),
  post: <T>(path: string, body?: unknown, opts: Omit<RequestOptions, 'method' | 'body'> = {}) => request<T>(path, { ...opts, method: 'POST', body }),
}
