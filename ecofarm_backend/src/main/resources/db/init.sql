-- ==========================================================
-- Script khởi tạo bảng: roles, users, user_roles
-- Database: PostgreSQL
-- Tương thích: BaseEntity, User, Role entities (JPA / Hibernate)
-- ==========================================================

-- 1. Bảng roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    updated_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    code VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Bảng users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    updated_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    full_name VARCHAR(255),
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

-- 3. Bảng liên kết user_roles (Many-to-Many)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Index tối ưu truy vấn
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles (role_id);

-- Dữ liệu mẫu ban đầu (Seed data)
INSERT INTO roles (code)
VALUES ('USER')
ON CONFLICT (code) DO NOTHING;


-- 4. Thêm tài khoản mẫu: systemadmin@gmail.com (Mật khẩu: admin123)
INSERT INTO users (full_name, username, password_hash, is_active, version, created_at, updated_at)
VALUES (
    'System Admin',
    'systemadmin@gmail.com',
    'bveYDLSZX7iMV3dtkAuMsQ==$mX4hO4tX+aCRJ4nOmesobBxdPMHUhcOViHk3SlW7ncU=',
    TRUE,
    0,
    (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
)
ON CONFLICT (username) DO NOTHING;

-- Gán quyền cho systemadmin@gmail.com
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r
WHERE u.username = 'systemadmin@gmail.com' AND r.code = 'USER'
ON CONFLICT DO NOTHING;
