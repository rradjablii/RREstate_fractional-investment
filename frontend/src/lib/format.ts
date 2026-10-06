import type { CurrencyCode, Decimal } from '@/types/api'

/** Display-only formatting of server-provided decimals. No arithmetic. */
export function formatMoney(amount: Decimal | null | undefined, currency: CurrencyCode = 'USD', fractionDigits?: number) {
  if (amount == null) return '—'
  const n = Number(amount)
  if (!Number.isFinite(n)) return '—'
  const whole = Number.isInteger(n)
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    minimumFractionDigits: fractionDigits ?? (whole ? 0 : 2),
    maximumFractionDigits: fractionDigits ?? 2,
  }).format(n)
}

export function formatPercent(value: Decimal | null | undefined, digits = 1) {
  if (value == null) return '—'
  const n = Number(value)
  return Number.isFinite(n) ? `${n.toFixed(digits)}%` : '—'
}

export function formatDate(iso: string | null | undefined) {
  if (!iso) return '—'
  const d = new Date(iso)
  return Number.isNaN(d.getTime()) ? '—' : d.toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
}

export function humanize(code: string) {
  return code.toLowerCase().replace(/_/g, ' ').replace(/^\w/, (c) => c.toUpperCase())
}
