CREATE TABLE investments (
    id              UUID PRIMARY KEY,
    user_id         UUID          NOT NULL REFERENCES users (id),
    opportunity_id  UUID          NOT NULL REFERENCES investment_opportunities (id),
    property_id     UUID          NOT NULL REFERENCES properties (id),
    amount          NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    currency        VARCHAR(3)       NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    interest_percent NUMERIC(12,6),
    idempotency_key VARCHAR(100)  NOT NULL,
    investment_date TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL,
    updated_at      TIMESTAMPTZ   NOT NULL,
    CONSTRAINT uq_investment_idempotency UNIQUE (user_id, idempotency_key)
);
CREATE INDEX ix_investments_user_created ON investments (user_id, created_at DESC);
CREATE INDEX ix_investments_status ON investments (status);

-- Aggregated confirmed position of a user in a property.
CREATE TABLE investor_positions (
    id               UUID PRIMARY KEY,
    user_id          UUID          NOT NULL REFERENCES users (id),
    property_id      UUID          NOT NULL REFERENCES properties (id),
    invested_amount  NUMERIC(19,4) NOT NULL,
    interest_percent NUMERIC(12,6) NOT NULL,
    currency         VARCHAR(3)       NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL,
    updated_at       TIMESTAMPTZ   NOT NULL,
    CONSTRAINT uq_position UNIQUE (user_id, property_id)
);

-- Immutable financial event log (append-only).
CREATE TABLE transactions (
    id            UUID PRIMARY KEY,
    user_id       UUID          NOT NULL REFERENCES users (id),
    type          VARCHAR(40)   NOT NULL,
    status        VARCHAR(20)   NOT NULL,
    amount        NUMERIC(19,4) NOT NULL,
    currency      VARCHAR(3)       NOT NULL,
    property_id   UUID REFERENCES properties (id),
    investment_id UUID REFERENCES investments (id),
    reference     VARCHAR(100),
    created_at    TIMESTAMPTZ   NOT NULL
);
CREATE INDEX ix_transactions_user_created ON transactions (user_id, created_at DESC);

CREATE TABLE distributions (
    id           UUID PRIMARY KEY,
    property_id  UUID          NOT NULL REFERENCES properties (id),
    total_amount NUMERIC(19,4) NOT NULL CHECK (total_amount > 0),
    currency     VARCHAR(3)       NOT NULL,
    period_start DATE          NOT NULL,
    period_end   DATE          NOT NULL,
    status       VARCHAR(20)   NOT NULL,
    created_by   UUID          NOT NULL REFERENCES users (id),
    created_at   TIMESTAMPTZ   NOT NULL,
    approved_at  TIMESTAMPTZ
);
CREATE INDEX ix_distributions_property ON distributions (property_id);

CREATE TABLE distribution_allocations (
    id              UUID PRIMARY KEY,
    distribution_id UUID          NOT NULL REFERENCES distributions (id),
    user_id         UUID          NOT NULL REFERENCES users (id),
    position_id     UUID          NOT NULL REFERENCES investor_positions (id),
    amount          NUMERIC(19,4) NOT NULL,
    currency        VARCHAR(3)       NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    paid_at         TIMESTAMPTZ,
    CONSTRAINT uq_allocation UNIQUE (distribution_id, position_id)
);
CREATE INDEX ix_allocations_user ON distribution_allocations (user_id, status);

CREATE TABLE audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    actor_id    UUID,
    action      VARCHAR(60)  NOT NULL,
    entity_type VARCHAR(40),
    entity_id   VARCHAR(64),
    details     VARCHAR(500),
    ip_address  VARCHAR(64),
    request_id  VARCHAR(64),
    created_at  TIMESTAMPTZ  NOT NULL
);
CREATE INDEX ix_audit_created ON audit_logs (created_at DESC);
CREATE INDEX ix_audit_actor ON audit_logs (actor_id);
