--liquibase formatted sql

--changeset ecofarm:005-catalog-certificates
--validCheckSum: ANY
--comment: FR-MST-003 - bảng chứng nhận VietGAP/GlobalGAP (số, hiệu lực, tệp PDF, cảnh báo hết hạn)
CREATE TABLE IF NOT EXISTS certificates (
    id BIGSERIAL PRIMARY KEY,
    greenhouse_id BIGINT,
    cert_type VARCHAR(32) NOT NULL,
    cert_no VARCHAR(64) NOT NULL,
    valid_from DATE,
    valid_to DATE,
    file_key VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    updated_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS certificates_cert_no_key ON certificates (cert_no);
--rollback DROP INDEX IF EXISTS certificates_cert_no_key;
--rollback DROP TABLE IF EXISTS certificates;

--changeset ecofarm:006-catalog-id-sequence-green-houses splitStatements:false
--validCheckSum: ANY
--comment: public_portable.sql không có sequence cho id green_houses; bỏ qua nếu cột là IDENTITY (do Hibernate tạo) hoặc đã có default
DO $$
DECLARE
    v_identity BOOLEAN;
    v_has_default BOOLEAN;
BEGIN
    SELECT (is_identity = 'YES'), (column_default IS NOT NULL)
      INTO v_identity, v_has_default
      FROM information_schema.columns
     WHERE table_schema = 'public' AND table_name = 'green_houses' AND column_name = 'id';
    IF FOUND AND v_identity = false AND v_has_default = false THEN
        CREATE SEQUENCE IF NOT EXISTS green_houses_id_seq;
        PERFORM setval('green_houses_id_seq', COALESCE((SELECT MAX(id) FROM green_houses), 1), true);
        EXECUTE 'ALTER TABLE green_houses ALTER COLUMN id SET DEFAULT nextval(''green_houses_id_seq'')';
    END IF;
END $$;

--changeset ecofarm:007-catalog-id-sequence-varieties splitStatements:false
--validCheckSum: ANY
--comment: public_portable.sql không có sequence cho id varieties; bỏ qua nếu cột là IDENTITY hoặc đã có default
DO $$
DECLARE
    v_identity BOOLEAN;
    v_has_default BOOLEAN;
BEGIN
    SELECT (is_identity = 'YES'), (column_default IS NOT NULL)
      INTO v_identity, v_has_default
      FROM information_schema.columns
     WHERE table_schema = 'public' AND table_name = 'varieties' AND column_name = 'id';
    IF FOUND AND v_identity = false AND v_has_default = false THEN
        CREATE SEQUENCE IF NOT EXISTS varieties_id_seq;
        PERFORM setval('varieties_id_seq', COALESCE((SELECT MAX(id) FROM varieties), 1), true);
        EXECUTE 'ALTER TABLE varieties ALTER COLUMN id SET DEFAULT nextval(''varieties_id_seq'')';
    END IF;
END $$;

--changeset ecofarm:008-catalog-id-sequence-certificates splitStatements:false
--validCheckSum: ANY
--comment: public_portable.sql không có sequence cho id certificates; bỏ qua nếu cột là IDENTITY hoặc đã có default
DO $$
DECLARE
    v_identity BOOLEAN;
    v_has_default BOOLEAN;
BEGIN
    SELECT (is_identity = 'YES'), (column_default IS NOT NULL)
      INTO v_identity, v_has_default
      FROM information_schema.columns
     WHERE table_schema = 'public' AND table_name = 'certificates' AND column_name = 'id';
    IF FOUND AND v_identity = false AND v_has_default = false THEN
        CREATE SEQUENCE IF NOT EXISTS certificates_id_seq;
        PERFORM setval('certificates_id_seq', COALESCE((SELECT MAX(id) FROM certificates), 1), true);
        EXECUTE 'ALTER TABLE certificates ALTER COLUMN id SET DEFAULT nextval(''certificates_id_seq'')';
    END IF;
END $$;
