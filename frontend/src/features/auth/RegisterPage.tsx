import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '@/api/auth'
import { Button } from '@/components/ui/Button'
import { Field } from '@/components/ui/Field'
import { registerSchema, type RegisterForm } from '@/schemas/auth'
import { AuthShell } from './AuthShell'
import { applyServerErrors } from './useServerFormErrors'

export default function RegisterPage() {
  const navigate = useNavigate()
  const [formError, setFormError] = useState<string | null>(null)
  const { register, handleSubmit, setError, formState: { errors, isSubmitting } } = useForm<RegisterForm>({ resolver: zodResolver(registerSchema) })

  const onSubmit = handleSubmit(async (values) => {
    setFormError(null)
    try {
      await authApi.register(values)
      navigate('/portfolio', { replace: true })
    } catch (err) {
      setFormError(applyServerErrors(err, setError))
    }
  })

  return (
    <AuthShell title="Create your account" subtitle="Start exploring opportunities in minutes." footer={<>Already have an account? <Link to="/login" className="font-medium text-sage-dark hover:underline">Log in</Link></>}>
      <form onSubmit={onSubmit} noValidate className="space-y-4">
        {formError && <p role="alert" className="rounded-control bg-cream px-4 py-3 text-sm text-warn">{formError}</p>}
        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="First name" autoComplete="given-name" error={errors.firstName?.message} {...register('firstName')} />
          <Field label="Last name" autoComplete="family-name" error={errors.lastName?.message} {...register('lastName')} />
        </div>
        <Field label="Email" type="email" autoComplete="email" error={errors.email?.message} {...register('email')} />
        <Field label="Password" type="password" autoComplete="new-password" hint="At least 12 characters." error={errors.password?.message} {...register('password')} />
        <Button type="submit" className="w-full" loading={isSubmitting}>Create account</Button>
        <p className="text-xs text-muted">Identity verification may be required before you can invest.</p>
      </form>
    </AuthShell>
  )
}
