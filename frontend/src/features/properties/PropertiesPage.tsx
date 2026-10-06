import { useState } from 'react'
import { PageHeader } from '@/components/layout/PageHeader'
import { EmptyState, ErrorState, LoadingState } from '@/components/ui/States'
import { Pagination } from '@/components/ui/Pagination'
import { config } from '@/lib/config'
import { useProperties } from '@/hooks/queries'
import { PropertyCard } from './PropertyCard'

export default function PropertiesPage() {
  const [page, setPage] = useState(0)
  const q = useProperties({ page, size: config.defaultPageSize })
  return (
    <div className="mx-auto max-w-[1280px] px-5 py-10 sm:px-8">
      <PageHeader title="Properties" subtitle="Browse investment opportunities and review the full details before you decide." />
      {q.isPending ? <LoadingState label="Loading properties…" />
        : q.isError ? <ErrorState error={q.error} onRetry={() => q.refetch()} />
        : q.data.content.length === 0 ? <EmptyState title="No properties yet" text="New opportunities will appear here as soon as they are published." />
        : (
          <>
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {q.data.content.map((p) => <PropertyCard key={p.id} property={p} />)}
            </div>
            <Pagination page={q.data} onChange={(p) => { setPage(p); window.scrollTo({ top: 0 }) }} />
          </>
        )}
    </div>
  )
}
