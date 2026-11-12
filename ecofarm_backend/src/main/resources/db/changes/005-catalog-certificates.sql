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

--changeset ecofarm:006-catalog-id-sequences-portable
--validCheckSum: ANY
--comment: public_portable.sql không có sequence cho id của green_houses/varieties/certificates, bổ sung để JPA IDENTITY insert được trên DB import từ dump
CREATE SEQUENCE IF NOT EXISTS green_houses_id_seq;
SELECT setval('green_houses_id_seq', COALESCE((SELECT MAX(id) FROM green_houses), 1), true);
ALTER TABLE green_houses ALTER COLUMN id SET DEFAULT nextval('green_houses_id_seq');

CREATE SEQUENCE IF NOT EXISTS varieties_id_seq;
SELECT setval('varieties_id_seq', COALESCE((SELECT MAX(id) FROM varieties), 1), true);
ALTER TABLE varieties ALTER COLUMN id SET DEFAULT nextval('varieties_id_seq');

CREATE SEQUENCE IF NOT EXISTS certificates_id_seq;
SELECT setval('certificates_id_seq', COALESCE((SELECT MAX(id) FROM certificates), 1), true);
ALTER TABLE certificates ALTER COLUMN id SET DEFAULT nextval('certificates_id_seq');
--rollback ALTER TABLE certificates ALTER COLUMN id DROP DEFAULT;
--rollback DROP SEQUENCE IF EXISTS certificates_id_seq;
--rollback ALTER TABLE varieties ALTER COLUMN id DROP DEFAULT;
--rollback DROP SEQUENCE IF EXISTS varieties_id_seq;
--rollback ALTER TABLE green_houses ALTER COLUMN id DROP DEFAULT;
--rollback DROP SEQUENCE IF EXISTS green_houses_id_seq;
