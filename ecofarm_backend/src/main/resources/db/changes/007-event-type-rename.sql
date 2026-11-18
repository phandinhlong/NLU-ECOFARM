--liquibase formatted sql

--changeset thaniel:007-event-type-rename splitStatements:true
--validCheckSum: ANY
--comment: rename Vietnamese event type constants to English
UPDATE cultivation_events SET event_type = 'SOWING' WHERE event_type = 'XUONG_GIONG';
UPDATE cultivation_events SET event_type = 'POLLINATION' WHERE event_type = 'THU_PHAN';
UPDATE cultivation_events SET event_type = 'FERTILIZE_IRRIGATE' WHERE event_type = 'BON_PHAN_TUOI';
UPDATE cultivation_events SET event_type = 'POLLINATION_ISOLATION' WHERE event_type = 'NGUNG_PHAN_CACH_LY';
UPDATE cultivation_events SET event_type = 'BRIX_MEASUREMENT' WHERE event_type = 'DO_BRIX';
UPDATE cultivation_events SET event_type = 'HARVEST' WHERE event_type = 'THU_HOACH';
UPDATE cultivation_events SET event_type = 'NOTE' WHERE event_type = 'GHI_CHU';
UPDATE batch_tasks SET task_type = 'SOWING' WHERE task_type = 'XUONG_GIONG';
UPDATE batch_tasks SET task_type = 'POLLINATION' WHERE task_type = 'THU_PHAN';
UPDATE batch_tasks SET task_type = 'FERTILIZE_IRRIGATE' WHERE task_type = 'BON_PHAN_TUOI';
UPDATE batch_tasks SET task_type = 'POLLINATION_ISOLATION' WHERE task_type = 'NGUNG_PHAN_CACH_LY';
UPDATE batch_tasks SET task_type = 'BRIX_MEASUREMENT' WHERE task_type = 'DO_BRIX';
UPDATE batch_tasks SET task_type = 'HARVEST' WHERE task_type = 'THU_HOACH';
UPDATE batch_tasks SET task_type = 'NOTE' WHERE task_type = 'GHI_CHU';

--rollback UPDATE batch_tasks SET task_type = 'GHI_CHU' WHERE task_type = 'NOTE';
--rollback UPDATE batch_tasks SET task_type = 'THU_HOACH' WHERE task_type = 'HARVEST';
--rollback UPDATE batch_tasks SET task_type = 'DO_BRIX' WHERE task_type = 'BRIX_MEASUREMENT';
--rollback UPDATE batch_tasks SET task_type = 'NGUNG_PHAN_CACH_LY' WHERE task_type = 'POLLINATION_ISOLATION';
--rollback UPDATE batch_tasks SET task_type = 'BON_PHAN_TUOI' WHERE task_type = 'FERTILIZE_IRRIGATE';
--rollback UPDATE batch_tasks SET task_type = 'THU_PHAN' WHERE task_type = 'POLLINATION';
--rollback UPDATE batch_tasks SET task_type = 'XUONG_GIONG' WHERE task_type = 'SOWING';
--rollback UPDATE cultivation_events SET event_type = 'GHI_CHU' WHERE event_type = 'NOTE';
--rollback UPDATE cultivation_events SET event_type = 'THU_HOACH' WHERE event_type = 'HARVEST';
--rollback UPDATE cultivation_events SET event_type = 'DO_BRIX' WHERE event_type = 'BRIX_MEASUREMENT';
--rollback UPDATE cultivation_events SET event_type = 'NGUNG_PHAN_CACH_LY' WHERE event_type = 'POLLINATION_ISOLATION';
--rollback UPDATE cultivation_events SET event_type = 'BON_PHAN_TUOI' WHERE event_type = 'FERTILIZE_IRRIGATE';
--rollback UPDATE cultivation_events SET event_type = 'THU_PHAN' WHERE event_type = 'POLLINATION';
--rollback UPDATE cultivation_events SET event_type = 'XUONG_GIONG' WHERE event_type = 'SOWING';
