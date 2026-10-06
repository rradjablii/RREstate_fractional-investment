import { Link } from 'react-router-dom'
import { Badge, statusTone } from '@/components/ui/Badge'
import { ProgressBar } from '@/components/ui/ProgressBar'
import { PropertyImage } from '@/components/ui/PropertyImage'
import { formatMoney, formatPercent, humanize } from '@/lib/format'
import type { PropertySummary } from '@/types/api'

export function PropertyCard({ property: p }: { property: PropertySummary }) {
  return (
    <Link to={`/properties/${p.id}`} className="group block overflow-hidden rounded-card border border-line bg-white shadow-card transition hover:-translate-y-0.5 hover:shadow-md">
      <div className="relative">
        <PropertyImage src={p.coverImageUrl} alt={p.name} className="aspect-[4/3] w-full" />
        <div className="absolute bottom-3 left-3 flex gap-1.5">
          <Badge className="bg-white/90 text-ink">{p.country}</Badge>
          <Badge tone={statusTone(p.status)}>{humanize(p.status)}</Badge>
        </div>
      </div>
      <div className="p-4">
        <p className="text-xs text-muted">{p.city}</p>
        <h3 className="mt-0.5 line-clamp-2 min-h-[2.75rem] font-semibold leading-snug">{p.name}</h3>
        <dl className="mt-3 flex justify-between text-sm">
          <div><dt className="text-xs text-muted">Projected yield</dt><dd className="font-semibold">{formatPercent(p.expectedYieldPercent)} / year</dd></div>
          <div className="text-right"><dt className="text-xs text-muted">Min. investment</dt><dd className="font-semibold">{formatMoney(p.minimumInvestment, p.currency)}</dd></div>
        </dl>
        <div className="mt-3"><ProgressBar percent={p.fundingProgressPercent} /></div>
      </div>
    </Link>
  )
}
