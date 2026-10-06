import { ChevronRight, Coins, LineChart as LineIcon, Wallet } from 'lucide-react'
import { Link } from 'react-router-dom'
import { Area, AreaChart, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { PageHeader } from '@/components/layout/PageHeader'
import { Badge, statusTone } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { MetricCard } from '@/components/ui/MetricCard'
import { PropertyImage } from '@/components/ui/PropertyImage'
import { EmptyState, ErrorState, LoadingState } from '@/components/ui/States'
import { usePortfolio } from '@/hooks/queries'
import { formatMoney, formatPercent, humanize } from '@/lib/format'

// Palette-only chart colours (design tokens mirrored for SVG fills).
const SLICES = ['#6fa58b', '#9cc4b0', '#a9d3dd', '#e9d9c4', '#c9d9c7', '#7fb0c0']

export default function PortfolioPage() {
  const q = usePortfolio()
  return (
    <div>
      <PageHeader title="My Portfolio" subtitle="Track your investments, earnings and portfolio performance." />
      {q.isPending ? <LoadingState label="Loading your portfolio…" />
        : q.isError ? <ErrorState error={q.error} onRetry={() => q.refetch()} />
        : <Content p={q.data} />}
    </div>
  )
}

function Content({ p }: { p: NonNullable<ReturnType<typeof usePortfolio>['data']> }) {
  const empty = p.positions.length === 0
  const ret = p.totalReturnPercent != null ? `${formatPercent(p.totalReturnPercent)} total return` : undefined
  return (
    <div className="space-y-8">
      <div className="grid gap-4 sm:grid-cols-3">
        <MetricCard label="Total Invested" value={formatMoney(p.totalInvested, p.currency)} icon={Wallet} />
        <MetricCard label="Total Distributions" value={formatMoney(p.totalDistributions, p.currency)} icon={Coins} tone="sand" />
        <MetricCard label="Current Portfolio Value" value={formatMoney(p.currentValue, p.currency)} icon={LineIcon} tone="sky" note={ret} />
      </div>

      {empty ? (
        <EmptyState title="You haven’t invested yet" text="Explore available properties and make your first investment." action={<Button asChild><Link to="/properties">Explore Properties</Link></Button>} />
      ) : (
        <>
          <div className="grid gap-4 lg:grid-cols-[2fr_3fr]">
            <Card className="p-6">
              <h2 className="font-semibold">Portfolio Allocation</h2>
              <div className="mt-4 flex flex-wrap items-center gap-6">
                <div className="relative size-40 shrink-0">
                  <ResponsiveContainer>
                    <PieChart>
                      <Pie data={p.allocation.map((a) => ({ name: a.propertyName, value: Number(a.percent) }))} dataKey="value" innerRadius={52} outerRadius={78} paddingAngle={2} stroke="none">
                        {p.allocation.map((_, i) => <Cell key={i} fill={SLICES[i % SLICES.length]} />)}
                      </Pie>
                    </PieChart>
                  </ResponsiveContainer>
                  <div className="pointer-events-none absolute inset-0 grid place-items-center text-center"><div><p className="text-2xl font-semibold">{p.positions.length}</p><p className="text-xs text-muted">Properties</p></div></div>
                </div>
                <ul className="min-w-0 flex-1 space-y-2 text-sm">
                  {p.allocation.map((a, i) => (
                    <li key={a.propertyId} className="flex items-center gap-2">
                      <span className="size-2.5 shrink-0 rounded-full" style={{ background: SLICES[i % SLICES.length] }} aria-hidden />
                      <span className="truncate text-muted">{a.propertyName}</span><span className="ml-auto font-medium">{formatPercent(a.percent, 0)}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </Card>

            <Card className="p-6">
              <h2 className="font-semibold">Portfolio Performance</h2>
              {p.performance.length < 2 ? <p className="mt-6 text-sm text-muted">Performance history will appear once there is enough data.</p> : (
                <div className="mt-4 h-48" aria-label="Portfolio value over time">
                  <ResponsiveContainer>
                    <AreaChart data={p.performance.map((d) => ({ date: d.date, value: Number(d.value) }))}>
                      <defs><linearGradient id="pv" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#6fa58b" stopOpacity={0.25} /><stop offset="100%" stopColor="#6fa58b" stopOpacity={0} /></linearGradient></defs>
                      <XAxis dataKey="date" tick={{ fontSize: 11, fill: '#60747a' }} tickLine={false} axisLine={false} minTickGap={32} />
                      <YAxis hide domain={['auto', 'auto']} />
                      <Tooltip formatter={(v) => formatMoney(String(v), p.currency)} contentStyle={{ borderRadius: 12, border: '1px solid rgb(23 59 74 / 0.1)' }} />
                      <Area type="monotone" dataKey="value" stroke="#6fa58b" strokeWidth={2} fill="url(#pv)" />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
              )}
            </Card>
          </div>

          <section aria-labelledby="myprops">
            <h2 id="myprops" className="mb-4 text-lg font-semibold">My Properties</h2>
            <div className="space-y-3">
              {p.positions.map((pos) => (
                <Link key={pos.propertyId} to={`/properties/${pos.propertyId}`} className="flex items-center gap-4 rounded-card border border-line bg-white p-4 shadow-card transition hover:bg-mint-soft">
                  <PropertyImage src={pos.coverImageUrl} alt={pos.propertyName} className="size-16 shrink-0 rounded-control sm:size-20" />
                  <div className="grid min-w-0 flex-1 gap-x-6 gap-y-2 sm:grid-cols-[1.4fr_1fr_1fr_1fr]">
                    <div className="min-w-0"><p className="text-xs text-muted">{pos.country}</p><p className="truncate font-medium">{pos.propertyName}</p></div>
                    <Cell2 label="Investment" value={formatMoney(pos.investedAmount, pos.currency)} />
                    <Cell2 label="Interest" value={formatPercent(pos.interestPercent)} />
                    <Cell2 label="Monthly Distribution" value={formatMoney(pos.monthlyDistribution, pos.currency)} />
                  </div>
                  <Badge tone={statusTone(pos.status)} className="hidden sm:inline-flex">{humanize(pos.status)}</Badge>
                  <ChevronRight className="size-4 shrink-0 text-muted" aria-hidden />
                </Link>
              ))}
            </div>
          </section>
        </>
      )}
    </div>
  )
}

const Cell2 = ({ label, value }: { label: string; value: string }) => (
  <div><p className="text-xs text-muted">{label}</p><p className="text-sm font-semibold">{value}</p></div>
)
