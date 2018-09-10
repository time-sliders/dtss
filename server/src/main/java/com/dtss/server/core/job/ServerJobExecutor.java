package com.dtss.server.core.job;

import com.dtss.commons.DateUtil;
import com.dtss.server.core.job.sender.JobSender;
import com.dtss.server.service.JobLogService;
import com.dtss.client.consts.JobConst;
import com.dtss.client.enums.OptType;
import com.dtss.client.model.JobConfig;
import com.dtss.client.model.JobExecutiveLog;
import com.dtss.server.core.job.sender.SendDispatcher;
import com.dtss.server.core.zk.LeaderLatch;
import com.dtss.server.service.JobQueryService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author luyun
 * @since 2018.01.20 20:48
 */
@Component
public class ServerJobExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ServerJobExecutor.class);

    @Autowired
    private SendDispatcher dispatcher;

    @Autowired
    private JobQueryService jobQueryService;

    @Autowired
    private JobLogService logService;

    @Autowired
    private ServerQuartzManager serverQuartzManager;

    public void execute(JobExecutionContext context)  {

        /*
         * 群首检查
         */
        if (!LeaderLatch.hasLeaderShip()) {
            return;
        }

        /*
         * 信息验证
         */
        Long id = (Long) context.getMergedJobDataMap().get(JobConst.ID);
        String app = (String) context.getMergedJobDataMap().get(JobConst.APP);
        JobConfig jobConfig = jobQueryService.findById(id);
        if (jobConfig == null || !jobConfig.isActivity()) {
            logger.info("server job warning > illegal job:" + id);
            JobConfig unExistsJob = new JobConfig();
            unExistsJob.setId(id);
            unExistsJob.setApp(app);
            serverQuartzManager.deleteLocalJob(jobConfig);
            return;
        }

        /*
         * 创建JOB_ID
         */
        Date date = context.getScheduledFireTime();
        String scheduleTimeStr = DateUtil.format(date, DateUtil.yyyyMMddHHmmss);

        /*
         * 记录日志
         */
        JobExecutiveLog log = logService.startLog(jobConfig, Long.valueOf(scheduleTimeStr));

        /*
         * 任务分发
         */
        JobSender sender = dispatcher.getExecutor(jobConfig.getType());
        if (sender == null) {
            logService.innerFail(log.getId(), "[DTSS]无法获取任务分发器,不支持的类型:" + jobConfig.getType());
            return;
        }
        sender.send(scheduleTimeStr, jobConfig, log, OptType.EXE.getCode());
    }


    public JobExecutiveLog executeJobWithoutLeaderCheck(JobConfig jobConfig) {

        Date now = new Date();
        JobExecutiveLog log = logService.startLog(jobConfig, now.getTime());

        JobSender sender = dispatcher.getExecutor(jobConfig.getType());
        if (sender == null) {
            logService.innerFail(log.getId(), "[DTSS]无法获取任务分发器,不支持的类型:" + jobConfig.getType());
            return log;
        }

        sender.send(String.valueOf(now.getTime()), jobConfig, log, OptType.EXE.getCode());
        return log;
    }

    public JobExecutiveLog testJob(JobConfig jobConfig) {

        Date now = new Date();
        JobExecutiveLog log = logService.startLog(jobConfig, now.getTime());

        JobSender sender = dispatcher.getExecutor(jobConfig.getType());
        if (sender == null) {
            logService.innerFail(log.getId(), "[DTSS]无法获取任务分发器,不支持的类型:" + jobConfig.getType());
            return log;
        }

        sender.send(String.valueOf(now.getTime()), jobConfig, log, OptType.TEST.getCode());
        return log;
    }

}
