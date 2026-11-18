--liquibase formatted sql

--changeset ecofarm:006-audit-table dbms:postgresql
--comment: Support fresh schemas without altering existing audit records.
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_id BIGINT,
    action VARCHAR(255) NOT NULL,
    entity VARCHAR(255) NOT NULL,
    entity_id BIGINT NOT NULL,
    detail TEXT,
    ip_addr VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    updated_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
);

--changeset ecofarm:006-audit-id-default dbms:postgresql splitStatements:false
--comment: Repair legacy IDs only when neither identity nor a default is present; never rewind an existing sequence.
DO $$
DECLARE
    v_identity BOOLEAN;
    v_has_default BOOLEAN;
BEGIN
    SELECT (is_identity = 'YES'), (column_default IS NOT NULL)
      INTO v_identity, v_has_default
      FROM information_schema.columns
     WHERE table_schema = 'public' AND table_name = 'audit_logs' AND column_name = 'id';
    IF FOUND AND v_identity = false AND v_has_default = false THEN
        LOCK TABLE public.audit_logs IN ACCESS EXCLUSIVE MODE;
        CREATE SEQUENCE IF NOT EXISTS public.audit_logs_id_seq OWNED BY public.audit_logs.id;
        PERFORM setval('public.audit_logs_id_seq', GREATEST(
            COALESCE((SELECT MAX(id) FROM public.audit_logs), 0) + 1,
            (SELECT last_value + CASE WHEN is_called THEN 1 ELSE 0 END FROM public.audit_logs_id_seq)
        ), false);
        ALTER TABLE public.audit_logs ALTER COLUMN id SET DEFAULT nextval('public.audit_logs_id_seq');
    END IF;
END $$;

--changeset ecofarm:006-audit-guard-function dbms:postgresql splitStatements:false
--comment: No application role, including ADMIN, may mutate or remove audit history.
CREATE FUNCTION public.reject_audit_log_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'audit_logs is append-only: % is not allowed', TG_OP
        USING ERRCODE = '55000';
END;
$$;

--changeset ecofarm:006-audit-guard-trigger dbms:postgresql
--comment: Statement-level protection also rejects bulk operations and truncation on an empty table.
CREATE TRIGGER audit_logs_append_only
    BEFORE UPDATE OR DELETE OR TRUNCATE ON public.audit_logs
    FOR EACH STATEMENT EXECUTE FUNCTION public.reject_audit_log_mutation();

--changeset ecofarm:006-audit-read-indexes dbms:postgresql
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor_id_id ON public.audit_logs (actor_id, id DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_entity_id_id ON public.audit_logs (entity, entity_id, id DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at_id ON public.audit_logs (created_at, id DESC);
