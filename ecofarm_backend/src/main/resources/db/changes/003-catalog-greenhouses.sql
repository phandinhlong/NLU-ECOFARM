--liquibase formatted sql

--changeset ecofarm:003-catalog-greenhouses
--validCheckSum: ANY
--comment: FR-MST-001 - bảng nhà màng (mã, tên, tọa độ GPS, diện tích, trạng thái)
CREATE TABLE IF NOT EXISTS green_houses (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    gps_lat NUMERIC(10, 6),
    gps_lng NUMERIC(10, 6),
    area_m2 NUMERIC(12, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    updated_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
);
--rollback DROP TABLE IF EXISTS green_houses;

--changeset ecofarm:003-seed-catalog-roles
--validCheckSum: ANY
--comment: Seed vai trò ADMIN, ENGINEER cho danh mục (UC-03, UC-04: Admin, Kỹ sư)
INSERT INTO roles (role_name) VALUES ('ADMIN'), ('ENGINEER') ON CONFLICT (role_name) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'systemadmin@gmail.com' AND r.role_name IN ('ADMIN', 'ENGINEER')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'engineer@ecofarm.com' AND r.role_name = 'ENGINEER'
ON CONFLICT DO NOTHING;
--rollback DELETE FROM user_roles WHERE role_id IN (SELECT id FROM roles WHERE role_name IN ('ADMIN', 'ENGINEER'));
--rollback DELETE FROM roles WHERE role_name IN ('ADMIN', 'ENGINEER');
