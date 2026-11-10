--liquibase formatted sql

--changeset james-be:000-hello-liquibase
--comment: Changeset kiểm tra Liquibase chạy được (không thay đổi schema)
SELECT 1;
--rollback empty