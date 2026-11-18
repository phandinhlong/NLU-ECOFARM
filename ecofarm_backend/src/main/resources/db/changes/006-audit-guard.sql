--liquibase formatted sql

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
