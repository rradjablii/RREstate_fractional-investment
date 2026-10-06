import { cn } from '@/lib/cn'

/** RREstate mark: house outline + window + sage hill (after the brand reference). */
export function LogoMark({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 48 48" className={cn('size-8', className)} role="img" aria-label="RREstate">
      <path d="M8 36V20.5L24 8l11 8.6" fill="none" stroke="#3f6559" strokeWidth="4.6" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M40 22v14" stroke="#a9ccb8" strokeWidth="4.6" strokeLinecap="round" />
      <rect x="21" y="20" width="3" height="3" rx=".6" fill="#3f6559" />
      <rect x="25.4" y="20" width="3" height="3" rx=".6" fill="#3f6559" />
      <rect x="21" y="24.4" width="3" height="3" rx=".6" fill="#3f6559" />
      <rect x="25.4" y="24.4" width="3" height="3" rx=".6" fill="#3f6559" />
      <path d="M7 40c5-6 14-7.5 21-4.5 5 2 9 2.5 13 .5-3 5-8 7-14 7-8 0-15-.5-20-3Z" fill="#8fc0a5" />
    </svg>
  )
}

export function Logo({ className, size = 'md' }: { className?: string; size?: 'sm' | 'md' | 'lg' }) {
  const text = { sm: 'text-lg', md: 'text-xl', lg: 'text-3xl' }[size]
  const mark = { sm: 'size-7', md: 'size-8', lg: 'size-12' }[size]
  return (
    <span className={cn('inline-flex items-center gap-2 text-ink', className)}>
      <LogoMark className={mark} />
      <span className={cn('font-medium tracking-tight', text)}>RREstate</span>
    </span>
  )
}
