import type { LucideIcon } from 'lucide-react'
import { Card } from './Card'

export function MetricCard({ label, value, icon: Icon, note, tone = 'mint' }: { label: string; value: string; icon: LucideIcon; note?: string; tone?: 'mint' | 'sand' | 'sky' }) {
  const bg = { mint: 'bg-mint text-sage-dark', sand: 'bg-sand/50 text-warn', sky: 'bg-sky text-ink' }[tone]
  return (
    <Card className="p-5">
      <div className="flex items-start justify-between">
        <p className="text-sm text-muted">{label}</p>
        <span className={`grid size-9 place-items-center rounded-full ${bg}`}><Icon className="size-4" aria-hidden /></span>
      </div>
      <p className="mt-2 text-3xl font-semibold tracking-tight">{value}</p>
      {note && <p className="mt-1 text-xs text-sage-dark">{note}</p>}
    </Card>
  )
}
