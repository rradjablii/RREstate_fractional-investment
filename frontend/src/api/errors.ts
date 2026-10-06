import type { ErrorResponse } from '@/types/api'

export class ApiError extends Error {
  readonly status: number
  readonly errorCode: string
  readonly correlationId?: string
  readonly fieldErrors: Record<string, string>

  constructor(body: ErrorResponse) {
    super(body.message)
    this.name = 'ApiError'
    this.status = body.status
    this.errorCode = body.errorCode
    this.correlationId = body.correlationId
    this.fieldErrors = Object.fromEntries((body.validationErrors ?? []).map((v) => [v.field, v.message]))
  }
}

/** User-safe message for any thrown value. */
export function errorMessage(err: unknown): string {
  if (err instanceof ApiError) {
    return err.correlationId ? `${err.message} (ref ${err.correlationId})` : err.message
  }
  return 'Something went wrong. Please try again.'
}
