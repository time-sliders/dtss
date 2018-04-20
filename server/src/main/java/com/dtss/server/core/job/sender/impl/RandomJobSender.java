package com.dtss.server.core.job.sender.impl;

import com.dtss.client.model.JobConfig;
import com.dtss.client.model.JobExecutiveLog;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 随机发送
 *
 * @author luyun
 * @since 2018.01.27 10:35
 */
@Component
public class RandomJobSender extends AbstractJobSender {

    private static final Logger logger = LoggerFactory.getLogger(RandomJobSender.class);

    @Override
    protected String getClientIp(JobConfig jobConfig, JobExecutiveLog log) {

        String client = clientSystemNodeWatcher.getClientRandomly(jobConfig.getApp());
        if (StringUtils.isBlank(client)) {
            jobLogService.innerFail(log.getId(),
                    "没有存活的客户端,任务执行失败");
        }
        return client;
    }
}