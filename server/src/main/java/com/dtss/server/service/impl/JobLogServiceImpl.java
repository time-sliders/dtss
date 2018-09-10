package com.dtss.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.dtss.server.service.JobLogService;
import com.dtss.client.enums.JobExeStatusEnum;
import com.dtss.client.model.JobConfig;
import com.dtss.client.model.JobExecutiveLog;
import com.dtss.server.core.zk.LeaderLatch;
import com.dtss.server.service.JobExecutiveLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.20
 */
@Service
public class JobLogServiceImpl implements JobLogService {

    @Autowired
    private JobExecutiveLogService jobExecutiveLogService;

    public JobExecutiveLog startLog(JobConfig jobConfig, Long startTimeLong) {

        JobExecutiveLog jobExecutiveLog = new JobExecutiveLog();
        jobExecutiveLog.setApp(jobConfig.getApp());
        jobExecutiveLog.setScheduleTime(startTimeLong);
        jobExecutiveLog.setParam(jobConfig.getParam());
        jobExecutiveLog.setTriggerServerIp(LeaderLatch.LOCAL_IP);
        jobExecutiveLog.setJobBeanName(jobConfig.getJobBeanName());
        jobExecutiveLog.setJobId(jobConfig.getId());
        jobExecutiveLog.setName(jobConfig.getName());
        jobExecutiveLog.setStatus(JobExeStatusEnum.PROCESSING.getCode());
        jobExecutiveLog.setStartTime(new Date());

        try {
            jobExecutiveLogService.insert(jobExecutiveLog);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("任务已经被其他服务触发:"
                    + JSON.toJSONString(jobConfig) + ",scheduleTime:" + startTimeLong);
        }

        return jobExecutiveLog;
    }

    @Override
    public void endLog(JobExecutiveLog log) {
        log.setFinishTime(new Date());
        jobExecutiveLogService.updateById(log);
    }

    @Override
    public void innerFail(Long logId, String msg) {
        JobExecutiveLog log = new JobExecutiveLog();
        log.setId(logId);
        log.setInnerMsg(msg);
        log.setStatus(JobExeStatusEnum.FAIL.getCode());
        endLog(log);
    }

    @Override
    public void addInnerMsg(Long logId, String msg) {
        JobExecutiveLog log = new JobExecutiveLog();
        log.setId(logId);
        log.setInnerMsg(msg);
        jobExecutiveLogService.updateById(log);
    }
}