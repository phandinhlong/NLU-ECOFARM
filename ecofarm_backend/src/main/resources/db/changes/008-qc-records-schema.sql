--liquibase formatted sql

--changeset ecofarm:008-qc-records-schema splitStatements:false
--validCheckSum: ANY
--comment: Bang qc_records va cac cot mo rong cho nghiep vu kiem dinh chat luong (QC)
CREATE TABLE IF NOT EXISTS qc_records (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT,
    brix DOUBLE PRECISION,
    avg_weight_kg DOUBLE PRECISION,
    passed BOOLEAN,
    inspected_by BIGINT,
    inspected_at BIGINT,
    attempt_number INTEGER,
    failure_reason VARCHAR(255),
    deficit_days INTEGER,
    sample_brix TEXT,
    sample_weights TEXT,
    status VARCHAR(64),
    admin_decision VARCHAR(32),
    admin_note TEXT,
    reviewed_by BIGINT,
    reviewed_at BIGINT,
    client_event_id VARCHAR(255),
    photo_hashes TEXT,
    system_suggested_passed BOOLEAN,
    criteria_evaluation TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    updated_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
);

ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS attempt_number INTEGER;
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS failure_reason VARCHAR(255);
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS deficit_days INTEGER;
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS sample_brix TEXT;
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS sample_weights TEXT;
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS status VARCHAR(64);
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS admin_decision VARCHAR(32);
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS admin_note TEXT;
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS reviewed_by BIGINT;
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS reviewed_at BIGINT;
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS client_event_id VARCHAR(255);
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS photo_hashes TEXT;
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS system_suggested_passed BOOLEAN;
ALTER TABLE qc_records ADD COLUMN IF NOT EXISTS criteria_evaluation TEXT;

CREATE INDEX IF NOT EXISTS idx_qc_records_batch_id ON qc_records (batch_id, id DESC);
CREATE INDEX IF NOT EXISTS idx_qc_records_client_event_id ON qc_records (client_event_id);
