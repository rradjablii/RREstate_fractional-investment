import * as Dialog from '@radix-ui/react-dialog'
import { zodResolver } from '@hookform/resolvers/zod'
import { X } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { z } from 'zod'
import { Button } from '@/components/ui/Button'
import { Field } from '@/components/ui/Field'
import { useCreateInvestment } from '@/hooks/queries'
import { applyServerErrors } from '@/features/auth/useServerFormErrors'
import { formatMoney } from '@/lib/format'
import type { PropertyDetail } from '@/types/api'

export function InvestDialog({ property, open, onOpenChange }: { property: PropertyDetail; open: boolean; onOpenChange: (o: boolean) => void }) {
  const min = Number(property.minimumInvestment)
  // Client-side check is UX only; the backend validates minimum, capacity, currency and eligibility.
  const schema = z.object({
    amount: z.string().trim().regex(/^\d+(\.\d{1,2})?$/, 'Enter a valid amount').refine((v) => Number(v) >= min, `Minimum is ${formatMoney(property.minimumInvestment, property.currency)}`),
  })
  const { register, handleSubmit, setError, reset, formState: { errors } } = useForm<{ amount: string }>({ resolver: zodResolver(schema) })
  const mutation = useCreateInvestment()
  const [formError, setFormError] = useState<string | null>(null)
  // One key per user intent: retries after network failure reuse it, so no duplicate investment.
  const [idempotencyKey, setIdempotencyKey] = useState(() => crypto.randomUUID())

  const onSubmit = handleSubmit(async ({ amount }) => {
    setFormError(null)
    try {
      await mutation.mutateAsync({ body: { opportunityId: property.opportunityId!, amount, currency: property.currency }, idempotencyKey })
      setIdempotencyKey(crypto.randomUUID())
      toast.success('Your investment request was received.')
      reset()
      onOpenChange(false)
    } catch (err) {
      setFormError(applyServerErrors(err, setError))
    }
  })

  return (
    <Dialog.Root open={open} onOpenChange={onOpenChange}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 z-50 bg-ink/30" />
        <Dialog.Content className="fixed left-1/2 top-1/2 z-50 w-[calc(100%-2rem)] max-w-md -translate-x-1/2 -translate-y-1/2 rounded-card bg-white p-7 shadow-xl">
          <Dialog.Close className="absolute right-4 top-4 grid size-9 place-items-center rounded-full hover:bg-mint-soft" aria-label="Close"><X className="size-4" /></Dialog.Close>
          <Dialog.Title className="text-xl font-semibold">Invest in {property.name}</Dialog.Title>
          <Dialog.Description className="mt-1 text-sm text-muted">Minimum investment {formatMoney(property.minimumInvestment, property.currency)}. Projected returns are not guaranteed.</Dialog.Description>
          <form onSubmit={onSubmit} noValidate className="mt-6 space-y-4">
            {formError && <p role="alert" className="rounded-control bg-cream px-4 py-3 text-sm text-warn">{formError}</p>}
            <Field label={`Amount (${property.currency})`} inputMode="decimal" autoComplete="off" error={errors.amount?.message} {...register('amount')} />
            <Button type="submit" className="w-full" loading={mutation.isPending}>Confirm investment</Button>
          </form>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  )
}
