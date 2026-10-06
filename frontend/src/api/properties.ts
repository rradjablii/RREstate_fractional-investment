import type { Page, PropertyDetail, PropertySummary, PropertyStatus } from '@/types/api'
import { api } from './client'

export interface PropertyFilters { page?: number; size?: number; country?: string; status?: PropertyStatus }

export const propertyApi = {
  list: (f: PropertyFilters = {}) => api.get<Page<PropertySummary>>('/properties', { ...f }),
  get: (id: string) => api.get<PropertyDetail>(`/properties/${encodeURIComponent(id)}`),
}
