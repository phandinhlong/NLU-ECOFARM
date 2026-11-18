--liquibase formatted sql

--changeset ecofarm:006-audit-read-indexes dbms:postgresql
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor_id_id ON public.audit_logs (actor_id, id DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_entity_id_id ON public.audit_logs (entity, entity_id, id DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at_id ON public.audit_logs (created_at, id DESC);
