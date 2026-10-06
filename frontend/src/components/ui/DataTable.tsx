import type { ReactNode } from 'react'
import { Card } from './Card'

export interface Column<T> { header: string; cell: (row: T) => ReactNode; align?: 'right' }

export function DataTable<T extends { id: string }>({ columns, rows, caption }: { columns: Column<T>[]; rows: T[]; caption: string }) {
  return (
    <Card className="overflow-x-auto">
      <table className="w-full min-w-[560px] text-left text-sm">
        <caption className="sr-only">{caption}</caption>
        <thead><tr className="border-b border-line text-xs text-muted">
          {columns.map((c) => <th key={c.header} scope="col" className={`px-5 py-3 font-medium ${c.align === 'right' ? 'text-right' : ''}`}>{c.header}</th>)}
        </tr></thead>
        <tbody className="divide-y divide-line">
          {rows.map((r) => <tr key={r.id}>{columns.map((c) => <td key={c.header} className={`px-5 py-3.5 ${c.align === 'right' ? 'text-right font-medium' : ''}`}>{c.cell(r)}</td>)}</tr>)}
        </tbody>
      </table>
    </Card>
  )
}
