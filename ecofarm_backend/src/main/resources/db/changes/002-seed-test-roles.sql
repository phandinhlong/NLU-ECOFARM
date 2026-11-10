--liquibase formatted sql

--changeset ecofarm:002-seed-test-roles
--comment: Grant blockchain test roles to the seeded admin account
INSERT INTO roles (code)
VALUES ('QUALITY_CONTROL'), ('FARMER')
ON CONFLICT (code) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'systemadmin@gmail.com'
  AND r.code IN ('QUALITY_CONTROL', 'FARMER')
ON CONFLICT DO NOTHING;
--rollback DELETE FROM user_roles WHERE role_id IN (SELECT id FROM roles WHERE code IN ('QUALITY_CONTROL', 'FARMER'));
--rollback DELETE FROM roles WHERE code IN ('QUALITY_CONTROL', 'FARMER');
