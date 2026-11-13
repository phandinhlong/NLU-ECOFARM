--liquibase formatted sql

--changeset ecofarm:003-packaging-schema splitStatements:false
--validCheckSum: ANY
--comment: Bo sung sequence, cot theo doi quet/reprint QR, bang qr_scan_logs va role PACKER cho module Packaging
DO $$
BEGIN
    -- Role PACKER + gan cho tai khoan admin test
    INSERT INTO roles (role_name) VALUES ('PACKER') ON CONFLICT (role_name) DO NOTHING;

    INSERT INTO user_roles (user_id, role_id)
    SELECT u.id, r.id
    FROM users u, roles r
    WHERE u.email IN ('systemadmin@gmail.com', 'admin@ecofarm.com', 'admin_demo')
      AND r.role_name = 'PACKER'
    ON CONFLICT DO NOTHING;

    -- Bang packages
    CREATE TABLE IF NOT EXISTS packages (
        id BIGSERIAL PRIMARY KEY,
        batch_id BIGINT NOT NULL,
        package_code VARCHAR(100) UNIQUE,
        unit_count INTEGER NOT NULL,
        packed_by BIGINT,
        packed_at BIGINT,
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        version INTEGER NOT NULL DEFAULT 1,
        created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
        updated_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
    );
    -- Chi gan sequence khi bang cu chua co default/identity (Hibernate update tao IDENTITY)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'packages' AND column_name = 'id'
          AND (is_identity = 'YES' OR column_default IS NOT NULL)
    ) THEN
        CREATE SEQUENCE IF NOT EXISTS packages_id_seq;
        PERFORM setval('packages_id_seq', COALESCE((SELECT MAX(id) FROM packages), 0) + 1, false);
        ALTER TABLE packages ALTER COLUMN id SET DEFAULT nextval('packages_id_seq');
    END IF;

    -- Bang qr_codes
    CREATE TABLE IF NOT EXISTS qr_codes (
        id BIGSERIAL PRIMARY KEY,
        package_id BIGINT NOT NULL,
        code VARCHAR(64) NOT NULL UNIQUE,
        serial INTEGER NOT NULL,
        status VARCHAR(32) NOT NULL DEFAULT 'GENERATED',
        printed_at BIGINT,
        scan_count INTEGER NOT NULL DEFAULT 0,
        last_scanned_at BIGINT,
        reprint_count INTEGER NOT NULL DEFAULT 0,
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        version INTEGER NOT NULL DEFAULT 1,
        created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
        updated_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
    );
    ALTER TABLE qr_codes ADD COLUMN IF NOT EXISTS scan_count INTEGER NOT NULL DEFAULT 0;
    ALTER TABLE qr_codes ADD COLUMN IF NOT EXISTS last_scanned_at BIGINT;
    ALTER TABLE qr_codes ADD COLUMN IF NOT EXISTS reprint_count INTEGER NOT NULL DEFAULT 0;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'qr_codes' AND column_name = 'id'
          AND (is_identity = 'YES' OR column_default IS NOT NULL)
    ) THEN
        CREATE SEQUENCE IF NOT EXISTS qr_codes_id_seq;
        PERFORM setval('qr_codes_id_seq', COALESCE((SELECT MAX(id) FROM qr_codes), 0) + 1, false);
        ALTER TABLE qr_codes ALTER COLUMN id SET DEFAULT nextval('qr_codes_id_seq');
    END IF;
    CREATE INDEX IF NOT EXISTS idx_qr_codes_package_serial ON qr_codes (package_id, serial);

    -- Bang cultivation_events (dam bao co sequence neu bang da ton tai tu dump)
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'cultivation_events'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'cultivation_events' AND column_name = 'id'
          AND (is_identity = 'YES' OR column_default IS NOT NULL)
    ) THEN
        CREATE SEQUENCE IF NOT EXISTS cultivation_events_id_seq;
        PERFORM setval('cultivation_events_id_seq', COALESCE((SELECT MAX(id) FROM cultivation_events), 0) + 1, false);
        ALTER TABLE cultivation_events ALTER COLUMN id SET DEFAULT nextval('cultivation_events_id_seq');
    END IF;

    -- Bang qc_records
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'qc_records'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'qc_records' AND column_name = 'id'
          AND (is_identity = 'YES' OR column_default IS NOT NULL)
    ) THEN
        CREATE SEQUENCE IF NOT EXISTS qc_records_id_seq;
        PERFORM setval('qc_records_id_seq', COALESCE((SELECT MAX(id) FROM qc_records), 0) + 1, false);
        ALTER TABLE qc_records ALTER COLUMN id SET DEFAULT nextval('qc_records_id_seq');
    END IF;

    -- Bang qr_scan_logs phuc vu FR-PCK-006 (chong sao chep tem)
    CREATE TABLE IF NOT EXISTS qr_scan_logs (
        id BIGSERIAL PRIMARY KEY,
        qr_code_id BIGINT NOT NULL,
        ip_addr VARCHAR(255),
        user_agent VARCHAR(512),
        scanned_at BIGINT NOT NULL
    );
    CREATE INDEX IF NOT EXISTS idx_qr_scan_logs_qr_time ON qr_scan_logs (qr_code_id, scanned_at DESC);

    -- Chuan hoa version cho batches
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'batches' AND column_name = 'version'
    ) THEN
        UPDATE batches SET version = 1 WHERE version IS NULL;
    END IF;
END $$;
--rollback DROP TABLE IF EXISTS qr_scan_logs;
