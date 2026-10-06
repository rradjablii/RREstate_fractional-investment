import { Slot } from '@radix-ui/react-slot'
import { Loader2 } from 'lucide-react'
import type { ButtonHTMLAttributes } from 'react'
import { cn } from '@/lib/cn'

const variants = {
  primary: 'bg-sage text-white hover:bg-sage-dark shadow-sm',
  secondary: 'bg-white text-ink border border-line hover:bg-mint-soft',
  ghost: 'text-ink hover:bg-mint-soft',
  danger: 'bg-white text-warn border border-sand hover:bg-cream',
} as const
const sizes = { sm: 'h-9 px-4 text-sm', md: 'h-11 px-6 text-sm', lg: 'h-12 px-7 text-base' } as const

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: keyof typeof variants
  size?: keyof typeof sizes
  loading?: boolean
  asChild?: boolean
}

export function Button({ variant = 'primary', size = 'md', loading, asChild, className, children, disabled, ...rest }: Props) {
  const Comp = asChild ? Slot : 'button'
  return (
    <Comp
      className={cn(
        'inline-flex items-center justify-center gap-2 rounded-full font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-60',
        variants[variant], sizes[size], className,
      )}
      disabled={disabled || loading}
      {...rest}
    >
      {asChild ? children : (<>{loading && <Loader2 className="size-4 animate-spin" aria-hidden />}{children}</>)}
    </Comp>
  )
}
