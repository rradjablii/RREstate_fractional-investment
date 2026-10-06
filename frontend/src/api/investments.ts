import type { CreateInvestmentRequest, Investment, Page } from '@/types/api'
import { api } from './client'

export const investmentApi = {
  /** idempotencyKey must be stable across retries of the same user intent. */
  create: (body: CreateInvestmentRequest, idempotencyKey: string) =>
    api.post<Investment>('/investments', body, { headers: { 'Idempotency-Key': idempotencyKey } }),
  list: (page = 0, size = 20) => api.get<Page<Investment>>('/investments', { page, size }),
}
