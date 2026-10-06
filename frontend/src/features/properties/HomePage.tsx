import { ArrowRight, Globe2, PlayCircle, ShieldCheck, TrendingUp, Users, type LucideIcon } from 'lucide-react'
import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/Button'
import { EmptyState, ErrorState, LoadingState } from '@/components/ui/States'
import { useProperties } from '@/hooks/queries'
import { PropertyCard } from './PropertyCard'

const benefits: { icon: LucideIcon; title: string; text: string }[] = [
  { icon: Users, title: 'Low Entry Barrier', text: 'Start with a small investment and be part of real estate.' },
  { icon: ShieldCheck, title: 'Transparent & Secure', text: 'Full property financials, documents and risks — shown up front.' },
  { icon: TrendingUp, title: 'Rental Income', text: 'Where applicable, receive distributions from rental income.' },
  { icon: Globe2, title: 'Global Opportunities', text: 'Access properties in different countries and markets.' },
]

export default function HomePage() {
  const q = useProperties({ page: 0, size: 4 })
  return (
    <>
      <section className="mx-auto max-w-[1280px] px-5 pt-6 sm:px-8">
        <div className="relative overflow-hidden rounded-[28px] bg-gradient-to-br from-sky via-mint to-sand/60 px-6 py-16 sm:px-12 sm:py-24 lg:py-28">
          <div aria-hidden className="absolute -right-24 -top-24 size-96 rounded-full bg-white/40 blur-3xl" />
          <div className="relative max-w-2xl">
            <span className="inline-block rounded-full bg-white/70 px-3.5 py-1 text-xs text-muted">Real Estate Investing for Everyone</span>
            <h1 className="mt-6 text-4xl font-bold leading-[1.05] tracking-tight sm:text-6xl">Own a piece<br />of real estate.<br />Build your future.</h1>
            <p className="mt-6 max-w-lg text-base leading-relaxed text-muted sm:text-lg">
              RREstate makes real estate investing simple and accessible. Invest in carefully selected properties, with transparent financials, even with a small amount of capital.
            </p>
            <div className="mt-8 flex flex-wrap items-center gap-4">
              <Button asChild size="lg"><Link to="/properties">Start Investing <ArrowRight className="size-4" /></Link></Button>
              <Link to="/how-it-works" className="inline-flex items-center gap-2 text-sm font-medium"><PlayCircle className="size-5 text-sage" />How It Works</Link>
            </div>
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-[1280px] px-5 py-16 sm:px-8">
        <div className="mb-6 flex items-end justify-between">
          <h2 className="text-2xl font-semibold tracking-tight">Featured Properties</h2>
          <Link to="/properties" className="inline-flex items-center gap-1 text-sm text-muted hover:text-ink">View all <ArrowRight className="size-4" /></Link>
        </div>
        {q.isPending ? <LoadingState label="Loading properties…" />
          : q.isError ? <ErrorState error={q.error} onRetry={() => q.refetch()} />
          : q.data.content.length === 0 ? <EmptyState title="Opportunities are coming soon" text="We’re preparing our first properties. Create an account to be notified." action={<Button asChild><Link to="/register">Get Started</Link></Button>} />
          : <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">{q.data.content.map((p) => <PropertyCard key={p.id} property={p} />)}</div>}
      </section>

      <section className="mx-auto max-w-[1280px] px-5 sm:px-8">
        <div className="grid gap-8 rounded-card bg-mint-soft p-8 sm:grid-cols-2 lg:grid-cols-4">
          {benefits.map(({ icon: Icon, title, text }) => (
            <div key={title}>
              <Icon className="mb-3 size-7 text-sage-dark" strokeWidth={1.5} aria-hidden />
              <h3 className="font-semibold">{title}</h3>
              <p className="mt-1 text-sm text-muted">{text}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="mx-auto max-w-[1280px] px-5 py-16 sm:px-8">
        <div className="rounded-[28px] bg-gradient-to-r from-mint to-sky px-6 py-14 text-center sm:px-12">
          <p className="text-2xl font-medium italic sm:text-3xl">Real estate isn’t just for the few.<br />It’s for everyone.</p>
          <Button asChild size="lg" className="mt-8"><Link to="/register">Get Started</Link></Button>
        </div>
      </section>
    </>
  )
}
