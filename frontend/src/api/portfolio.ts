import type { Distribution, DocumentItem, Page, Portfolio, Transaction } from '@/types/api'
import { api } from './client'

export const portfolioApi = { me: () => api.get<Portfolio>('/portfolio/me') }
export const transactionApi = { list: (page = 0, size = 20) => api.get<Page<Transaction>>('/transactions', { page, size }) }
export const distributionApi = { list: (page = 0, size = 20) => api.get<Page<Distribution>>('/distributions', { page, size }) }
export const documentApi = { list: (page = 0, size = 20) => api.get<Page<DocumentItem>>('/documents', { page, size }) }
