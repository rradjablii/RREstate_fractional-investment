import type { Page } from '@/types/api'
import { Button } from './Button'

export function Pagination({ page, onChange }: { page: Pick<Page<unknown>, 'page' | 'totalPages'>; onChange: (p: number) => void }) {
  if (page.totalPages <= 1) return null
  return (
    <nav aria-label="Pagination" className="mt-6 flex items-center justify-between">
      <Button variant="secondary" size="sm" disabled={page.page <= 0} onClick={() => onChange(page.page - 1)}>Previous</Button>
      <span className="text-sm text-muted">Page {page.page + 1} of {page.totalPages}</span>
      <Button variant="secondary" size="sm" disabled={page.page + 1 >= page.totalPages} onClick={() => onChange(page.page + 1)}>Next</Button>
    </nav>
  )
}
