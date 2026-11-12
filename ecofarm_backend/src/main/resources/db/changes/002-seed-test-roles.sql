--liquibase formatted sql

--changeset ecofarm:002-seed-test-roles
--validCheckSum: ANY
--comment: Grant blockchain test roles to the seeded admin account
INSERT INTO roles (id, code, is_active, version, created_at, updated_at)
SELECT base + rn, r.code, TRUE, 1,
       (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
       (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
FROM (SELECT COALESCE(MAX(id), 0) AS base FROM roles) b
CROSS JOIN (VALUES ('QUALITY_CONTROL', 1), ('FARMER', 2)) AS r(code, rn)
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = r.code);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'systemadmin@gmail.com'
  AND r.code IN ('QUALITY_CONTROL', 'FARMER')
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
--rollback DELETE FROM user_roles WHERE role_id IN (SELECT id FROM roles WHERE code IN ('QUALITY_CONTROL', 'FARMER'));
--rollback DELETE FROM roles WHERE code IN ('QUALITY_CONTROL', 'FARMER');
