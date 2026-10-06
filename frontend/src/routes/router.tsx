import { lazy, Suspense } from 'react'
import { createBrowserRouter } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { PublicLayout } from '@/components/layout/PublicLayout'
import { LoadingState } from '@/components/ui/States'
import { AboutPage, HowItWorksPage, NotFoundPage } from '@/features/properties/InfoPages'
import { RequireAuth } from './guards'

const lazyPage = (C: React.LazyExoticComponent<React.ComponentType>) => <Suspense fallback={<LoadingState />}><C /></Suspense>

const Home = lazy(() => import('@/features/properties/HomePage'))
const Properties = lazy(() => import('@/features/properties/PropertiesPage'))
const PropertyDetail = lazy(() => import('@/features/properties/PropertyDetailPage'))
const Login = lazy(() => import('@/features/auth/LoginPage'))
const Register = lazy(() => import('@/features/auth/RegisterPage'))
const Settings = lazy(() => import('@/features/auth/SettingsPage'))
const Portfolio = lazy(() => import('@/features/portfolio/PortfolioPage'))
const Transactions = lazy(() => import('@/features/transactions/TransactionsPage'))
const Distributions = lazy(() => import('@/features/distributions/DistributionsPage'))
const Documents = lazy(() => import('@/features/documents/DocumentsPage'))
const Admin = lazy(() => import('@/features/admin/AdminPage'))

export const router = createBrowserRouter([
  { path: '/login', element: lazyPage(Login) },
  { path: '/register', element: lazyPage(Register) },
  {
    element: <PublicLayout />,
    children: [
      { path: '/', element: lazyPage(Home) },
      { path: '/properties', element: lazyPage(Properties) },
      { path: '/properties/:id', element: lazyPage(PropertyDetail) },
      { path: '/how-it-works', element: <HowItWorksPage /> },
      { path: '/about', element: <AboutPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
  {
    element: <RequireAuth />,
    children: [{
      element: <AppLayout />,
      children: [
        { path: '/portfolio', element: lazyPage(Portfolio) },
        { path: '/transactions', element: lazyPage(Transactions) },
        { path: '/distributions', element: lazyPage(Distributions) },
        { path: '/documents', element: lazyPage(Documents) },
        { path: '/settings', element: lazyPage(Settings) },
      ],
    }],
  },
  {
    element: <RequireAuth roles={['ADMIN', 'OPERATOR', 'COMPLIANCE']} />,
    children: [{ element: <AppLayout />, children: [{ path: '/admin', element: lazyPage(Admin) }] }],
  },
])
