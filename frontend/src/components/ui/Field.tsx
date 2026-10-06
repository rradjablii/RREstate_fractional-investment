import { forwardRef, useId, type InputHTMLAttributes } from 'react'
import { cn } from '@/lib/cn'

interface Props extends InputHTMLAttributes<HTMLInputElement> { label: string; error?: string; hint?: string }

export const Field = forwardRef<HTMLInputElement, Props>(function Field({ label, error, hint, className, id, ...rest }, ref) {
  const auto = useId()
  const inputId = id ?? auto
  const descId = `${inputId}-desc`
  return (
    <div>
      <label htmlFor={inputId} className="mb-1.5 block text-sm font-medium">{label}</label>
      <input
        ref={ref}
        id={inputId}
        aria-invalid={!!error}
        aria-describedby={error || hint ? descId : undefined}
        className={cn('h-11 w-full rounded-control border bg-white px-4 text-sm placeholder:text-muted/70', error ? 'border-warn' : 'border-line', className)}
        {...rest}
      />
      {(error || hint) && <p id={descId} role={error ? 'alert' : undefined} className={cn('mt-1.5 text-xs', error ? 'text-warn' : 'text-muted')}>{error ?? hint}</p>}
    </div>
  )
})
