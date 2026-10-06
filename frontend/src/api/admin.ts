import type { AdminInvestment, Page, PropertySummary } from '@/types/api'
import { api } from './client'

export const adminApi = {
  properties: (page = 0, size = 20) => api.get<Page<PropertySummary>>('/admin/properties', { page, size }),
  investments: (page = 0, size = 20) => api.get<Page<AdminInvestment>>('/admin/investments', { page, size }),
}
