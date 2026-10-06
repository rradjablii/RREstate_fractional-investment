import { useState } from 'react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Badge, statusTone } from '@/components/ui/Badge'
import { PagedList } from '@/components/ui/PagedList'
import { useAdminInvestments, useAdminProperties } from '@/hooks/queries'
import { cn } from '@/lib/cn'
import { formatDate, formatMoney, formatPercent, humanize } from '@/lib/format'

export default function AdminPage() {
  const [tab, setTab] = useState<'properties' | 'investments'>('properties')
  const [pp, setPp] = useState(0)
  const [ip, setIp] = useState(0)
  const props = useAdminProperties(pp)
  const invs = useAdminInvestments(ip)
  return (
    <div>
      <PageHeader title="Admin" subtitle="Platform oversight. Access is enforced by the server." />
      <div role="tablist" aria-label="Admin sections" className="mb-6 inline-flex rounded-full bg-cream p-1">
        {(['properties', 'investments'] as const).map((t) => (
          <button key={t} role="tab" aria-selected={tab === t} onClick={() => setTab(t)} className={cn('rounded-full px-5 py-2 text-sm capitalize', tab === t ? 'bg-white font-medium shadow-sm' : 'text-muted')}>{t}</button>
        ))}
      </div>
      {tab === 'properties' ? (
        <PagedList query={props} onPage={setPp} caption="Properties" emptyTitle="No properties"
          columns={[
            { header: 'Name', cell: (p) => p.name }, { header: 'Location', cell: (p) => `${p.city}, ${p.country}` },
            { header: 'Status', cell: (p) => <Badge tone={statusTone(p.status)}>{humanize(p.status)}</Badge> },
            { header: 'Yield', cell: (p) => formatPercent(p.expectedYieldPercent) },
            { header: 'Raised', align: 'right', cell: (p) => `${formatMoney(p.raisedAmount, p.currency)} / ${formatMoney(p.targetRaise, p.currency)}` },
          ]} />
      ) : (
        <PagedList query={invs} onPage={setIp} caption="Investments" emptyTitle="No investments"
          columns={[
            { header: 'Date', cell: (i) => formatDate(i.createdAt) }, { header: 'Investor', cell: (i) => i.userEmail },
            { header: 'Property', cell: (i) => i.propertyName },
            { header: 'Status', cell: (i) => <Badge tone={statusTone(i.status)}>{humanize(i.status)}</Badge> },
            { header: 'Amount', align: 'right', cell: (i) => formatMoney(i.amount, i.currency) },
          ]} />
      )}
    </div>
  )
}
