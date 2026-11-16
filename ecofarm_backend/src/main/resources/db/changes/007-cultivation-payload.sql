--liquibase formatted sql

--changeset ecofarm:007-cultivation-payload
ALTER TABLE IF EXISTS cultivation_events ADD COLUMN IF NOT EXISTS payload_json TEXT;
