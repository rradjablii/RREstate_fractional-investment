# RREstate

Real-estate investment platform MVP: Spring Boot API + React frontend + PostgreSQL, behind Caddy.

```
backend/    Java 21, Spring Boot 4.1, Flyway, JPA, Spring Security (JWT RS256)
frontend/   React 19, TypeScript, Vite, Tailwind 4, TanStack Query
Caddyfile   reverse proxy: /api -> backend, / -> frontend
compose.yaml, compose.production.yaml
```

## Run everything (Docker)

```bash
cp .env.example .env      # set POSTGRES_PASSWORD, ADMIN_EMAIL, ADMIN_PASSWORD
docker compose up --build
# http://localhost:8081
```
The default `dev` profile generates ephemeral JWT keys and enables investing. Production **requires** `JWT_PRIVATE_KEY`/`JWT_PUBLIC_KEY` and a real `SITE_ADDRESS` (see `docs`/below).

## Local development

```bash
# database
docker run -d --name rrestate-pg -e POSTGRES_USER=rrestate -e POSTGRES_PASSWORD=rrestate -e POSTGRES_DB=rrestate -p 5432:5432 postgres:18-alpine
# backend (http://localhost:8080, Swagger UI at /swagger-ui.html in dev)
cd backend && SPRING_PROFILES_ACTIVE=dev ADMIN_EMAIL=admin@rrestate.local ADMIN_PASSWORD='change-me-please-1' ./gradlew bootRun
# frontend (http://localhost:5173, proxies /api to :8080)
cd frontend && npm install && npm run dev
```

## Tests

```bash
cd backend && ./gradlew test      # unit + PostgreSQL Testcontainers integration tests (needs Docker)
cd frontend && npm test && npm run lint && npm run build
```

## JWT keys (production)

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out jwt.pem
openssl pkcs8 -topk8 -nocrypt -in jwt.pem -outform DER | base64 | tr -d '\n'   # JWT_PRIVATE_KEY
openssl rsa -in jwt.pem -pubout -outform DER | base64 | tr -d '\n'             # JWT_PUBLIC_KEY
```
Store them in your secret manager / server `.env`; never commit them.

## Key design decisions

- **Auth**: 15-min RS256 access token (kept in browser memory only) + rotating 14-day opaque refresh token in an HttpOnly, SameSite=Strict cookie scoped to `/api/v1/auth`; reuse of a rotated token revokes all sessions. CSRF is disabled on the stateless API; rationale in `SecurityConfig`.
- **Money**: `BigDecimal` everywhere, `NUMERIC(19,4)`, wire format = decimal strings, explicit rounding in `MoneyRules`.
- **Investing**: `Idempotency-Key` + `UNIQUE(user_id, key)`; capacity reserved by an atomic SQL update (concurrency-tested); positions are created only when an operator records a verified payment. **No payment provider is integrated.**
- **Compliance gates**: `INVESTMENT_ENABLED` and `KYC_REQUIRED` (default off / on). KYC verification itself is not implemented, so with `KYC_REQUIRED=true` nobody can invest. Legal structure, securities rules and payments need professional review before any real money flows.
- **Portfolio**: computed server-side from positions and the immutable ledger; "current value" is at cost until a valuation model exists; totals cover one currency.
- **Rate limiting** is in-memory (single instance); use a shared store before scaling out.

## Not built yet

KYC flow, payment provider + signed webhooks, document upload/download storage, notifications, admin UI forms (admin API exists), password reset, backups automation (see below), CI pipeline.

## Backups (production)

`docker compose exec -T postgres pg_dump -U $POSTGRES_USER $POSTGRES_DB | gzip > backup-$(date +%F).sql.gz`, copy off-server, and test a restore regularly. Docker volumes alone are not a backup.
