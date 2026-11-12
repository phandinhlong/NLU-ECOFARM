/*
 Navicat Premium Dump SQL

 Source Server         : postgres
 Source Server Type    : PostgreSQL
 Source Server Version : 130023 (130023)
 Source Host           : localhost:5432
 Source Catalog        : ecofarm
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 130023 (130023)
 File Encoding         : 65001

 Date: 10/11/2026 20:13:51
*/


-- ----------------------------
-- Sequence structure for anchors_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."anchors_id_seq";
CREATE SEQUENCE "public"."anchors_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for audit_logs_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."audit_logs_id_seq";
CREATE SEQUENCE "public"."audit_logs_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for batches_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."batches_id_seq";
CREATE SEQUENCE "public"."batches_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for merkle_snapshots_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."merkle_snapshots_id_seq";
CREATE SEQUENCE "public"."merkle_snapshots_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for outbox_events_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."outbox_events_id_seq";
CREATE SEQUENCE "public"."outbox_events_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for recalls_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."recalls_id_seq";
CREATE SEQUENCE "public"."recalls_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for users_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."users_id_seq";
CREATE SEQUENCE "public"."users_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for anchors
-- ----------------------------
DROP TABLE IF EXISTS "public"."anchors";
CREATE TABLE "public"."anchors" (
  "id" int8 NOT NULL DEFAULT nextval('anchors_id_seq'::regclass),
  "snapshot_id" int8,
  "chain_id" int4,
  "contract_address" varchar(255) COLLATE "pg_catalog"."default",
  "tx_hash" varchar(255) COLLATE "pg_catalog"."default",
  "block_number" int8,
  "confirmations" int4,
  "status" varchar(255) COLLATE "pg_catalog"."default",
  "anchored_at" int8,
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8,
  "explorer_url" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of anchors
-- ----------------------------
BEGIN;
INSERT INTO "public"."anchors" ("id", "snapshot_id", "chain_id", "contract_address", "tx_hash", "block_number", "confirmations", "status", "anchored_at", "is_active", "version", "created_at", "updated_at", "explorer_url") VALUES (1, 1, 1337, '0x1234567890123456789012345678901234567890', '0xdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd', 501, 12, 'CONFIRMED', 1794614500000, 't', 1, 1794614400000, 1794614500000, NULL);
INSERT INTO "public"."anchors" ("id", "snapshot_id", "chain_id", "contract_address", "tx_hash", "block_number", "confirmations", "status", "anchored_at", "is_active", "version", "created_at", "updated_at", "explorer_url") VALUES (2, 2, 1337, '0x1234567890123456789012345678901234567890', '0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee', 502, 3, 'PENDING', 1794700900000, 't', 1, 1794700800000, 1794700900000, NULL);
INSERT INTO "public"."anchors" ("id", "snapshot_id", "chain_id", "contract_address", "tx_hash", "block_number", "confirmations", "status", "anchored_at", "is_active", "version", "created_at", "updated_at", "explorer_url") VALUES (5, NULL, 1337, NULL, '0x49752e0f2d4d031fdf62a62e2d13f5b66dfe5588a7c430087cf65176af297259', NULL, 216, 'CONFIRMED', 1794218835207, NULL, 1, NULL, NULL, NULL);
INSERT INTO "public"."anchors" ("id", "snapshot_id", "chain_id", "contract_address", "tx_hash", "block_number", "confirmations", "status", "anchored_at", "is_active", "version", "created_at", "updated_at", "explorer_url") VALUES (4, NULL, 1337, NULL, '0xcbb852708f04416dd2edb512b7cbf7d3c1e5efb362bb51b5d56912cdc90de9c0', NULL, 1142, 'CONFIRMED', 1794218835223, 't', 1, NULL, NULL, NULL);
INSERT INTO "public"."anchors" ("id", "snapshot_id", "chain_id", "contract_address", "tx_hash", "block_number", "confirmations", "status", "anchored_at", "is_active", "version", "created_at", "updated_at", "explorer_url") VALUES (6, NULL, 1337, NULL, '0xa0eed0385cff0e8b15e9e34021c778a36b1a271f4290300b46d5781a7fcffbe6', NULL, 56, 'CONFIRMED', 1794218835237, NULL, 1, NULL, NULL, NULL);
INSERT INTO "public"."anchors" ("id", "snapshot_id", "chain_id", "contract_address", "tx_hash", "block_number", "confirmations", "status", "anchored_at", "is_active", "version", "created_at", "updated_at", "explorer_url") VALUES (645, 2, 1337, NULL, '0x19569056cc7460cd8aacf9858ec8f1b6475b1a9defae67b7b5c439a36f9681b5', 1676, 12, 'CONFIRMED', 1794220585008, NULL, 11, NULL, NULL, NULL);
INSERT INTO "public"."anchors" ("id", "snapshot_id", "chain_id", "contract_address", "tx_hash", "block_number", "confirmations", "status", "anchored_at", "is_active", "version", "created_at", "updated_at", "explorer_url") VALUES (644, 2, 1337, NULL, '0x4bb4528cbd04d74a2db254e77d9ad2033ef40d0390dc399c36380bb8bf871e06', 1676, 12, 'CONFIRMED', 1794220335010, NULL, 10, NULL, NULL, NULL);
INSERT INTO "public"."anchors" ("id", "snapshot_id", "chain_id", "contract_address", "tx_hash", "block_number", "confirmations", "status", "anchored_at", "is_active", "version", "created_at", "updated_at", "explorer_url") VALUES (643, 2, 1337, NULL, '0x8b37e0f7333de18db6cab71064ead46a403027479e085a01041716c0bd54b036', 1124, 12, 'CONFIRMED', 1794220020014, NULL, 10, NULL, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for audit_logs
-- ----------------------------
DROP TABLE IF EXISTS "public"."audit_logs";
CREATE TABLE "public"."audit_logs" (
  "id" int8 NOT NULL DEFAULT nextval('audit_logs_id_seq'::regclass),
  "actor_id" int8,
  "action" varchar(255) COLLATE "pg_catalog"."default",
  "entity" varchar(255) COLLATE "pg_catalog"."default",
  "entity_id" int8,
  "detail" text COLLATE "pg_catalog"."default",
  "ip_addr" varchar(255) COLLATE "pg_catalog"."default",
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8,
  "actor_name" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of audit_logs
-- ----------------------------
BEGIN;
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (1, 1, 'CREATE', 'BATCH', 1, '4001', '192.168.1.10', 't', 1, 1794182400000, 1794182400000, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (2, 2, 'RECORD_EVENT', 'CULTIVATION_EVENT', 3, '4002', '192.168.1.20', 't', 1, 1794355200000, 1794355200000, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (3, 3, 'QC_PASS', 'QC_RECORD', 1, '4003', '192.168.1.30', 't', 1, 1794355200000, 1794355200000, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (5, NULL, 'test', NULL, NULL, NULL, NULL, 't', 1, 123456, NULL, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (6, NULL, 'OUTBOX_EVENT_ANCHORED', 'outboxEvents', 2, 'Anchor{id=4, txHash=''0xcbb852708f04416dd2edb512b7cbf7d3c1e5efb362bb51b5d56912cdc90de9c0'', status=''SUCCESS''}', NULL, 't', 1, 1794213120345, NULL, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (7, NULL, 'OUTBOX_EVENT_ANCHORED', 'outboxEvents', 3, 'Anchor{id=5, txHash=''0x49752e0f2d4d031fdf62a62e2d13f5b66dfe5588a7c430087cf65176af297259'', status=''SUBMITTED''}', NULL, 't', 1, 1794217753284, NULL, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (8, NULL, 'OUTBOX_EVENT_ANCHORED', 'outboxEvents', 4, 'Anchor{id=6, txHash=''0xa0eed0385cff0e8b15e9e34021c778a36b1a271f4290300b46d5781a7fcffbe6'', status=''SUBMITTED''}', NULL, 't', 1, 1794218554124, NULL, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (9, NULL, 'ANCHOR_CONFIRMED', 'anchors', 5, 'Anchor{id=5, txHash=''0x49752e0f2d4d031fdf62a62e2d13f5b66dfe5588a7c430087cf65176af297259'', status=''CONFIRMED''}', NULL, 't', 1, 1794218835208, NULL, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (10, NULL, 'ANCHOR_CONFIRMED', 'anchors', 4, 'Anchor{id=4, txHash=''0xcbb852708f04416dd2edb512b7cbf7d3c1e5efb362bb51b5d56912cdc90de9c0'', status=''CONFIRMED''}', NULL, 't', 1, 1794218835223, NULL, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (11, NULL, 'ANCHOR_CONFIRMED', 'anchors', 6, 'Anchor{id=6, txHash=''0xa0eed0385cff0e8b15e9e34021c778a36b1a271f4290300b46d5781a7fcffbe6'', status=''CONFIRMED''}', NULL, 't', 1, 1794218835237, NULL, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (12, NULL, 'OUTBOX_EVENT_ANCHORED', 'outboxEvents', 5, 'Anchor{id=643, txHash=''0x8b37e0f7333de18db6cab71064ead46a403027479e085a01041716c0bd54b036'', status=''SUBMITTED''}', NULL, 't', 1, 1794219957025, NULL, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (13, NULL, 'ANCHOR_CONFIRMED', 'anchors', 643, 'Anchor{id=643, txHash=''0x8b37e0f7333de18db6cab71064ead46a403027479e085a01041716c0bd54b036'', status=''CONFIRMED''}', NULL, 't', 1, 1794220020014, NULL, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (14, NULL, 'OUTBOX_EVENT_ANCHORED', 'outboxEvents', 6, 'Anchor{id=644, txHash=''0x4bb4528cbd04d74a2db254e77d9ad2033ef40d0390dc399c36380bb8bf871e06'', status=''SUBMITTED''}', NULL, 't', 1, 1794220269134, NULL, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (15, NULL, 'ANCHOR_CONFIRMED', 'anchors', 644, 'Anchor{id=644, txHash=''0x4bb4528cbd04d74a2db254e77d9ad2033ef40d0390dc399c36380bb8bf871e06'', status=''CONFIRMED''}', NULL, 't', 1, 1794220335011, NULL, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (16, NULL, 'OUTBOX_EVENT_ANCHORED', 'outboxEvents', 7, 'Anchor{id=645, txHash=''0x19569056cc7460cd8aacf9858ec8f1b6475b1a9defae67b7b5c439a36f9681b5'', status=''SUBMITTED''}', NULL, 't', 1, 1794220515141, NULL, NULL);
INSERT INTO "public"."audit_logs" ("id", "actor_id", "action", "entity", "entity_id", "detail", "ip_addr", "is_active", "version", "created_at", "updated_at", "actor_name") VALUES (17, NULL, 'ANCHOR_CONFIRMED', 'anchors', 645, 'Anchor{id=645, txHash=''0x19569056cc7460cd8aacf9858ec8f1b6475b1a9defae67b7b5c439a36f9681b5'', status=''CONFIRMED''}', NULL, 't', 1, 1794220585008, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for batch_assignments
-- ----------------------------
DROP TABLE IF EXISTS "public"."batch_assignments";
CREATE TABLE "public"."batch_assignments" (
  "id" int8 NOT NULL,
  "batch_id" int8,
  "user_id" int8,
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of batch_assignments
-- ----------------------------
BEGIN;
INSERT INTO "public"."batch_assignments" ("id", "batch_id", "user_id", "is_active", "version", "created_at", "updated_at") VALUES (1, 1, 2, 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."batch_assignments" ("id", "batch_id", "user_id", "is_active", "version", "created_at", "updated_at") VALUES (2, 2, 2, 't', 1, 1794182400000, 1794182400000);
COMMIT;

-- ----------------------------
-- Table structure for batches
-- ----------------------------
DROP TABLE IF EXISTS "public"."batches";
CREATE TABLE "public"."batches" (
  "id" int8 NOT NULL DEFAULT nextval('batches_id_seq'::regclass),
  "batch_code" varchar(255) COLLATE "pg_catalog"."default",
  "greenhouse_id" int8,
  "variety_id" int8,
  "status" varchar(255) COLLATE "pg_catalog"."default",
  "sow_date" date,
  "harvest_date" date,
  "salt_enc" int8,
  "created_by" int8,
  "activated_at" int8,
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of batches
-- ----------------------------
BEGIN;
INSERT INTO "public"."batches" ("id", "batch_code", "greenhouse_id", "variety_id", "status", "sow_date", "harvest_date", "salt_enc", "created_by", "activated_at", "is_active", "version", "created_at", "updated_at") VALUES (1, 'BATCH-2026-001', 1, 1, 'HARVESTED', '2026-09-01', '2026-11-01', 123456, 1, 1794182400000, 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."batches" ("id", "batch_code", "greenhouse_id", "variety_id", "status", "sow_date", "harvest_date", "salt_enc", "created_by", "activated_at", "is_active", "version", "created_at", "updated_at") VALUES (2, 'BATCH-2026-002', 2, 2, 'GROWING', '2026-10-15', NULL, 234567, 1, 1794182400000, 't', 1, 1794182400000, 1794182400000);
COMMIT;

-- ----------------------------
-- Table structure for certificates
-- ----------------------------
DROP TABLE IF EXISTS "public"."certificates";
CREATE TABLE "public"."certificates" (
  "id" int8 NOT NULL,
  "greenhouse_id" int8,
  "cert_type" varchar COLLATE "pg_catalog"."default",
  "cert_no" varchar COLLATE "pg_catalog"."default",
  "valid_from" date,
  "valid_to" date,
  "file_key" varchar COLLATE "pg_catalog"."default",
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of certificates
-- ----------------------------
BEGIN;
INSERT INTO "public"."certificates" ("id", "greenhouse_id", "cert_type", "cert_no", "valid_from", "valid_to", "file_key", "is_active", "version", "created_at", "updated_at") VALUES (1, 1, 'VIETGAP', 'VG-DL-2026-001', '2026-01-01', '2027-01-01', 'certificates/vietgap-gh-dl-001.pdf', 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."certificates" ("id", "greenhouse_id", "cert_type", "cert_no", "valid_from", "valid_to", "file_key", "is_active", "version", "created_at", "updated_at") VALUES (2, 2, 'ORGANIC_INTERNAL', 'ORG-DL-2026-002', '2026-03-01', '2027-03-01', 'certificates/organic-gh-dl-002.pdf', 't', 1, 1794182400000, 1794182400000);
COMMIT;

-- ----------------------------
-- Table structure for cultivation_events
-- ----------------------------
DROP TABLE IF EXISTS "public"."cultivation_events";
CREATE TABLE "public"."cultivation_events" (
  "id" int8 NOT NULL,
  "batch_id" int8,
  "seq" int4,
  "event_type" varchar COLLATE "pg_catalog"."default",
  "payload" int8,
  "event_hash" varchar COLLATE "pg_catalog"."default",
  "status" varchar COLLATE "pg_catalog"."default",
  "recorded_by" int8,
  "recorded_at" int8,
  "client_event_id" varchar COLLATE "pg_catalog"."default",
  "amends_event_id" int8,
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of cultivation_events
-- ----------------------------
BEGIN;
INSERT INTO "public"."cultivation_events" ("id", "batch_id", "seq", "event_type", "payload", "event_hash", "status", "recorded_by", "recorded_at", "client_event_id", "amends_event_id", "is_active", "version", "created_at", "updated_at") VALUES (1, 1, 1, 'SOWING', 1001, 'sha256-demo-event-001', 'CONFIRMED', 2, 1794182400000, 'demo-event-001', NULL, 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."cultivation_events" ("id", "batch_id", "seq", "event_type", "payload", "event_hash", "status", "recorded_by", "recorded_at", "client_event_id", "amends_event_id", "is_active", "version", "created_at", "updated_at") VALUES (2, 1, 2, 'FERTILIZING', 1002, 'sha256-demo-event-002', 'CONFIRMED', 2, 1794268800000, 'demo-event-002', NULL, 't', 1, 1794268800000, 1794268800000);
INSERT INTO "public"."cultivation_events" ("id", "batch_id", "seq", "event_type", "payload", "event_hash", "status", "recorded_by", "recorded_at", "client_event_id", "amends_event_id", "is_active", "version", "created_at", "updated_at") VALUES (3, 1, 3, 'HARVESTING', 1003, 'sha256-demo-event-003', 'CONFIRMED', 2, 1794355200000, 'demo-event-003', NULL, 't', 1, 1794355200000, 1794355200000);
INSERT INTO "public"."cultivation_events" ("id", "batch_id", "seq", "event_type", "payload", "event_hash", "status", "recorded_by", "recorded_at", "client_event_id", "amends_event_id", "is_active", "version", "created_at", "updated_at") VALUES (4, 2, 1, 'SOWING', 2001, 'sha256-demo-event-004', 'CONFIRMED', 2, 1794441600000, 'demo-event-004', NULL, 't', 1, 1794441600000, 1794441600000);
INSERT INTO "public"."cultivation_events" ("id", "batch_id", "seq", "event_type", "payload", "event_hash", "status", "recorded_by", "recorded_at", "client_event_id", "amends_event_id", "is_active", "version", "created_at", "updated_at") VALUES (5, 2, 2, 'IRRIGATION', 2002, 'sha256-demo-event-005', 'PENDING', 2, 1794528000000, 'demo-event-005', NULL, 't', 1, 1794528000000, 1794528000000);
COMMIT;

-- ----------------------------
-- Table structure for event_attachments
-- ----------------------------
DROP TABLE IF EXISTS "public"."event_attachments";
CREATE TABLE "public"."event_attachments" (
  "id" int8 NOT NULL,
  "event_id" int8,
  "file_key" varchar COLLATE "pg_catalog"."default",
  "sha256" varchar COLLATE "pg_catalog"."default",
  "mime_type" varchar COLLATE "pg_catalog"."default",
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of event_attachments
-- ----------------------------
BEGIN;
INSERT INTO "public"."event_attachments" ("id", "event_id", "file_key", "sha256", "mime_type", "is_active", "version", "created_at", "updated_at") VALUES (1, 1, 'events/1/sowing-photo.jpg', 'demo-sha256-photo-001', 'image/jpeg', 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."event_attachments" ("id", "event_id", "file_key", "sha256", "mime_type", "is_active", "version", "created_at", "updated_at") VALUES (2, 3, 'events/3/harvest-report.pdf', 'demo-sha256-doc-001', 'application/pdf', 't', 1, 1794355200000, 1794355200000);
COMMIT;

-- ----------------------------
-- Table structure for green_houses
-- ----------------------------
DROP TABLE IF EXISTS "public"."green_houses";
CREATE TABLE "public"."green_houses" (
  "id" int8 NOT NULL,
  "code" varchar COLLATE "pg_catalog"."default",
  "name" varchar COLLATE "pg_catalog"."default",
  "gps_lat" numeric,
  "gps_lng" numeric,
  "area_m2" numeric,
  "status" varchar COLLATE "pg_catalog"."default",
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of green_houses
-- ----------------------------
BEGIN;
INSERT INTO "public"."green_houses" ("id", "code", "name", "gps_lat", "gps_lng", "area_m2", "status", "is_active", "version", "created_at", "updated_at") VALUES (1, 'GH-DL-001', 'Nha kinh Da Lat so 1', 11.9404, 108.4583, 2500.00, 'ACTIVE', 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."green_houses" ("id", "code", "name", "gps_lat", "gps_lng", "area_m2", "status", "is_active", "version", "created_at", "updated_at") VALUES (2, 'GH-DL-002', 'Nha kinh Da Lat so 2', 11.9412, 108.4601, 1800.00, 'ACTIVE', 't', 1, 1794182400000, 1794182400000);
COMMIT;

-- ----------------------------
-- Table structure for ledger_records
-- ----------------------------
DROP TABLE IF EXISTS "public"."ledger_records";
CREATE TABLE "public"."ledger_records" (
  "id" int8 NOT NULL,
  "event_id" int8,
  "tx_hash" varchar COLLATE "pg_catalog"."default",
  "block_number" int8,
  "status" varchar COLLATE "pg_catalog"."default",
  "confirmed_at" int8,
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of ledger_records
-- ----------------------------
BEGIN;
INSERT INTO "public"."ledger_records" ("id", "event_id", "tx_hash", "block_number", "status", "confirmed_at", "is_active", "version", "created_at", "updated_at") VALUES (1, 1, '0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 101, 'CONFIRMED', 1794182500000, 't', 1, 1794182400000, 1794182500000);
INSERT INTO "public"."ledger_records" ("id", "event_id", "tx_hash", "block_number", "status", "confirmed_at", "is_active", "version", "created_at", "updated_at") VALUES (2, 2, '0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb', 102, 'CONFIRMED', 1794268900000, 't', 1, 1794268800000, 1794268900000);
INSERT INTO "public"."ledger_records" ("id", "event_id", "tx_hash", "block_number", "status", "confirmed_at", "is_active", "version", "created_at", "updated_at") VALUES (3, 3, '0xcccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc', 103, 'CONFIRMED', 1794355300000, 't', 1, 1794355200000, 1794355300000);
COMMIT;

-- ----------------------------
-- Table structure for merkle_snapshots
-- ----------------------------
DROP TABLE IF EXISTS "public"."merkle_snapshots";
CREATE TABLE "public"."merkle_snapshots" (
  "id" int8 NOT NULL DEFAULT nextval('merkle_snapshots_id_seq'::regclass),
  "batch_id" int8,
  "merkle_root" varchar(255) COLLATE "pg_catalog"."default",
  "leaf_count" int4,
  "leaves" json,
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of merkle_snapshots
-- ----------------------------
BEGIN;
INSERT INTO "public"."merkle_snapshots" ("id", "batch_id", "merkle_root", "leaf_count", "leaves", "is_active", "version", "created_at", "updated_at") VALUES (1, 1, '0x1111111111111111111111111111111111111111111111111111111111111111', 3, '["sha256-demo-event-001","sha256-demo-event-002","sha256-demo-event-003"]', 't', 1, 1794614400000, 1794614400000);
INSERT INTO "public"."merkle_snapshots" ("id", "batch_id", "merkle_root", "leaf_count", "leaves", "is_active", "version", "created_at", "updated_at") VALUES (2, 2, '0x2222222222222222222222222222222222222222222222222222222222222222', 2, '["sha256-demo-event-004","sha256-demo-event-005"]', 't', 1, 1794700800000, 1794700800000);
COMMIT;

-- ----------------------------
-- Table structure for outbox_events
-- ----------------------------
DROP TABLE IF EXISTS "public"."outbox_events";
CREATE TABLE "public"."outbox_events" (
  "id" int8 NOT NULL DEFAULT nextval('outbox_events_id_seq'::regclass),
  "aggregate_type" varchar COLLATE "pg_catalog"."default",
  "aggregate_id" int8,
  "topic" varchar COLLATE "pg_catalog"."default",
  "payload" int8,
  "status" varchar(255) COLLATE "pg_catalog"."default",
  "attempts" int4,
  "next_attempt_at" int8,
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8,
  "eventdata" text COLLATE "pg_catalog"."default",
  "batch_id_hash" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "seq" int4 NOT NULL DEFAULT 0
)
;

-- ----------------------------
-- Records of outbox_events
-- ----------------------------
BEGIN;
INSERT INTO "public"."outbox_events" ("id", "aggregate_type", "aggregate_id", "topic", "payload", "status", "attempts", "next_attempt_at", "is_active", "version", "created_at", "updated_at", "eventdata", "batch_id_hash", "seq") VALUES (2, 'merkle_leaf', 1, 'anchor', NULL, 'SUCCESS', 0, NULL, 't', 1, 1794212771037, 1794213120354, '0x1111111111111111111111111111111111111111111111111111111111111111', '0xtestbatch', 1);
INSERT INTO "public"."outbox_events" ("id", "aggregate_type", "aggregate_id", "topic", "payload", "status", "attempts", "next_attempt_at", "is_active", "version", "created_at", "updated_at", "eventdata", "batch_id_hash", "seq") VALUES (3, 'merkle_leaf', 2, 'anchor', NULL, 'SUCCESS', 0, NULL, 't', 1, 1794217752441, 1794217753292, '0x9999aa99', '0xtestreal', 1);
INSERT INTO "public"."outbox_events" ("id", "aggregate_type", "aggregate_id", "topic", "payload", "status", "attempts", "next_attempt_at", "is_active", "version", "created_at", "updated_at", "eventdata", "batch_id_hash", "seq") VALUES (4, 'merkle_leaf', 2, 'anchor', NULL, 'SUCCESS', 0, NULL, 't', 1, 1794218553559, 1794218554127, '0xdeadbeef2', '0xtestreal2', 1);
INSERT INTO "public"."outbox_events" ("id", "aggregate_type", "aggregate_id", "topic", "payload", "status", "attempts", "next_attempt_at", "is_active", "version", "created_at", "updated_at", "eventdata", "batch_id_hash", "seq") VALUES (5, 'merkle_leaf', 2, 'anchor', NULL, 'SUCCESS', 0, NULL, 't', 1, 1794219625535, 1794219957027, '0xcafe1234', '0xtestreal3', 1);
INSERT INTO "public"."outbox_events" ("id", "aggregate_type", "aggregate_id", "topic", "payload", "status", "attempts", "next_attempt_at", "is_active", "version", "created_at", "updated_at", "eventdata", "batch_id_hash", "seq") VALUES (6, 'merkle_leaf', 2, 'anchor', NULL, 'SUCCESS', 0, NULL, 't', 1, 1794220268659, 1794220269137, '0xfeedface', '0xtestfinal', 1);
INSERT INTO "public"."outbox_events" ("id", "aggregate_type", "aggregate_id", "topic", "payload", "status", "attempts", "next_attempt_at", "is_active", "version", "created_at", "updated_at", "eventdata", "batch_id_hash", "seq") VALUES (7, 'merkle_leaf', 2, 'anchor', NULL, 'SUCCESS', 0, NULL, 't', 1, 1794220514682, 1794220515144, '0xfeedface2', '0xtestfinal2', 1);
COMMIT;

-- ----------------------------
-- Table structure for packages
-- ----------------------------
DROP TABLE IF EXISTS "public"."packages";
CREATE TABLE "public"."packages" (
  "id" int8 NOT NULL,
  "batch_id" int8,
  "package_code" varchar COLLATE "pg_catalog"."default",
  "unit_count" int4,
  "packed_by" int8,
  "packed_at" int8,
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of packages
-- ----------------------------
BEGIN;
INSERT INTO "public"."packages" ("id", "batch_id", "package_code", "unit_count", "packed_by", "packed_at", "is_active", "version", "created_at", "updated_at") VALUES (1, 1, 'PKG-2026-001', 100, 2, 1794441600000, 't', 1, 1794441600000, 1794441600000);
INSERT INTO "public"."packages" ("id", "batch_id", "package_code", "unit_count", "packed_by", "packed_at", "is_active", "version", "created_at", "updated_at") VALUES (2, 1, 'PKG-2026-002', 100, 2, 1794441600000, 't', 1, 1794441600000, 1794441600000);
INSERT INTO "public"."packages" ("id", "batch_id", "package_code", "unit_count", "packed_by", "packed_at", "is_active", "version", "created_at", "updated_at") VALUES (3, 2, 'PKG-2026-003', 50, 2, 1794528000000, 't', 1, 1794528000000, 1794528000000);
COMMIT;

-- ----------------------------
-- Table structure for qc_records
-- ----------------------------
DROP TABLE IF EXISTS "public"."qc_records";
CREATE TABLE "public"."qc_records" (
  "id" int8 NOT NULL,
  "batch_id" int8,
  "brix" numeric,
  "avg_weight_kg" numeric,
  "passed" bool,
  "inspected_by" int8,
  "inspected_at" int8,
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of qc_records
-- ----------------------------
BEGIN;
INSERT INTO "public"."qc_records" ("id", "batch_id", "brix", "avg_weight_kg", "passed", "inspected_by", "inspected_at", "is_active", "version", "created_at", "updated_at") VALUES (1, 1, 8.50, 0.045, 't', 3, 1794355200000, 't', 1, 1794355200000, 1794355200000);
INSERT INTO "public"."qc_records" ("id", "batch_id", "brix", "avg_weight_kg", "passed", "inspected_by", "inspected_at", "is_active", "version", "created_at", "updated_at") VALUES (2, 2, 5.20, 0.320, 't', 3, 1794528000000, 't', 1, 1794528000000, 1794528000000);
COMMIT;

-- ----------------------------
-- Table structure for qr_codes
-- ----------------------------
DROP TABLE IF EXISTS "public"."qr_codes";
CREATE TABLE "public"."qr_codes" (
  "id" int8 NOT NULL,
  "package_id" int8,
  "code" varchar COLLATE "pg_catalog"."default",
  "serial" int4,
  "status" varchar COLLATE "pg_catalog"."default",
  "printed_at" int8,
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of qr_codes
-- ----------------------------
BEGIN;
INSERT INTO "public"."qr_codes" ("id", "package_id", "code", "serial", "status", "printed_at", "is_active", "version", "created_at", "updated_at") VALUES (1, 1, 'ECOFARM-QR-000001', 1, 'PRINTED', 1794441600000, 't', 1, 1794441600000, 1794441600000);
INSERT INTO "public"."qr_codes" ("id", "package_id", "code", "serial", "status", "printed_at", "is_active", "version", "created_at", "updated_at") VALUES (2, 1, 'ECOFARM-QR-000002', 2, 'PRINTED', 1794441600000, 't', 1, 1794441600000, 1794441600000);
INSERT INTO "public"."qr_codes" ("id", "package_id", "code", "serial", "status", "printed_at", "is_active", "version", "created_at", "updated_at") VALUES (3, 2, 'ECOFARM-QR-000003', 3, 'PRINTED', 1794441600000, 't', 1, 1794441600000, 1794441600000);
INSERT INTO "public"."qr_codes" ("id", "package_id", "code", "serial", "status", "printed_at", "is_active", "version", "created_at", "updated_at") VALUES (4, 3, 'ECOFARM-QR-000004', 4, 'GENERATED', 1794528000000, 't', 1, 1794528000000, 1794528000000);
COMMIT;

-- ----------------------------
-- Table structure for recalls
-- ----------------------------
DROP TABLE IF EXISTS "public"."recalls";
CREATE TABLE "public"."recalls" (
  "id" int8 NOT NULL DEFAULT nextval('recalls_id_seq'::regclass),
  "batch_id" int8,
  "reason" varchar COLLATE "pg_catalog"."default",
  "created_by" int8,
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "updated_at" int8,
  "created_at" int8,
  "revoke_tx_hash" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of recalls
-- ----------------------------
BEGIN;
INSERT INTO "public"."recalls" ("id", "batch_id", "reason", "created_by", "is_active", "version", "updated_at", "created_at", "revoke_tx_hash") VALUES (1, 2, 'Du lieu thu hoi mau de kiem thu quy trinh; khong phai su kien thuc te.', 1, 't', 1, 1794787200000, 1794787200000, NULL);
INSERT INTO "public"."recalls" ("id", "batch_id", "reason", "created_by", "is_active", "version", "updated_at", "created_at", "revoke_tx_hash") VALUES (3, 1, 'test', 1, 't', 1, 1794185411065, 1794185411065, NULL);
INSERT INTO "public"."recalls" ("id", "batch_id", "reason", "created_by", "is_active", "version", "updated_at", "created_at", "revoke_tx_hash") VALUES (6, 1, 'Sản phẩm có vấn đề chất lượng', 1, 't', 1, 1794190710091, 1794190710091, NULL);
INSERT INTO "public"."recalls" ("id", "batch_id", "reason", "created_by", "is_active", "version", "updated_at", "created_at", "revoke_tx_hash") VALUES (7, 1, 'fk-removal-test', 1, 't', 1, 1794194630547, 1794194630547, NULL);
INSERT INTO "public"."recalls" ("id", "batch_id", "reason", "created_by", "is_active", "version", "updated_at", "created_at", "revoke_tx_hash") VALUES (8, 1, 'Sản phẩm có vấn đề chất lượng', 1, 't', 1, 1794195391929, 1794195391929, NULL);
INSERT INTO "public"."recalls" ("id", "batch_id", "reason", "created_by", "is_active", "version", "updated_at", "created_at", "revoke_tx_hash") VALUES (9, 1, 'Sản phẩm có vấn đề chất lượng', 1, 't', 1, 1794195393929, 1794195393929, NULL);
INSERT INTO "public"."recalls" ("id", "batch_id", "reason", "created_by", "is_active", "version", "updated_at", "created_at", "revoke_tx_hash") VALUES (10, 2, 'Sản phẩm có vấn đề chất lượng', 1, 't', 1, 1794211496744, 1794211496744, NULL);
INSERT INTO "public"."recalls" ("id", "batch_id", "reason", "created_by", "is_active", "version", "updated_at", "created_at", "revoke_tx_hash") VALUES (11, 2, 'Sản phẩm có vấn đề chất lượng', 1, 't', 1, 1794211497801, 1794211497801, NULL);
INSERT INTO "public"."recalls" ("id", "batch_id", "reason", "created_by", "is_active", "version", "updated_at", "created_at", "revoke_tx_hash") VALUES (12, 2, 'Sản phẩm có vấn đề chất lượng', 1, 't', 1, 1794211498666, 1794211498666, NULL);
INSERT INTO "public"."recalls" ("id", "batch_id", "reason", "created_by", "is_active", "version", "updated_at", "created_at", "revoke_tx_hash") VALUES (13, 2, 'Sản phẩm có vấn đề chất lượng', 1, 't', 1, 1794211513457, 1794211513457, NULL);
INSERT INTO "public"."recalls" ("id", "batch_id", "reason", "created_by", "is_active", "version", "updated_at", "created_at", "revoke_tx_hash") VALUES (14, 1, 'test recall spec alignment', 1, 't', 1, 1794217163158, 1794217163158, NULL);
INSERT INTO "public"."recalls" ("id", "batch_id", "reason", "created_by", "is_active", "version", "updated_at", "created_at", "revoke_tx_hash") VALUES (15, 1, 'test RBAC', 1, 't', 1, 1794307102284, 1794307102284, NULL);
INSERT INTO "public"."recalls" ("id", "batch_id", "reason", "created_by", "is_active", "version", "updated_at", "created_at", "revoke_tx_hash") VALUES (16, 1, 'test RBAC', 1, 't', 1, 1794307102371, 1794307102371, NULL);
COMMIT;

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS "public"."roles";
CREATE TABLE "public"."roles" (
  "id" int8 NOT NULL,
  "code" varchar COLLATE "pg_catalog"."default",
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of roles
-- ----------------------------
BEGIN;
INSERT INTO "public"."roles" ("id", "code", "is_active", "version", "created_at", "updated_at") VALUES (1, 'ADMIN', 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."roles" ("id", "code", "is_active", "version", "created_at", "updated_at") VALUES (2, 'FARMER', 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."roles" ("id", "code", "is_active", "version", "created_at", "updated_at") VALUES (3, 'QUALITY_CONTROL', 't', 1, 1794182400000, 1794182400000);
COMMIT;

-- ----------------------------
-- Table structure for shipments
-- ----------------------------
DROP TABLE IF EXISTS "public"."shipments";
CREATE TABLE "public"."shipments" (
  "id" int8 NOT NULL,
  "batch_id" int8,
  "destination" varchar COLLATE "pg_catalog"."default",
  "vehicle_plate" varchar COLLATE "pg_catalog"."default",
  "shipped_at" int8,
  "received_at" int8,
  "status" varchar COLLATE "pg_catalog"."default",
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of shipments
-- ----------------------------
BEGIN;
INSERT INTO "public"."shipments" ("id", "batch_id", "destination", "vehicle_plate", "shipped_at", "received_at", "status", "is_active", "version", "created_at", "updated_at") VALUES (1, 1, 'Sieu thi Da Lat Mart', '49A-123.45', 1794528000000, 1794614400000, 'DELIVERED', 't', 1, 1794528000000, 1794614400000);
INSERT INTO "public"."shipments" ("id", "batch_id", "destination", "vehicle_plate", "shipped_at", "received_at", "status", "is_active", "version", "created_at", "updated_at") VALUES (2, 2, 'Kho trung tam Lam Dong', '49C-678.90', 1794700800000, NULL, 'IN_TRANSIT', 't', 1, 1794700800000, 1794700800000);
COMMIT;

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS "public"."user_roles";
CREATE TABLE "public"."user_roles" (
  "user_id" int8 NOT NULL,
  "role_id" int8 NOT NULL,
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of user_roles
-- ----------------------------
BEGIN;
INSERT INTO "public"."user_roles" ("user_id", "role_id", "is_active", "version", "created_at", "updated_at") VALUES (1, 1, 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."user_roles" ("user_id", "role_id", "is_active", "version", "created_at", "updated_at") VALUES (2, 2, 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."user_roles" ("user_id", "role_id", "is_active", "version", "created_at", "updated_at") VALUES (3, 3, 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."user_roles" ("user_id", "role_id", "is_active", "version", "created_at", "updated_at") VALUES (5, 3, 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."user_roles" ("user_id", "role_id", "is_active", "version", "created_at", "updated_at") VALUES (6, 2, 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."user_roles" ("user_id", "role_id", "is_active", "version", "created_at", "updated_at") VALUES (7, 1, 't', 1, 1794182400000, 1794182400000);
COMMIT;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS "public"."users";
CREATE TABLE "public"."users" (
  "id" int8 NOT NULL DEFAULT nextval('users_id_seq'::regclass),
  "username" varchar(255) COLLATE "pg_catalog"."default",
  "password_hash" varchar COLLATE "pg_catalog"."default",
  "full_name" varchar(255) COLLATE "pg_catalog"."default",
  "phone" varchar(255) COLLATE "pg_catalog"."default",
  "status" varchar(255) COLLATE "pg_catalog"."default",
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of users
-- ----------------------------
BEGIN;
INSERT INTO "public"."users" ("id", "username", "password_hash", "full_name", "phone", "status", "is_active", "version", "created_at", "updated_at") VALUES (1, 'admin_demo', '$2b$12$DEMO_HASH_REPLACE_BEFORE_LOGIN', 'Nguyen Quang Minh', '0900000001', 'ACTIVE', 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."users" ("id", "username", "password_hash", "full_name", "phone", "status", "is_active", "version", "created_at", "updated_at") VALUES (2, 'farmer_demo', '$2b$12$DEMO_HASH_REPLACE_BEFORE_LOGIN', 'Tran Thi Lan', '0900000002', 'ACTIVE', 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."users" ("id", "username", "password_hash", "full_name", "phone", "status", "is_active", "version", "created_at", "updated_at") VALUES (3, 'qc_demo', '$2b$12$DEMO_HASH_REPLACE_BEFORE_LOGIN', 'Le Van Nam', '0900000003', 'ACTIVE', 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."users" ("id", "username", "password_hash", "full_name", "phone", "status", "is_active", "version", "created_at", "updated_at") VALUES (5, 'engineer@ecofarm.com', 'Y7K3vOZm0SWGe+Mek+zbNA==$9qlAsgcU9EbKqsqPOY9xbEUzDHJO1Jqd2as0YPzwfc0=', 'engineer@ecofarm.com', NULL, NULL, 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."users" ("id", "username", "password_hash", "full_name", "phone", "status", "is_active", "version", "created_at", "updated_at") VALUES (6, 'worker@ecofarm.com', 'pJvTpZjIjPnfKt4vlQ0DuQ==$RKLEEvafMbgUl57SD4jsJ7r0Z8X3FsqktjvHJXA2CRY=', 'worker@ecofarm.com', NULL, NULL, 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."users" ("id", "username", "password_hash", "full_name", "phone", "status", "is_active", "version", "created_at", "updated_at") VALUES (7, 'admin@ecofarm.com', 'JsDcyJnIX8oPQ5m1oPwI4Q==$zA2+Sbeflmdz7UskXDXxUdAGaf63gG5VaCD2tL2++KQ=', 'admin@ecofarm.com', NULL, NULL, 't', 1, 1794182400000, 1794182400000);
COMMIT;

-- ----------------------------
-- Table structure for varieties
-- ----------------------------
DROP TABLE IF EXISTS "public"."varieties";
CREATE TABLE "public"."varieties" (
  "id" int8 NOT NULL,
  "code" varchar COLLATE "pg_catalog"."default",
  "name" varchar COLLATE "pg_catalog"."default",
  "is_active" bool DEFAULT true,
  "version" int4 DEFAULT 1,
  "created_at" int8,
  "updated_at" int8
)
;

-- ----------------------------
-- Records of varieties
-- ----------------------------
BEGIN;
INSERT INTO "public"."varieties" ("id", "code", "name", "is_active", "version", "created_at", "updated_at") VALUES (1, 'TOM-CHERRY', 'Ca chua bi', 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."varieties" ("id", "code", "name", "is_active", "version", "created_at", "updated_at") VALUES (2, 'DAXANH-01', 'Dua leo xanh', 't', 1, 1794182400000, 1794182400000);
INSERT INTO "public"."varieties" ("id", "code", "name", "is_active", "version", "created_at", "updated_at") VALUES (3, 'RAU-XALACH', 'Xa lach thuy canh', 't', 1, 1794182400000, 1794182400000);
COMMIT;

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."anchors_id_seq"', 645, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."audit_logs_id_seq"', 17, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."batches_id_seq"', 3, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."merkle_snapshots_id_seq"', 3, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."outbox_events_id_seq"', 7, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."recalls_id_seq"', 16, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."users_id_seq"', 7, true);

-- ----------------------------
-- Primary Key structure for table anchors
-- ----------------------------
ALTER TABLE "public"."anchors" ADD CONSTRAINT "anchors_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table audit_logs
-- ----------------------------
ALTER TABLE "public"."audit_logs" ADD CONSTRAINT "audit_logs_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table batch_assignments
-- ----------------------------
ALTER TABLE "public"."batch_assignments" ADD CONSTRAINT "batch_assignments_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table batches
-- ----------------------------
ALTER TABLE "public"."batches" ADD CONSTRAINT "batches_batch_code_key" UNIQUE ("batch_code");

-- ----------------------------
-- Primary Key structure for table batches
-- ----------------------------
ALTER TABLE "public"."batches" ADD CONSTRAINT "batches_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table certificates
-- ----------------------------
ALTER TABLE "public"."certificates" ADD CONSTRAINT "certificates_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table cultivation_events
-- ----------------------------
ALTER TABLE "public"."cultivation_events" ADD CONSTRAINT "cultivation_events_client_event_id_key" UNIQUE ("client_event_id");

-- ----------------------------
-- Primary Key structure for table cultivation_events
-- ----------------------------
ALTER TABLE "public"."cultivation_events" ADD CONSTRAINT "cultivation_events_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table event_attachments
-- ----------------------------
ALTER TABLE "public"."event_attachments" ADD CONSTRAINT "event_attachments_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table green_houses
-- ----------------------------
ALTER TABLE "public"."green_houses" ADD CONSTRAINT "green_houses_code_key" UNIQUE ("code");

-- ----------------------------
-- Primary Key structure for table green_houses
-- ----------------------------
ALTER TABLE "public"."green_houses" ADD CONSTRAINT "green_houses_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table ledger_records
-- ----------------------------
ALTER TABLE "public"."ledger_records" ADD CONSTRAINT "ledger_records_event_id_key" UNIQUE ("event_id");

-- ----------------------------
-- Primary Key structure for table ledger_records
-- ----------------------------
ALTER TABLE "public"."ledger_records" ADD CONSTRAINT "ledger_records_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table merkle_snapshots
-- ----------------------------
ALTER TABLE "public"."merkle_snapshots" ADD CONSTRAINT "merkle_snapshots_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table outbox_events
-- ----------------------------
ALTER TABLE "public"."outbox_events" ADD CONSTRAINT "outbox_events_batch_id_hash_seq_unique" UNIQUE ("batch_id_hash", "seq");
ALTER TABLE "public"."outbox_events" ADD CONSTRAINT "ukc09lkg06xa9t3ufq2wqqm1dkn" UNIQUE ("batch_id_hash", "seq");

-- ----------------------------
-- Primary Key structure for table outbox_events
-- ----------------------------
ALTER TABLE "public"."outbox_events" ADD CONSTRAINT "outbox_events_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table packages
-- ----------------------------
ALTER TABLE "public"."packages" ADD CONSTRAINT "packages_package_code_key" UNIQUE ("package_code");

-- ----------------------------
-- Primary Key structure for table packages
-- ----------------------------
ALTER TABLE "public"."packages" ADD CONSTRAINT "packages_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table qc_records
-- ----------------------------
ALTER TABLE "public"."qc_records" ADD CONSTRAINT "qc_records_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table qr_codes
-- ----------------------------
ALTER TABLE "public"."qr_codes" ADD CONSTRAINT "qr_codes_code_key" UNIQUE ("code");

-- ----------------------------
-- Primary Key structure for table qr_codes
-- ----------------------------
ALTER TABLE "public"."qr_codes" ADD CONSTRAINT "qr_codes_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table recalls
-- ----------------------------
ALTER TABLE "public"."recalls" ADD CONSTRAINT "recalls_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table roles
-- ----------------------------
ALTER TABLE "public"."roles" ADD CONSTRAINT "roles_code_key" UNIQUE ("code");

-- ----------------------------
-- Primary Key structure for table roles
-- ----------------------------
ALTER TABLE "public"."roles" ADD CONSTRAINT "roles_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table shipments
-- ----------------------------
ALTER TABLE "public"."shipments" ADD CONSTRAINT "shipments_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table user_roles
-- ----------------------------
ALTER TABLE "public"."user_roles" ADD CONSTRAINT "user_roles_pkey" PRIMARY KEY ("user_id", "role_id");

-- ----------------------------
-- Uniques structure for table users
-- ----------------------------
ALTER TABLE "public"."users" ADD CONSTRAINT "users_username_key" UNIQUE ("username");

-- ----------------------------
-- Primary Key structure for table users
-- ----------------------------
ALTER TABLE "public"."users" ADD CONSTRAINT "users_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table varieties
-- ----------------------------
ALTER TABLE "public"."varieties" ADD CONSTRAINT "varieties_code_key" UNIQUE ("code");

-- ----------------------------
-- Primary Key structure for table varieties
-- ----------------------------
ALTER TABLE "public"."varieties" ADD CONSTRAINT "varieties_pkey" PRIMARY KEY ("id");
