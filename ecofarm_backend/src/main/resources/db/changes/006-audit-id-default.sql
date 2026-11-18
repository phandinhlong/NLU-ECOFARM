--liquibase formatted sql

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
