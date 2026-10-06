/** Centralised frontend constants. Only public configuration lives here. */
export const config = {
  apiBaseUrl: (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, ''),
  apiPrefix: '/api/v1',
  defaultPageSize: 12,
  tablePageSize: 20,
  /** Refresh the access token this long before it expires. */
  tokenRefreshSkewMs: 30_000,
} as const
