import type { HTMLAttributes } from 'react'
import { cn } from '@/lib/cn'

const tones = {
  green: 'bg-mint text-sage-dark',
  blue: 'bg-sky text-ink',
  sand: 'bg-sand/60 text-warn',
  neutral: 'bg-cream text-muted',
} as const

export function Badge({ tone = 'neutral', className, ...rest }: HTMLAttributes<HTMLSpanElement> & { tone?: keyof typeof tones }) {
  return <span className={cn('inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium', tones[tone], className)} {...rest} />
}

export function statusTone(status: string): keyof typeof tones {
  switch (status) {
    case 'OPEN': case 'ACTIVE': case 'CONFIRMED': case 'COMPLETED': case 'PAID': case 'VERIFIED': return 'green'
    case 'COMING_SOON': case 'PENDING': case 'PAYMENT_PENDING': return 'blue'
    case 'FAILED': case 'REJECTED': case 'REFUNDED': return 'sand'
    default: return 'neutral'
  }
}
