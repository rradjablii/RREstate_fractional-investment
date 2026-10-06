import { useSession } from '@/stores/session'
import { ApiError } from './errors'
import { api } from './client'

const user = { id: '1', email: 'a@b.c', firstName: 'A', lastName: 'B', roles: ['USER' as const] }
const json = (status: number, body: unknown) => new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })

afterEach(() => { vi.restoreAllMocks(); useSession.getState().clear() })

describe('api client', () => {
  it('refreshes once on 401 and retries the request', async () => {
    useSession.getState().setSession(user, 'old', 60)
    const fetchMock = vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(json(401, { status: 401, errorCode: 'UNAUTHENTICATED', message: 'expired' }))
      .mockResolvedValueOnce(json(200, { accessToken: 'new', expiresInSeconds: 60, user }))
      .mockResolvedValueOnce(json(200, { ok: true }))
    await expect(api.get('/portfolio/me')).resolves.toEqual({ ok: true })
    expect(fetchMock).toHaveBeenCalledTimes(3)
    expect(useSession.getState().accessToken).toBe('new')
  })

  it('maps error bodies to ApiError with field errors', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(json(400, { status: 400, errorCode: 'VALIDATION_ERROR', message: 'bad', validationErrors: [{ field: 'amount', message: 'too low' }] }))
    const err = await api.post('/investments', {}).catch((e: unknown) => e) as ApiError
    expect(err).toBeInstanceOf(ApiError)
    expect(err.fieldErrors).toEqual({ amount: 'too low' })
  })

  it('reports network failures safely', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('boom'))
    await expect(api.get('/properties')).rejects.toMatchObject({ errorCode: 'NETWORK_ERROR' })
  })
})
