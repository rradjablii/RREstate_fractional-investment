/**
 * API contract (frontend view of /api/v1).
 *
 * The backend is not in this repository yet; endpoint paths come from the
 * RREstate master prompt (§16) and DTO shapes below are the contract this
 * frontend expects. Keep in sync with the backend OpenAPI document.
 *
 * Money and percentages are decimal STRINGS (serialized BigDecimal) — the
 * browser never does financial arithmetic on them.
 */

export type Decimal = string
export type CurrencyCode = string // ISO 4217, e.g. "EUR"
export type Role = 'USER' | 'ADMIN' | 'OPERATOR' | 'COMPLIANCE'

export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ValidationError {
  field: string
  message: string
}

export interface ErrorResponse {
  timestamp?: string
  status: number
  errorCode: string
  message: string
  path?: string
  correlationId?: string
  validationErrors?: ValidationError[]
}

export interface User {
  id: string
  email: string
  firstName: string
  lastName: string
  roles: Role[]
  kycStatus?: 'NOT_STARTED' | 'PENDING' | 'VERIFIED' | 'REJECTED'
}

export interface AuthResponse {
  accessToken: string
  expiresInSeconds: number
  user: User
}

export interface LoginRequest { email: string; password: string }
export interface RegisterRequest extends LoginRequest { firstName: string; lastName: string }

export type PropertyStatus = 'DRAFT' | 'COMING_SOON' | 'OPEN' | 'FUNDED' | 'CLOSED'

export interface PropertySummary {
  id: string
  name: string
  country: string
  city: string
  propertyType: string
  status: PropertyStatus
  coverImageUrl: string | null
  currency: CurrencyCode
  expectedYieldPercent: Decimal
  minimumInvestment: Decimal
  targetRaise: Decimal
  raisedAmount: Decimal
  fundingProgressPercent: Decimal
}

export interface FinancialLine { label: string; amount: Decimal; basis: 'PROJECTED' | 'ESTIMATED' | 'HISTORICAL' | 'ACTUAL' }

export interface PropertyDocumentRef { id: string; title: string; category: string; sizeBytes?: number }

export interface PropertyDetail extends PropertySummary {
  description: string
  address: string | null
  acquisitionPrice: Decimal
  valuation: Decimal | null
  opportunityId: string | null
  investmentEnabled: boolean
  legalStructure: string | null
  entityName: string | null
  financials: FinancialLine[]
  projectedNetIncome: Decimal | null
  images: { id: string; url: string; caption?: string }[]
  risks: string[]
  disclosures: string[]
  documents: PropertyDocumentRef[]
}

export interface CreateInvestmentRequest {
  opportunityId: string
  amount: Decimal
  currency: CurrencyCode
}

export type InvestmentStatus = 'PENDING' | 'PAYMENT_PENDING' | 'CONFIRMED' | 'FAILED' | 'REFUNDED'

export interface Investment {
  id: string
  propertyId: string
  propertyName: string
  amount: Decimal
  currency: CurrencyCode
  status: InvestmentStatus
  interestPercent: Decimal | null
  investmentDate: string | null
  createdAt: string
}

export interface AllocationSlice { propertyId: string; propertyName: string; percent: Decimal }
export interface PerformancePoint { date: string; value: Decimal }

export interface PortfolioPosition {
  propertyId: string
  propertyName: string
  country: string
  coverImageUrl: string | null
  currency: CurrencyCode
  investedAmount: Decimal
  interestPercent: Decimal
  monthlyDistribution: Decimal | null
  status: string
}

export interface Portfolio {
  currency: CurrencyCode
  totalInvested: Decimal
  totalDistributions: Decimal
  currentValue: Decimal
  totalReturnPercent: Decimal | null
  allocation: AllocationSlice[]
  performance: PerformancePoint[]
  positions: PortfolioPosition[]
}

export interface Transaction {
  id: string
  type: string
  status: string
  amount: Decimal
  currency: CurrencyCode
  propertyName: string | null
  reference: string | null
  createdAt: string
}

export interface Distribution {
  id: string
  propertyName: string
  amount: Decimal
  currency: CurrencyCode
  status: string
  periodStart: string | null
  periodEnd: string | null
  paidAt: string | null
}

export interface DocumentItem {
  id: string
  title: string
  category: string
  propertyName: string | null
  createdAt: string
}

export interface AdminInvestment extends Investment { userEmail: string }
