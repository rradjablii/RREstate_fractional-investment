import { useState } from 'react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Badge, statusTone } from '@/components/ui/Badge'
import { PagedList } from '@/components/ui/PagedList'
import { useDistributions } from '@/hooks/queries'
import { formatDate, formatMoney, humanize } from '@/lib/format'

export default function DistributionsPage() {
  const [page, setPage] = useState(0)
  const q = useDistributions(page)
  return (
    <div>
      <PageHeader title="Distributions" subtitle="Income paid out to you from your investments." />
      <PagedList query={q} onPage={setPage} caption="Distributions" emptyTitle="No distributions yet" emptyText="When a property you’ve invested in makes a distribution, it will appear here."
        columns={[
          { header: 'Property', cell: (d) => d.propertyName },
          { header: 'Period', cell: (d) => `${formatDate(d.periodStart)} – ${formatDate(d.periodEnd)}` },
          { header: 'Paid', cell: (d) => formatDate(d.paidAt) },
          { header: 'Status', cell: (d) => <Badge tone={statusTone(d.status)}>{humanize(d.status)}</Badge> },
          { header: 'Amount', align: 'right', cell: (d) => formatMoney(d.amount, d.currency) },
        ]} />
    </div>
  )
}
