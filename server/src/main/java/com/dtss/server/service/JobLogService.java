package com.dtss.server.service;

import com.dtss.client.model.JobConfig;
import com.dtss.client.model.JobExecutiveLog;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.20
 */
public interface JobLogService {

    /**
     * 开始记录日志
     */
    JobExecutiveLog startLog(JobConfig jobConfig, Long startTimeLong);

    /**
     * 结束
     */
    void endLog(JobExecutiveLog log);

    /**
     * 内部原因导致JOB失败
     *
     * @param logId 日志ID
     * @param msg   失败原因
     */
    void innerFail(Long logId, String msg);

    /**
     * 为job新增一条内部信息
     *
     * @param logId 日志ID
     * @param msg   内部信息
     */
    void addInnerMsg(Long logId, String msg);
}
