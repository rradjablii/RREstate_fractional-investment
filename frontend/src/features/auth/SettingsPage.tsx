import { PageHeader } from '@/components/layout/PageHeader'
import { Badge, statusTone } from '@/components/ui/Badge'
import { Card } from '@/components/ui/Card'
import { humanize } from '@/lib/format'
import { useSession } from '@/stores/session'

export default function SettingsPage() {
  const user = useSession((s) => s.user)!
  const rows: [string, React.ReactNode][] = [
    ['Name', `${user.firstName} ${user.lastName}`],
    ['Email', user.email],
    ['Identity verification', <Badge tone={statusTone(user.kycStatus ?? 'NOT_STARTED')}>{humanize(user.kycStatus ?? 'NOT_STARTED')}</Badge>],
  ]
  return (
    <div>
      <PageHeader title="Settings" subtitle="Your account details." />
      <Card className="max-w-2xl divide-y divide-line">
        {rows.map(([k, v]) => <div key={k} className="flex items-center justify-between gap-4 px-6 py-4 text-sm"><span className="text-muted">{k}</span><span className="font-medium">{v}</span></div>)}
      </Card>
    </div>
  )
}
