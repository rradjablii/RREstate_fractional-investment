import type { FieldValues, Path, UseFormSetError } from 'react-hook-form'
import { ApiError, errorMessage } from '@/api/errors'

/** Maps backend validationErrors onto form fields; returns a general message otherwise. */
export function applyServerErrors<T extends FieldValues>(err: unknown, setError: UseFormSetError<T>): string | null {
  if (err instanceof ApiError && Object.keys(err.fieldErrors).length) {
    for (const [field, message] of Object.entries(err.fieldErrors)) setError(field as Path<T>, { message })
    return null
  }
  return errorMessage(err)
}
