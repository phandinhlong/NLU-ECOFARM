--liquibase formatted sql

--changeset ecofarm:010-qc-harvest-schema
-- Harvest: exact event instant in epoch ms (harvest_date stays as the display-only DATE).
ALTER TABLE batches ADD COLUMN IF NOT EXISTS harvested_at BIGINT;

-- QC: persist the basis used so a later log back-fill/amendment can invalidate a stale pass,
-- plus engineer note, size description, and the computed status. attachments reuse the
-- existing event_attachments store via the harvest/QC event, so no new table here.
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS size_description VARCHAR(255);
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS note TEXT;
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS threshold_brix_used DOUBLE PRECISION;
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS min_isolation_days_used INTEGER;
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS isolation_eligible_at BIGINT;
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS harvested_at_used BIGINT;
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS status VARCHAR(32);
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS superseded BOOLEAN DEFAULT FALSE;
