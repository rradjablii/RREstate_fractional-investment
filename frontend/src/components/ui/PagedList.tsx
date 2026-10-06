import type { UseQueryResult } from '@tanstack/react-query'
import type { Page } from '@/types/api'
import { DataTable, type Column } from './DataTable'
import { Pagination } from './Pagination'
import { EmptyState, ErrorState, LoadingState } from './States'

/** Loading / error / empty / table / pagination for any paged resource. */
export function PagedList<T extends { id: string }>({ query, columns, caption, emptyTitle, emptyText, onPage }: {
  query: UseQueryResult<Page<T>>; columns: Column<T>[]; caption: string; emptyTitle: string; emptyText?: string; onPage: (p: number) => void
}) {
  if (query.isPending) return <LoadingState />
  if (query.isError) return <ErrorState error={query.error} onRetry={() => query.refetch()} />
  if (query.data.content.length === 0) return <EmptyState title={emptyTitle} text={emptyText} />
  return (<><DataTable columns={columns} rows={query.data.content} caption={caption} /><Pagination page={query.data} onChange={onPage} /></>)
}
