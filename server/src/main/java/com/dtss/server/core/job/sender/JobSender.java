package com.dtss.server.core.job.sender;

import com.dtss.client.enums.OptType;
import com.dtss.client.model.JobConfig;
import com.dtss.client.model.JobExecutiveLog;

/**
 * @author luyun
 * @since 2018.01.27 10:26
 */
public interface JobSender {

    /**
     * 发送某一个任务到客户端
     *
     * @param jobInstanceId job每一次执行的唯一ID
     * @param jobConfig     job配置信息
     * @param log           任务执行日志
     * @param optType       {@link OptType}
     */
    void send(String jobInstanceId, JobConfig jobConfig, JobExecutiveLog log, Integer optType);

}
