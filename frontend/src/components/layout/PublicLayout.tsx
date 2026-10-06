import { Menu, X } from 'lucide-react'
import { useState } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import { Button } from '@/components/ui/Button'
import { Logo } from '@/components/ui/Logo'
import { cn } from '@/lib/cn'
import { useSession } from '@/stores/session'

const links = [
  { to: '/', label: 'Home', end: true },
  { to: '/properties', label: 'Properties' },
  { to: '/how-it-works', label: 'How It Works' },
  { to: '/about', label: 'About' },
]

export function PublicLayout() {
  const [open, setOpen] = useState(false)
  const user = useSession((s) => s.user)
  const linkCls = ({ isActive }: { isActive: boolean }) =>
    cn('border-b-2 py-1 text-sm transition-colors', isActive ? 'border-ink font-medium' : 'border-transparent text-muted hover:text-ink')
  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-30 border-b border-line bg-ivory/90 backdrop-blur">
        <div className="mx-auto flex h-16 max-w-[1280px] items-center justify-between px-5 sm:px-8">
          <Link to="/" aria-label="RREstate home"><Logo /></Link>
          <nav aria-label="Main" className="hidden gap-8 md:flex">
            {links.map((l) => <NavLink key={l.to} to={l.to} end={l.end} className={linkCls}>{l.label}</NavLink>)}
          </nav>
          <div className="hidden items-center gap-4 md:flex">
            {user ? <Button asChild size="sm"><Link to="/portfolio">My portfolio</Link></Button> : (
              <>
                <Link to="/login" className="text-sm text-muted hover:text-ink">Log in</Link>
                <Button asChild size="sm"><Link to="/register">Get Started</Link></Button>
              </>
            )}
          </div>
          <button className="grid size-10 place-items-center rounded-full md:hidden" aria-label={open ? 'Close menu' : 'Open menu'} aria-expanded={open} onClick={() => setOpen((o) => !o)}>
            {open ? <X className="size-5" /> : <Menu className="size-5" />}
          </button>
        </div>
        {open && (
          <nav aria-label="Mobile" className="flex flex-col gap-1 border-t border-line px-5 py-3 md:hidden" onClick={() => setOpen(false)}>
            {links.map((l) => <NavLink key={l.to} to={l.to} end={l.end} className="rounded-lg px-3 py-2.5 text-sm hover:bg-mint-soft">{l.label}</NavLink>)}
            {user ? <Link to="/portfolio" className="rounded-lg px-3 py-2.5 text-sm font-medium">My portfolio</Link> : (
              <>
                <Link to="/login" className="rounded-lg px-3 py-2.5 text-sm">Log in</Link>
                <Link to="/register" className="rounded-lg px-3 py-2.5 text-sm font-medium text-sage-dark">Get Started</Link>
              </>
            )}
          </nav>
        )}
      </header>
      <main className="flex-1"><Outlet /></main>
      <footer className="border-t border-line bg-cream">
        <div className="mx-auto max-w-[1280px] space-y-3 px-5 py-10 text-sm text-muted sm:px-8">
          <Logo size="sm" />
          <p className="max-w-3xl text-xs leading-relaxed">
            Investing in real estate involves risk, including possible loss of capital. Projected figures are estimates, not guarantees.
            Availability of investments depends on your jurisdiction and eligibility.
          </p>
          <p className="text-xs">© {new Date().getFullYear()} RREstate</p>
        </div>
      </footer>
    </div>
  )
}
