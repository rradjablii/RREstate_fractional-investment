CREATE TABLE properties (
    id                UUID PRIMARY KEY,
    name              VARCHAR(200)  NOT NULL,
    country           VARCHAR(80)   NOT NULL,
    city              VARCHAR(120)  NOT NULL,
    address           VARCHAR(300),
    property_type     VARCHAR(60)   NOT NULL,
    description       TEXT          NOT NULL,
    status            VARCHAR(20)   NOT NULL,
    currency          VARCHAR(3)       NOT NULL,
    acquisition_price NUMERIC(19,4) NOT NULL,
    valuation         NUMERIC(19,4),
    expected_yield_percent NUMERIC(7,3) NOT NULL,
    projected_net_income   NUMERIC(19,4),
    legal_structure   VARCHAR(200),
    entity_name       VARCHAR(200),
    created_at        TIMESTAMPTZ   NOT NULL,
    updated_at        TIMESTAMPTZ   NOT NULL
);
CREATE INDEX ix_properties_status_created ON properties (status, created_at DESC);

CREATE TABLE investment_opportunities (
    id                 UUID PRIMARY KEY,
    property_id        UUID          NOT NULL UNIQUE REFERENCES properties (id),
    minimum_investment NUMERIC(19,4) NOT NULL CHECK (minimum_investment > 0),
    target_raise       NUMERIC(19,4) NOT NULL CHECK (target_raise > 0),
    raised_amount      NUMERIC(19,4) NOT NULL DEFAULT 0,
    investment_enabled BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_raised_within_target CHECK (raised_amount >= 0 AND raised_amount <= target_raise)
);

CREATE TABLE property_images (
    id          UUID PRIMARY KEY,
    property_id UUID NOT NULL REFERENCES properties (id),
    url         VARCHAR(1000) NOT NULL,
    caption     VARCHAR(200),
    sort_order  INT NOT NULL DEFAULT 0
);
CREATE INDEX ix_property_images_property ON property_images (property_id, sort_order);

CREATE TABLE property_documents (
    id          UUID PRIMARY KEY,
    property_id UUID NOT NULL REFERENCES properties (id),
    title       VARCHAR(200) NOT NULL,
    category    VARCHAR(40)  NOT NULL,
    size_bytes  BIGINT,
    created_at  TIMESTAMPTZ  NOT NULL
);
CREATE INDEX ix_property_documents_property ON property_documents (property_id);

CREATE TABLE property_financial_lines (
    id          UUID PRIMARY KEY,
    property_id UUID NOT NULL REFERENCES properties (id),
    label       VARCHAR(120) NOT NULL,
    amount      NUMERIC(19,4) NOT NULL,
    basis       VARCHAR(20) NOT NULL,
    sort_order  INT NOT NULL DEFAULT 0
);
CREATE INDEX ix_property_fin_property ON property_financial_lines (property_id, sort_order);

CREATE TABLE property_risks (
    property_id UUID NOT NULL REFERENCES properties (id),
    sort_order  INT  NOT NULL,
    text        VARCHAR(1000) NOT NULL,
    PRIMARY KEY (property_id, sort_order)
);
CREATE TABLE property_disclosures (
    property_id UUID NOT NULL REFERENCES properties (id),
    sort_order  INT  NOT NULL,
    text        VARCHAR(2000) NOT NULL,
    PRIMARY KEY (property_id, sort_order)
);
