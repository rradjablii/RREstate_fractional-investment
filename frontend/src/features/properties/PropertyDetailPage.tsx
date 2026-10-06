import { ArrowLeft, FileText, MapPin } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { Badge, statusTone } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { ProgressBar } from '@/components/ui/ProgressBar'
import { PropertyImage } from '@/components/ui/PropertyImage'
import { ErrorState, LoadingState } from '@/components/ui/States'
import { useProperty } from '@/hooks/queries'
import { formatMoney, formatPercent, humanize } from '@/lib/format'
import { useSession } from '@/stores/session'
import { InvestDialog } from './InvestDialog'

export default function PropertyDetailPage() {
  const { id = '' } = useParams()
  const q = useProperty(id)
  const user = useSession((s) => s.user)
  const navigate = useNavigate()
  const [investOpen, setInvestOpen] = useState(false)

  if (q.isPending) return <LoadingState label="Loading property…" />
  if (q.isError) return <div className="mx-auto max-w-[1280px] px-5 py-10 sm:px-8"><ErrorState error={q.error} onRetry={() => q.refetch()} /></div>
  const p = q.data
  const canInvest = p.status === 'OPEN' && p.investmentEnabled && !!p.opportunityId

  return (
    <div className="mx-auto max-w-[1280px] px-5 py-8 sm:px-8">
      <Link to="/properties" className="mb-5 inline-flex items-center gap-1.5 text-sm text-muted hover:text-ink"><ArrowLeft className="size-4" />All properties</Link>
      <PropertyImage src={p.coverImageUrl} alt={p.name} className="aspect-[16/7] w-full rounded-card" />

      <div className="mt-8 grid gap-8 lg:grid-cols-[1fr_380px]">
        <div className="space-y-10">
          <div>
            <div className="flex flex-wrap gap-2"><Badge tone="blue">{p.country}</Badge><Badge tone={statusTone(p.status)}>{humanize(p.status)}</Badge><Badge>{p.propertyType}</Badge></div>
            <h1 className="mt-3 text-3xl font-bold tracking-tight sm:text-4xl">{p.name}</h1>
            <p className="mt-2 flex items-center gap-1.5 text-sm text-muted"><MapPin className="size-4" />{p.city}, {p.country}{p.address ? ` · ${p.address}` : ''}</p>
            <p className="mt-5 max-w-3xl whitespace-pre-line leading-relaxed text-muted">{p.description}</p>
          </div>

          <section aria-labelledby="fin">
            <h2 id="fin" className="mb-4 text-xl font-semibold">Financial overview</h2>
            <Card className="divide-y divide-line">
              <Row label="Acquisition price" value={formatMoney(p.acquisitionPrice, p.currency)} />
              <Row label="Valuation" value={formatMoney(p.valuation, p.currency)} />
              {p.financials.map((f) => <Row key={f.label} label={`${f.label} (${f.basis.toLowerCase()})`} value={formatMoney(f.amount, p.currency)} />)}
              <Row label="Projected net income" value={formatMoney(p.projectedNetIncome, p.currency)} strong />
            </Card>
            <p className="mt-2 text-xs text-muted">Figures marked projected or estimated are not guaranteed.</p>
          </section>

          {(p.legalStructure || p.entityName) && (
            <section aria-labelledby="struct">
              <h2 id="struct" className="mb-4 text-xl font-semibold">Investment structure</h2>
              <Card className="divide-y divide-line">
                {p.legalStructure && <Row label="Structure" value={p.legalStructure} />}
                {p.entityName && <Row label="Holding entity" value={p.entityName} />}
              </Card>
            </section>
          )}

          {p.risks.length > 0 && (
            <section aria-labelledby="risks">
              <h2 id="risks" className="mb-4 text-xl font-semibold">Risks</h2>
              <ul className="list-disc space-y-2 pl-5 text-sm text-muted">{p.risks.map((r) => <li key={r}>{r}</li>)}</ul>
            </section>
          )}

          {p.documents.length > 0 && (
            <section aria-labelledby="docs">
              <h2 id="docs" className="mb-4 text-xl font-semibold">Documents</h2>
              <Card className="divide-y divide-line">
                {p.documents.map((d) => <div key={d.id} className="flex items-center gap-3 px-5 py-3.5 text-sm"><FileText className="size-4 text-sage-dark" aria-hidden />{d.title}<Badge className="ml-auto">{d.category}</Badge></div>)}
              </Card>
            </section>
          )}

          {p.images.length > 1 && (
            <section aria-labelledby="photos">
              <h2 id="photos" className="mb-4 text-xl font-semibold">Photos</h2>
              <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">{p.images.map((i) => <PropertyImage key={i.id} src={i.url} alt={i.caption ?? p.name} className="aspect-[4/3] w-full rounded-control" />)}</div>
            </section>
          )}

          {p.disclosures.length > 0 && (
            <section aria-labelledby="disc" className="rounded-card bg-cream p-6">
              <h2 id="disc" className="mb-2 text-base font-semibold">Important disclosures</h2>
              <ul className="list-disc space-y-1.5 pl-5 text-xs leading-relaxed text-muted">{p.disclosures.map((d) => <li key={d}>{d}</li>)}</ul>
            </section>
          )}
        </div>

        <aside className="lg:sticky lg:top-24 lg:self-start">
          <Card className="space-y-5 p-6">
            <div className="grid grid-cols-2 gap-4">
              <Stat label="Projected yield" value={`${formatPercent(p.expectedYieldPercent)} / yr`} />
              <Stat label="Min. investment" value={formatMoney(p.minimumInvestment, p.currency)} />
            </div>
            <div>
              <ProgressBar percent={p.fundingProgressPercent} />
              <p className="mt-1 text-xs text-muted">{formatMoney(p.raisedAmount, p.currency)} of {formatMoney(p.targetRaise, p.currency)}</p>
            </div>
            {canInvest ? (
              <Button className="w-full" size="lg" onClick={() => (user ? setInvestOpen(true) : navigate('/login', { state: { from: `/properties/${p.id}` } }))}>
                {user ? 'Invest' : 'Log in to invest'}
              </Button>
            ) : (
              <p className="rounded-control bg-mint-soft px-4 py-3 text-sm text-muted">Investing isn’t available for this property right now.</p>
            )}
          </Card>
        </aside>
      </div>
      {canInvest && user && <InvestDialog property={p} open={investOpen} onOpenChange={setInvestOpen} />}
    </div>
  )
}

const Row = ({ label, value, strong }: { label: string; value: string; strong?: boolean }) => (
  <div className="flex justify-between gap-4 px-5 py-3.5 text-sm"><span className="text-muted">{label}</span><span className={strong ? 'font-semibold' : 'font-medium'}>{value}</span></div>
)
const Stat = ({ label, value }: { label: string; value: string }) => (
  <div><p className="text-xs text-muted">{label}</p><p className="text-xl font-semibold">{value}</p></div>
)
