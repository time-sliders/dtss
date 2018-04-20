CREATE DATABASE IF NOT EXISTS dtss
  DEFAULT CHARSET utf8
  COLLATE utf8_general_ci;

USE dtss;
DROP TABLE dtss.job_config;
CREATE TABLE dtss.job_config (
  `id`                  VARCHAR(32)      NOT NULL,
  `name`                VARCHAR(64)      DEFAULT NULL
  COMMENT 'JOB名称',
  `app`                 VARCHAR(32)      NOT NULL
  COMMENT 'JOB所属应用',
  `type`                TINYINT UNSIGNED NOT NULL
  COMMENT '任务类型 JobTypeEnum',
  `trigger_mode`        TINYINT UNSIGNED NOT NULL
  COMMENT '触发模式 JobTriggerMode',
  `cron`                VARCHAR(32)      DEFAULT NULL
  COMMENT 'JOB执行的corn表达式',
  `job_bean_name`       VARCHAR(64)      NOT NULL
  COMMENT 'JOB 的 springBeanName',
  `client_ip`           VARCHAR(256)     DEFAULT NULL
  COMMENT '客户端IP(绝对单点&单点优先等情况)',
  `activity`            TINYINT UNSIGNED DEFAULT '0'
  COMMENT 'JOB是否激活',
  `description`         VARCHAR(256)     DEFAULT NULL
  COMMENT 'JOB描述',
  `param`               VARCHAR(1024)    DEFAULT NULL
  COMMENT 'job执行参数',
  `owner_phone`         VARCHAR(64)      DEFAULT NULL
  COMMENT '开发人员手机',
  version               BIGINT DEFAULT 0 NOT NULL
  COMMENT '版本',
  last_modify_time_long BIGINT DEFAULT 0 NOT NULL
  COMMENT '最新更新时间',
  `create_time`         DATETIME         DEFAULT NULL
  COMMENT '创建时间',
  `modify_time`         DATETIME         DEFAULT NULL
  COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_app` (`app`),
  KEY `idx_job_bean_name` (`job_bean_name`),
  KEY `idx_name` (`name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COMMENT = 'JOB配置表';

DROP TABLE dtss.job_executive_log;
CREATE TABLE dtss.job_executive_log (
  `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `job_id`            VARCHAR(32)     NOT NULL
  COMMENT 'JOB ID',
  `app`               VARCHAR(32)     NOT NULL
  COMMENT 'JOB所属应用',
  schedule_time       BIGINT UNSIGNED NOT NULL,
  slice_index         INT UNSIGNED             DEFAULT 0
  COMMENT '任务分片索引',
  name                VARCHAR(64) COMMENT 'JOB名称',
  `job_bean_name`     VARCHAR(64)     NOT NULL
  COMMENT 'JOB 的 springBeanName',
  `trigger_server_ip` VARCHAR(32)              DEFAULT NULL
  COMMENT '触发服务器IP',
  `execute_client_ip` VARCHAR(32)              DEFAULT NULL
  COMMENT '执行客户端IP',
  `start_time`        DATETIME                 DEFAULT NULL
  COMMENT '开始时间',
  `finish_time`       DATETIME                 DEFAULT NULL
  COMMENT '结束时间',
  `param`             VARCHAR(1024)            DEFAULT NULL
  COMMENT 'job执行参数',
  `status`            TINYINT                  DEFAULT 0
  COMMENT '执行状态 JobExeStatusEnum',
  `inner_msg`         VARCHAR(256)             DEFAULT NULL
  COMMENT '内部异常信息 任务调度框架内部处理信息',
  `job_exe_result`    VARCHAR(1024)            DEFAULT NULL
  COMMENT 'job执行结果',
  `create_time`       DATETIME                 DEFAULT NULL
  COMMENT '创建时间',
  `modify_time`       DATETIME                 DEFAULT NULL
  COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_job_executive_log (job_id, schedule_time, slice_index),
  KEY `idx_app` (`app`),
  KEY `idx_job_bean_name` (`job_bean_name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COMMENT = 'JOB执行记录表';

DROP TABLE dtss.job_executive_log_his;
CREATE TABLE dtss.job_executive_log_his (
  `id`                BIGINT UNSIGNED NOT NULL,
  `job_id`            VARCHAR(32)     NOT NULL
  COMMENT 'JOB ID',
  `app`               VARCHAR(32)     NOT NULL
  COMMENT 'JOB所属应用',
  schedule_time       BIGINT UNSIGNED NOT NULL,
  slice_index         INT UNSIGNED  DEFAULT 0
  COMMENT '任务分片索引',
  name                VARCHAR(64) COMMENT 'JOB名称',
  `job_bean_name`     VARCHAR(64)     NOT NULL
  COMMENT 'JOB 的 springBeanName',
  `trigger_server_ip` VARCHAR(32)   DEFAULT NULL
  COMMENT '触发服务器IP',
  `execute_client_ip` VARCHAR(32)   DEFAULT NULL
  COMMENT '执行客户端IP',
  `start_time`        DATETIME      DEFAULT NULL
  COMMENT '开始时间',
  `finish_time`       DATETIME      DEFAULT NULL
  COMMENT '结束时间',
  `param`             VARCHAR(1024) DEFAULT NULL
  COMMENT 'job执行参数',
  `status`            TINYINT       DEFAULT 0
  COMMENT '执行状态 JobExeStatusEnum',
  `inner_msg`         VARCHAR(256)  DEFAULT NULL
  COMMENT '内部异常信息 任务调度框架内部处理信息',
  `job_exe_result`    VARCHAR(1024) DEFAULT NULL
  COMMENT 'job执行结果',
  `create_time`       DATETIME      DEFAULT NULL
  COMMENT '创建时间',
  `modify_time`       DATETIME      DEFAULT NULL
  COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_job_id_union_key` (job_id, schedule_time, slice_index),
  KEY `idx_app` (`app`),
  KEY `idx_job_bean_name` (`job_bean_name`),
  KEY `idx_create_time` (`create_time`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COMMENT = 'JOB执行历史记录表';