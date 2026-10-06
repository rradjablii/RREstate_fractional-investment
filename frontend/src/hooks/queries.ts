import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { adminApi } from '@/api/admin'
import { investmentApi } from '@/api/investments'
import { documentApi, distributionApi, portfolioApi, transactionApi } from '@/api/portfolio'
import { propertyApi, type PropertyFilters } from '@/api/properties'
import type { CreateInvestmentRequest } from '@/types/api'

export const keys = {
  properties: (f: PropertyFilters) => ['properties', f] as const,
  property: (id: string) => ['property', id] as const,
  portfolio: ['portfolio'] as const,
  transactions: (p: number) => ['transactions', p] as const,
  distributions: (p: number) => ['distributions', p] as const,
  documents: (p: number) => ['documents', p] as const,
  adminProperties: (p: number) => ['admin', 'properties', p] as const,
  adminInvestments: (p: number) => ['admin', 'investments', p] as const,
}

export const useProperties = (f: PropertyFilters) =>
  useQuery({ queryKey: keys.properties(f), queryFn: () => propertyApi.list(f), placeholderData: keepPreviousData })

export const useProperty = (id: string) => useQuery({ queryKey: keys.property(id), queryFn: () => propertyApi.get(id) })

export const usePortfolio = () => useQuery({ queryKey: keys.portfolio, queryFn: portfolioApi.me })

export const useTransactions = (page: number) =>
  useQuery({ queryKey: keys.transactions(page), queryFn: () => transactionApi.list(page), placeholderData: keepPreviousData })

export const useDistributions = (page: number) =>
  useQuery({ queryKey: keys.distributions(page), queryFn: () => distributionApi.list(page), placeholderData: keepPreviousData })

export const useDocuments = (page: number) =>
  useQuery({ queryKey: keys.documents(page), queryFn: () => documentApi.list(page), placeholderData: keepPreviousData })

export const useAdminProperties = (page: number) =>
  useQuery({ queryKey: keys.adminProperties(page), queryFn: () => adminApi.properties(page), placeholderData: keepPreviousData })

export const useAdminInvestments = (page: number) =>
  useQuery({ queryKey: keys.adminInvestments(page), queryFn: () => adminApi.investments(page), placeholderData: keepPreviousData })

export function useCreateInvestment() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ body, idempotencyKey }: { body: CreateInvestmentRequest; idempotencyKey: string }) =>
      investmentApi.create(body, idempotencyKey),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: keys.portfolio })
      void qc.invalidateQueries({ queryKey: ['properties'] })
      void qc.invalidateQueries({ queryKey: ['property'] })
      void qc.invalidateQueries({ queryKey: ['transactions'] })
    },
  })
}
