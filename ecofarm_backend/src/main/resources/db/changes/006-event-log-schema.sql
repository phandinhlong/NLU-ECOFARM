--liquibase formatted sql

--changeset ecofarm:006-event-log-schema splitStatements:false
--validCheckSum: ANY
--comment: batch assignment + event log foundation (reviews, chain status, amend, batch_tasks)
ALTER TABLE cultivation_events ADD COLUMN IF NOT EXISTS occurred_at BIGINT;
ALTER TABLE cultivation_events ADD COLUMN IF NOT EXISTS review_status VARCHAR(32) DEFAULT 'UNREVIEWED';
ALTER TABLE cultivation_events ADD COLUMN IF NOT EXISTS reviewed_by BIGINT;
ALTER TABLE cultivation_events ADD COLUMN IF NOT EXISTS reviewed_at BIGINT;
ALTER TABLE cultivation_events ADD COLUMN IF NOT EXISTS reject_reason TEXT;
ALTER TABLE cultivation_events ADD COLUMN IF NOT EXISTS pending_approver_id BIGINT;
ALTER TABLE cultivation_events ADD COLUMN IF NOT EXISTS amend_reason VARCHAR(32);
ALTER TABLE cultivation_events ADD COLUMN IF NOT EXISTS amend_note TEXT;

DO $$
DECLARE
    v_type TEXT;
BEGIN
    SELECT data_type
      INTO v_type
      FROM information_schema.columns
     WHERE table_schema = 'public' AND table_name = 'cultivation_events' AND column_name = 'payload';
    IF v_type IS NOT NULL AND v_type <> 'text' THEN
        ALTER TABLE cultivation_events ALTER COLUMN payload TYPE TEXT USING payload::text;
    END IF;
END $$;

ALTER TABLE batch_assignments ADD COLUMN IF NOT EXISTS assigned_by BIGINT;
ALTER TABLE outbox_events ADD COLUMN IF NOT EXISTS event_id BIGINT;
ALTER TABLE outbox_events ADD COLUMN IF NOT EXISTS target_chain VARCHAR(16) DEFAULT 'BOTH';

CREATE TABLE IF NOT EXISTS batch_tasks (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT,
    worker_id BIGINT,
    task_type VARCHAR(32) NOT NULL,
    note TEXT,
    assigned_by BIGINT,
    assigned_at BIGINT,
    completed_at BIGINT,
    status VARCHAR(16) NOT NULL DEFAULT 'ASSIGNED',
    event_id BIGINT,
    version BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    updated_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_batch_tasks_batch_id ON batch_tasks (batch_id, id DESC);
CREATE INDEX IF NOT EXISTS idx_batch_tasks_worker_status ON batch_tasks (worker_id, status);

--rollback DROP INDEX IF EXISTS idx_batch_tasks_worker_status;
--rollback DROP INDEX IF EXISTS idx_batch_tasks_batch_id;
--rollback DROP TABLE IF EXISTS batch_tasks;
--rollback ALTER TABLE outbox_events DROP COLUMN IF EXISTS target_chain;
--rollback ALTER TABLE outbox_events DROP COLUMN IF EXISTS event_id;
--rollback ALTER TABLE batch_assignments DROP COLUMN IF EXISTS assigned_by;
--rollback ALTER TABLE cultivation_events DROP COLUMN IF EXISTS amend_note;
--rollback ALTER TABLE cultivation_events DROP COLUMN IF EXISTS amend_reason;
--rollback ALTER TABLE cultivation_events DROP COLUMN IF EXISTS pending_approver_id;
--rollback ALTER TABLE cultivation_events DROP COLUMN IF EXISTS reject_reason;
--rollback ALTER TABLE cultivation_events DROP COLUMN IF EXISTS reviewed_at;
--rollback ALTER TABLE cultivation_events DROP COLUMN IF EXISTS reviewed_by;
--rollback ALTER TABLE cultivation_events DROP COLUMN IF EXISTS review_status;
--rollback ALTER TABLE cultivation_events DROP COLUMN IF EXISTS occurred_at;
