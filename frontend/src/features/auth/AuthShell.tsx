import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { Card } from '@/components/ui/Card'
import { Logo } from '@/components/ui/Logo'

export function AuthShell({ title, subtitle, children, footer }: { title: string; subtitle: string; children: ReactNode; footer: ReactNode }) {
  return (
    <div className="grid min-h-screen place-items-center bg-gradient-to-b from-mint-soft to-ivory px-5 py-10">
      <div className="w-full max-w-md">
        <Link to="/" className="mb-8 flex justify-center" aria-label="RREstate home"><Logo size="lg" /></Link>
        <Card className="p-7 sm:p-9">
          <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
          <p className="mt-1 text-sm text-muted">{subtitle}</p>
          <div className="mt-6">{children}</div>
        </Card>
        <p className="mt-6 text-center text-sm text-muted">{footer}</p>
      </div>
    </div>
  )
}
