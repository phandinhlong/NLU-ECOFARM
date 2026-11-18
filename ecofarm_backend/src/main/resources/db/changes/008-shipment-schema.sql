--liquibase formatted sql

--changeset ecofarm:008-shipment-schema
--validCheckSum: ANY
--comment: FR-SHP-001/002 - Bo sung cot quantity, received_by, condition_note, photo_urls cho shipments va role PARTNER_QC

ALTER TABLE shipments ADD COLUMN IF NOT EXISTS quantity INTEGER;
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS received_by BIGINT;
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS condition_note TEXT;
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS photo_urls TEXT;

INSERT INTO roles (role_name) VALUES ('PARTNER_QC')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email IN ('systemadmin@gmail.com', 'qc_demo')
  AND r.role_name = 'PARTNER_QC'
ON CONFLICT DO NOTHING;

--rollback ALTER TABLE shipments DROP COLUMN IF EXISTS quantity, DROP COLUMN IF EXISTS received_by, DROP COLUMN IF EXISTS condition_note, DROP COLUMN IF EXISTS photo_urls;
--rollback DELETE FROM user_roles WHERE role_id IN (SELECT id FROM roles WHERE role_name = 'PARTNER_QC');
--rollback DELETE FROM roles WHERE role_name = 'PARTNER_QC';
