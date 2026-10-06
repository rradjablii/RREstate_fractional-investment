import { formatMoney, formatPercent } from './format'

describe('format', () => {
  it('formats whole and fractional money', () => {
    expect(formatMoney('3450', 'USD')).toBe('$3,450')
    expect(formatMoney('7.5', 'USD')).toBe('$7.50')
  })
  it('handles missing values', () => {
    expect(formatMoney(null)).toBe('—')
    expect(formatPercent(undefined)).toBe('—')
  })
  it('formats percent', () => expect(formatPercent('7.84')).toBe('7.8%'))
})
