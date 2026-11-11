--liquibase formatted sql

--changeset ecofarm:002-seed-test-roles
--validCheckSum: ANY
--comment: Grant blockchain test roles to the seeded admin account
INSERT INTO roles (role_name)
VALUES ('QUALITY_CONTROL'), ('FARMER')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'systemadmin@gmail.com'
  AND r.role_name IN ('QUALITY_CONTROL', 'FARMER')
ON CONFLICT DO NOTHING;
--rollback DELETE FROM user_roles WHERE role_id IN (SELECT id FROM roles WHERE role_name IN ('QUALITY_CONTROL', 'FARMER'));
--rollback DELETE FROM roles WHERE role_name IN ('QUALITY_CONTROL', 'FARMER');
