import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { authApi } from '@/api/auth'
import { Button } from '@/components/ui/Button'
import { Field } from '@/components/ui/Field'
import { loginSchema, type LoginForm } from '@/schemas/auth'
import { AuthShell } from './AuthShell'
import { applyServerErrors } from './useServerFormErrors'

export default function LoginPage() {
  const navigate = useNavigate()
  const from = (useLocation().state as { from?: string } | null)?.from ?? '/portfolio'
  const [formError, setFormError] = useState<string | null>(null)
  const { register, handleSubmit, setError, formState: { errors, isSubmitting } } = useForm<LoginForm>({ resolver: zodResolver(loginSchema) })

  const onSubmit = handleSubmit(async (values) => {
    setFormError(null)
    try {
      await authApi.login(values)
      navigate(from, { replace: true })
    } catch (err) {
      setFormError(applyServerErrors(err, setError))
    }
  })

  return (
    <AuthShell title="Welcome back" subtitle="Log in to track your investments." footer={<>New to RREstate? <Link to="/register" className="font-medium text-sage-dark hover:underline">Create an account</Link></>}>
      <form onSubmit={onSubmit} noValidate className="space-y-4">
        {formError && <p role="alert" className="rounded-control bg-cream px-4 py-3 text-sm text-warn">{formError}</p>}
        <Field label="Email" type="email" autoComplete="email" error={errors.email?.message} {...register('email')} />
        <Field label="Password" type="password" autoComplete="current-password" error={errors.password?.message} {...register('password')} />
        <Button type="submit" className="w-full" loading={isSubmitting}>Log in</Button>
      </form>
    </AuthShell>
  )
}
