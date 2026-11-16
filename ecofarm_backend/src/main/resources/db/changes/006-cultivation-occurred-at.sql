--liquibase formatted sql

--changeset ecofarm:006-cultivation-occurred-at
ALTER TABLE IF EXISTS cultivation_events ADD COLUMN IF NOT EXISTS occurred_at BIGINT;
