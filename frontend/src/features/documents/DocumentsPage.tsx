import { useState } from 'react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Badge } from '@/components/ui/Badge'
import { PagedList } from '@/components/ui/PagedList'
import { useDocuments } from '@/hooks/queries'
import { formatDate, humanize } from '@/lib/format'

export default function DocumentsPage() {
  const [page, setPage] = useState(0)
  const q = useDocuments(page)
  return (
    <div>
      <PageHeader title="Documents" subtitle="Agreements, statements and property documents available to you." />
      <PagedList query={q} onPage={setPage} caption="Documents" emptyTitle="No documents yet" emptyText="Documents related to your investments will appear here."
        columns={[
          { header: 'Title', cell: (d) => d.title },
          { header: 'Category', cell: (d) => <Badge>{humanize(d.category)}</Badge> },
          { header: 'Property', cell: (d) => d.propertyName ?? '—' },
          { header: 'Added', cell: (d) => formatDate(d.createdAt) },
        ]} />
    </div>
  )
}
