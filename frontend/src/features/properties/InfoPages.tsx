import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/Button'

const Prose = ({ title, children }: { title: string; children: React.ReactNode }) => (
  <div className="mx-auto max-w-3xl px-5 py-14 sm:px-8">
    <h1 className="text-4xl font-bold tracking-tight">{title}</h1>
    <div className="mt-6 space-y-5 leading-relaxed text-muted">{children}</div>
    <Button asChild className="mt-10"><Link to="/properties">Browse properties</Link></Button>
  </div>
)

export function HowItWorksPage() {
  return (
    <Prose title="How It Works">
      <p><strong className="text-ink">1. Create an account.</strong> Sign up and, where required, complete identity verification.</p>
      <p><strong className="text-ink">2. Explore properties.</strong> Every listing shows its financials, structure, risks and documents.</p>
      <p><strong className="text-ink">3. Invest.</strong> Choose an amount at or above the minimum. Availability depends on your jurisdiction and eligibility.</p>
      <p><strong className="text-ink">4. Track your portfolio.</strong> Follow your investments, transactions and any distributions in one place.</p>
      <p className="text-xs">Investments carry risk, including loss of capital. Projected figures are not guarantees.</p>
    </Prose>
  )
}

export function AboutPage() {
  return (
    <Prose title="About RREstate">
      <p>RREstate exists to make real estate investing more accessible to people who can’t afford to buy an entire property.</p>
      <p>We focus on clear information: what a property costs, what it is expected to earn, what it costs to run, and what could go wrong.</p>
    </Prose>
  )
}

export function NotFoundPage() {
  return (
    <div className="mx-auto max-w-xl px-5 py-24 text-center">
      <h1 className="text-4xl font-bold">Page not found</h1>
      <p className="mt-3 text-muted">The page you’re looking for doesn’t exist.</p>
      <Button asChild className="mt-8"><Link to="/">Back to home</Link></Button>
    </div>
  )
}
