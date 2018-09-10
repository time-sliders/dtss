USE dtss;
INSERT INTO dtss.job_config
(name, app, type, trigger_mode, cron, job_bean_name, client_ip, activity,
description,param, version, create_time)
VALUES
('Dtss历史任务记录迁移', 'dtss', 1, 1, '0 0 4 * * ?', 'hisLogMigrateJob', null, 1, '将N个月以前的历史执行记录迁移到历史表', null,  1, now()),
('Dtss处理中Job清理', 'dtss', 1, 1, '0 20 4 * * ?', 'statusCorrectJob', null, 1, '清理N天以前的，还在处理中的JOB', null,  1, now());
