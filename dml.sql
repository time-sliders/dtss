USE dtss;
INSERT INTO dtss.job_config
(id, name, app, type, trigger_mode, cron, job_bean_name, client_ip, activity,
description,param, version,last_modify_time_long, create_time)
VALUES
('1', 'Dtss自检JOB', 'dtss', 1, 1, '0 0/5 * * * ?', 'selfCheckJob', null, 1, 'Dtss自检JOB','', 0, 0, now()),
('2', 'Dtss数据一致性检测JOB', 'dtss', 1,1, '0 0/10 * * * ?', 'selfCheckJob', null, 1, '检测ZK与DB数据是否一致', '',  9, 1523364011303, now()),
('3', 'Dtss历史任务记录迁移', 'dtss', 1, 1, '0 0 4 * * ?', 'hisLogMigrateJob', null, 1, '将N个月以前的历史执行记录迁移到历史表', '',  1, 1523185609535, now()),
('4', 'Dtss处理中Job清理', 'dtss', 1, 1, '0 20 4 * * ?', 'statusCorrectJob', null, 1, '清理N天以前的，还在处理中的JOB', '',  1, 1523186520335, now());
