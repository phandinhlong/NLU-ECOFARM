--liquibase formatted sql

--changeset ecofarm:008-cultivation-schema
CREATE TABLE IF NOT EXISTS cultivation_events (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    batch_id BIGINT,
    seq INTEGER,
    event_type VARCHAR(255),
    payload BIGINT,
    payload_json TEXT,
    event_hash VARCHAR(255),
    status VARCHAR(255),
    recorded_by BIGINT,
    recorded_at BIGINT,
    occurred_at BIGINT,
    client_event_id VARCHAR(255),
    amends_event_id BIGINT
);

ALTER TABLE cultivation_events ADD COLUMN IF NOT EXISTS occurred_at BIGINT;
ALTER TABLE cultivation_events ADD COLUMN IF NOT EXISTS payload_json TEXT;
ALTER TABLE cultivation_events ADD COLUMN IF NOT EXISTS client_event_id VARCHAR(255);
ALTER TABLE cultivation_events ADD COLUMN IF NOT EXISTS amends_event_id BIGINT;
CREATE UNIQUE INDEX IF NOT EXISTS uq_cultivation_events_batch_seq ON cultivation_events (batch_id, seq);
CREATE UNIQUE INDEX IF NOT EXISTS uq_cultivation_events_client_id ON cultivation_events (client_event_id);

CREATE TABLE IF NOT EXISTS event_attachments (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    event_id BIGINT,
    file_key VARCHAR(255) NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    mime_type VARCHAR(255) NOT NULL
);
