-- Flyway baseline migration: creates all tables required for Step 1 of the
-- SOQL Explorer roadmap. Subsequent steps add columns/tables as
-- separate V*__*.sql files; this one must never be edited once applied.
--
-- Naming convention: snake_case tables and columns, UUID primary keys.
-- All timestamps are TIMESTAMPTZ stored in UTC.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

----------------------------------------------------------------------------
-- app_user
----------------------------------------------------------------------------
CREATE TABLE app_user (
    id                  UUID         PRIMARY KEY,
    email               VARCHAR(254) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    enabled             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL
);

CREATE UNIQUE INDEX idx_app_user_email ON app_user (LOWER(email));

CREATE TABLE app_user_role (
    user_id             UUID         NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role                VARCHAR(32)  NOT NULL,
    PRIMARY KEY (user_id, role)
);

----------------------------------------------------------------------------
-- sf_connection
----------------------------------------------------------------------------
CREATE TABLE sf_connection (
    id                  UUID         PRIMARY KEY,
    owner_id            UUID         NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    org_id              VARCHAR(18)  NOT NULL,
    instance_url        VARCHAR(512) NOT NULL,
    environment         VARCHAR(16)  NOT NULL,
    display_name        VARCHAR(128) NOT NULL,
    refresh_token       BYTEA        NOT NULL,
    is_default          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_sf_connection_owner ON sf_connection (owner_id);
CREATE UNIQUE INDEX idx_sf_connection_owner_org ON sf_connection (owner_id, org_id);
-- At most one default connection per user. Partial unique index does it cleanly.
CREATE UNIQUE INDEX idx_sf_connection_one_default
    ON sf_connection (owner_id)
    WHERE is_default = TRUE;

----------------------------------------------------------------------------
-- query_history
----------------------------------------------------------------------------
CREATE TABLE query_history (
    id                  UUID         PRIMARY KEY,
    user_id             UUID         NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    connection_id       UUID         NOT NULL REFERENCES sf_connection(id) ON DELETE CASCADE,
    soql                TEXT         NOT NULL,
    rows_returned       INTEGER      NOT NULL DEFAULT 0,
    execution_ms        INTEGER      NOT NULL,
    status              VARCHAR(16)  NOT NULL,
    error_message       TEXT,
    executed_at         TIMESTAMPTZ  NOT NULL,
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX idx_query_history_user_exec ON query_history (user_id, executed_at DESC)
    WHERE deleted_at IS NULL;

----------------------------------------------------------------------------
-- query_template
----------------------------------------------------------------------------
CREATE TABLE query_template (
    id                  UUID         NOT NULL,
    version             INTEGER      NOT NULL,
    owner_id            UUID         NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    name                VARCHAR(200) NOT NULL,
    soql                TEXT         NOT NULL,
    -- Tags stored as a delimited TEXT blob for portability (H2/Testcontainers cross-compat).
    -- The application layer normalizes (sorted, lower-cased, non-blank) before persisting.
    tags                TEXT         NOT NULL DEFAULT '',
    shared              BOOLEAN      NOT NULL DEFAULT FALSE,
    is_latest           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (id, version)
);

CREATE INDEX idx_query_template_owner_latest ON query_template (owner_id) WHERE is_latest = TRUE;
CREATE INDEX idx_query_template_shared_latest ON query_template (shared) WHERE is_latest = TRUE AND shared = TRUE;
