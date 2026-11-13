--liquibase formatted sql

--changeset ecofarm:000-sync-legacy-columns runOnChange:true splitStatements:false
--validCheckSum: ANY
--comment: Dong bo ten cot users.username -> email, roles.code -> role_name, unique index va sequence cua roles
DO $$
BEGIN
    EXECUTE 'GRANT USAGE ON SCHEMA public TO PUBLIC';

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'username'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'email'
        ) THEN
            ALTER TABLE users RENAME COLUMN username TO email;
        ELSE
            UPDATE users SET email = username WHERE email IS NULL AND username IS NOT NULL;
            ALTER TABLE users DROP COLUMN IF EXISTS username;
        END IF;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'email'
    ) THEN
        CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_unique ON users (email);
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'roles' AND column_name = 'code'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'roles' AND column_name = 'role_name'
        ) THEN
            ALTER TABLE roles RENAME COLUMN code TO role_name;
        ELSE
            UPDATE roles SET role_name = code WHERE role_name IS NULL AND code IS NOT NULL;
            ALTER TABLE roles DROP COLUMN IF EXISTS code;
        END IF;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'roles' AND column_name = 'role_name'
    ) THEN
        CREATE UNIQUE INDEX IF NOT EXISTS idx_roles_role_name_unique ON roles (role_name);
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'roles'
    ) THEN
        CREATE SEQUENCE IF NOT EXISTS roles_id_seq;
        PERFORM setval('roles_id_seq', COALESCE((SELECT MAX(id) FROM roles), 0) + 1, false);
        ALTER TABLE roles ALTER COLUMN id SET DEFAULT nextval('roles_id_seq');
    END IF;
END $$;
--rollback SELECT 1;
