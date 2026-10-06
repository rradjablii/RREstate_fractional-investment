import { Building2 } from 'lucide-react'
import { cn } from '@/lib/cn'

/** Real image when the API provides one, neutral placeholder otherwise. */
export function PropertyImage({ src, alt, className }: { src: string | null | undefined; alt: string; className?: string }) {
  return src ? (
    <img src={src} alt={alt} loading="lazy" className={cn('object-cover', className)} />
  ) : (
    <div role="img" aria-label={alt} className={cn('grid place-items-center bg-gradient-to-br from-sky to-mint text-sage', className)}>
      <Building2 className="size-8" aria-hidden />
    </div>
  )
}
