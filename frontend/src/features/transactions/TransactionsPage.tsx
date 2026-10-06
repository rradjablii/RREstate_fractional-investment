import { useState } from 'react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Badge, statusTone } from '@/components/ui/Badge'
import { PagedList } from '@/components/ui/PagedList'
import { useTransactions } from '@/hooks/queries'
import { formatDate, formatMoney, humanize } from '@/lib/format'

export default function TransactionsPage() {
  const [page, setPage] = useState(0)
  const q = useTransactions(page)
  return (
    <div>
      <PageHeader title="Transactions" subtitle="A record of every investment, payment and refund on your account." />
      <PagedList query={q} onPage={setPage} caption="Transactions" emptyTitle="No transactions yet" emptyText="Your investments and payments will be listed here."
        columns={[
          { header: 'Date', cell: (t) => formatDate(t.createdAt) },
          { header: 'Type', cell: (t) => humanize(t.type) },
          { header: 'Property', cell: (t) => t.propertyName ?? '—' },
          { header: 'Status', cell: (t) => <Badge tone={statusTone(t.status)}>{humanize(t.status)}</Badge> },
          { header: 'Amount', align: 'right', cell: (t) => formatMoney(t.amount, t.currency) },
        ]} />
    </div>
  )
}
