import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useEffect } from 'react'
import { RouterProvider } from 'react-router-dom'
import { Toaster } from 'sonner'
import { refreshSession } from '@/api/client'
import { ApiError } from '@/api/errors'
import { router } from '@/routes/router'
import { useSession } from '@/stores/session'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      refetchOnWindowFocus: false,
      // Don't retry client errors (401/403/404/...); retry transient failures once.
      retry: (count, err) => !(err instanceof ApiError && err.status >= 400 && err.status < 500) && count < 1,
    },
  },
})

export default function App() {
  const setInitialized = useSession((s) => s.setInitialized)
  // Restore session from the HttpOnly refresh cookie on first load.
  useEffect(() => { void refreshSession().finally(setInitialized) }, [setInitialized])
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
      <Toaster position="top-right" />
    </QueryClientProvider>
  )
}
