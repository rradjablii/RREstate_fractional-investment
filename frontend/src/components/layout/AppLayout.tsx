import { useQueryClient } from '@tanstack/react-query'
import { Briefcase, Building2, FileText, Gift, LogOut, Menu, Receipt, Settings, Shield, X, type LucideIcon } from 'lucide-react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { authApi } from '@/api/auth'
import { Logo } from '@/components/ui/Logo'
import { cn } from '@/lib/cn'
import { useSession } from '@/stores/session'
import { useUi } from '@/stores/ui'

const nav: { to: string; label: string; icon: LucideIcon }[] = [
  { to: '/portfolio', label: 'Portfolio', icon: Briefcase },
  { to: '/properties', label: 'Properties', icon: Building2 },
  { to: '/transactions', label: 'Transactions', icon: Receipt },
  { to: '/distributions', label: 'Distributions', icon: Gift },
  { to: '/documents', label: 'Documents', icon: FileText },
  { to: '/settings', label: 'Settings', icon: Settings },
]

export function AppLayout() {
  const user = useSession((s) => s.user)!
  const { sidebarOpen, setSidebarOpen } = useUi()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const isStaff = user.roles.some((r) => r !== 'USER')
  const initials = `${user.firstName[0] ?? ''}${user.lastName[0] ?? ''}`.toUpperCase()

  async function logout() {
    try { await authApi.logout() } catch { toast.error('Could not reach the server; you were signed out locally.') }
    qc.clear()
    navigate('/')
  }

  const item = ({ isActive }: { isActive: boolean }) =>
    cn('flex items-center gap-3 rounded-control px-3.5 py-2.5 text-sm transition-colors', isActive ? 'bg-mint font-medium text-ink' : 'text-muted hover:bg-mint-soft hover:text-ink')

  const sidebar = (
    <nav aria-label="Dashboard" className="flex flex-col gap-1" onClick={() => setSidebarOpen(false)}>
      {nav.map(({ to, label, icon: Icon }) => (
        <NavLink key={to} to={to} className={item}>{({ isActive }) => (<><Icon className={cn('size-[18px]', isActive && 'text-sage-dark')} aria-hidden />{label}</>)}</NavLink>
      ))}
      {isStaff && <NavLink to="/admin" className={item}><Shield className="size-[18px]" aria-hidden />Admin</NavLink>}
    </nav>
  )

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-line bg-white/90 px-4 backdrop-blur sm:px-8">
        <div className="flex items-center gap-2">
          <button className="grid size-10 place-items-center rounded-full lg:hidden" aria-label="Open navigation" onClick={() => setSidebarOpen(true)}><Menu className="size-5" /></button>
          <Link to="/" aria-label="RREstate home"><Logo /></Link>
        </div>
        <div className="flex items-center gap-3">
          <span className="grid size-9 place-items-center rounded-full bg-sand/70 text-xs font-semibold" aria-hidden>{initials}</span>
          <span className="hidden text-sm font-medium sm:block">{user.firstName} {user.lastName}</span>
          <button onClick={logout} className="grid size-10 place-items-center rounded-full text-muted hover:bg-mint-soft hover:text-ink" aria-label="Log out"><LogOut className="size-[18px]" /></button>
        </div>
      </header>
      <div className="mx-auto flex max-w-[1400px]">
        <aside className="sticky top-16 hidden h-[calc(100vh-4rem)] w-60 shrink-0 border-r border-line p-4 lg:block">{sidebar}</aside>
        {sidebarOpen && (
          <div className="fixed inset-0 z-40 lg:hidden" role="dialog" aria-modal="true" aria-label="Navigation">
            <div className="absolute inset-0 bg-ink/30" onClick={() => setSidebarOpen(false)} />
            <div className="absolute inset-y-0 left-0 w-72 bg-ivory p-4 shadow-xl">
              <div className="mb-4 flex items-center justify-between"><Logo /><button className="grid size-10 place-items-center" aria-label="Close navigation" onClick={() => setSidebarOpen(false)}><X className="size-5" /></button></div>
              {sidebar}
            </div>
          </div>
        )}
        <main className="min-w-0 flex-1 px-4 py-8 sm:px-8 lg:px-10"><Outlet /></main>
      </div>
    </div>
  )
}
