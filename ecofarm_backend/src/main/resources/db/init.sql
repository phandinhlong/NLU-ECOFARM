-- ==========================================================
-- Script khởi tạo bảng: roles, users, user_roles
-- Database: PostgreSQL
-- Tương thích schema DB portable (public_portable.sql): roles.code, users.username
-- ==========================================================

-- 1. Bảng roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version INT NOT NULL DEFAULT 1,
    created_at BIGINT,
    updated_at BIGINT
);

-- 2. Bảng users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    full_name VARCHAR(255),
    phone VARCHAR(255),
    status VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version INT NOT NULL DEFAULT 1,
    created_at BIGINT,
    updated_at BIGINT
);

-- 3. Bảng liên kết user_roles (Many-to-Many)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version INT NOT NULL DEFAULT 1,
    created_at BIGINT,
    updated_at BIGINT,
    PRIMARY KEY (user_id, role_id)
);

-- Index tối ưu truy vấn
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles (role_id);

-- Dữ liệu mẫu ban đầu (Seed data): vai trò USER
INSERT INTO roles (id, code, is_active, version, created_at, updated_at)
SELECT (SELECT COALESCE(MAX(id), 0) + 1 FROM roles), 'USER', TRUE, 1,
       (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
       (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'USER');


-- 4. Thêm tài khoản mẫu: systemadmin@gmail.com (Mật khẩu: admin123)
INSERT INTO users (username, password_hash, full_name, is_active, version, created_at, updated_at)
SELECT 'systemadmin@gmail.com',
       'bveYDLSZX7iMV3dtkAuMsQ==$mX4hO4tX+aCRJ4nOmesobBxdPMHUhcOViHk3SlW7ncU=',
       'System Admin',
       TRUE,
       1,
       (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
       (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'systemadmin@gmail.com');

-- Gán quyền cho systemadmin@gmail.com
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'systemadmin@gmail.com' AND r.code = 'USER'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
