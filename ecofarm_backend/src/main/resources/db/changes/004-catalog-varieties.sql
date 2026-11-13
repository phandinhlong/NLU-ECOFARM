--liquibase formatted sql

--changeset ecofarm:004-catalog-varieties
--validCheckSum: ANY
--comment: FR-MST-002 - bảng giống dưa (mã, tên, ngày thụ phấn đến thu hoạch, ngưỡng Brix, ngày cách ly)
CREATE TABLE IF NOT EXISTS varieties (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    days_pollination_to_harvest INTEGER,
    min_brix NUMERIC(3, 1),
    min_isolation_days INTEGER,
    version BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    updated_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
);

-- Bảng varieties cũ (public_portable.sql) chưa có các cột nghiệp vụ của FR-MST-002
ALTER TABLE varieties ADD COLUMN IF NOT EXISTS days_pollination_to_harvest INTEGER;
ALTER TABLE varieties ADD COLUMN IF NOT EXISTS min_brix NUMERIC(3, 1);
ALTER TABLE varieties ADD COLUMN IF NOT EXISTS min_isolation_days INTEGER;
--rollback ALTER TABLE varieties DROP COLUMN IF EXISTS days_pollination_to_harvest;
--rollback ALTER TABLE varieties DROP COLUMN IF EXISTS min_brix;
--rollback ALTER TABLE varieties DROP COLUMN IF EXISTS min_isolation_days;
--rollback DROP TABLE IF EXISTS varieties;
