import type { Decimal } from '@/types/api'

export function ProgressBar({ percent, label }: { percent: Decimal; label?: string }) {
  const n = Math.min(100, Math.max(0, Number(percent) || 0))
  return (
    <div>
      <div className="h-1.5 overflow-hidden rounded-full bg-mint" role="progressbar" aria-valuenow={Math.round(n)} aria-valuemin={0} aria-valuemax={100} aria-label={label ?? 'Funding progress'}>
        <div className="h-full rounded-full bg-sage transition-[width] duration-700" style={{ width: `${n}%` }} />
      </div>
      <p className="mt-1.5 text-xs text-muted">{Math.round(n)}% funded</p>
    </div>
  )
}
