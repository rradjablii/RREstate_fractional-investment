import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { LoadingState } from '@/components/ui/States'
import { useSession } from '@/stores/session'
import type { Role } from '@/types/api'

/** UX-only route protection. The backend enforces real authorization. */
export function RequireAuth({ roles }: { roles?: Role[] }) {
  const { user, initialized } = useSession()
  const location = useLocation()
  if (!initialized) return <LoadingState label="Checking your session…" />
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  if (roles && !user.roles.some((r) => roles.includes(r))) return <Navigate to="/portfolio" replace />
  return <Outlet />
}
