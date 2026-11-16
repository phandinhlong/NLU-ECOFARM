--liquibase formatted sql

--changeset ecofarm:009-cultivation-sync-flags
ALTER TABLE cultivation_events ADD COLUMN IF NOT EXISTS device_clock_skew_ms BIGINT;
ALTER TABLE cultivation_events ADD COLUMN IF NOT EXISTS needs_review BOOLEAN;
