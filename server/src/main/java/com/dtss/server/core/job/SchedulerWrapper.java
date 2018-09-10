package com.dtss.server.core.job;

import com.dtss.client.consts.JobConst;
import com.dtss.client.model.JobConfig;
import org.quartz.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * {@link org.quartz.Scheduler}外覆类，用于封装Scheduler相关操作
 */
@Component
public class SchedulerWrapper implements InitializingBean {

    @Resource
    private SchedulerFactoryBean schedulerFactoryBean;

    private Scheduler scheduler;

    /**
     * 根据jobConfig的name、group获取JobKey
     */
    public JobKey getJobKeyByJobConfig(JobConfig cfg) {
        String name = String.valueOf(cfg.getId());
        String group = cfg.getApp();
        return JobKey.jobKey(name, group);
    }

    /**
     * 根据jobConfig的name、group获取TriggerKey
     */
    public TriggerKey getTriggerKeyByJobConfig(JobConfig cfg) {
        String name = String.valueOf(cfg.getId());
        String group = cfg.getApp();
        return TriggerKey.triggerKey(name, group);
    }

    /**
     * 根据jobConfig创建一个JobDetail
     */
    public JobDetail createJobDetailByJobConfig(JobConfig cfg, Class<? extends Job> jobClass) {
        JobKey jobKey = getJobKeyByJobConfig(cfg);
        return JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .usingJobData(JobConst.ID, cfg.getId())
                .usingJobData(JobConst.APP, cfg.getApp())
                .build();
    }

    /**
     * 创建一个CronTrigger
     */
    public Trigger createCronTrigger(JobConfig cfg) throws SchedulerException {
        return TriggerBuilder.newTrigger()
                .withIdentity(getTriggerKeyByJobConfig(cfg))
                .withSchedule(CronScheduleBuilder.cronSchedule(cfg.getCron()))
                .build();
    }

    /**
     * 将JobDetail、Trigger添加到scheduler中, 如果已经有了，会 rescheduleJob。
     */
    public Date scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {

        JobKey jobKey = jobDetail.getKey();
        TriggerKey triggerKey = trigger.getKey();

        if (scheduler.checkExists(jobKey) && scheduler.checkExists(triggerKey)) {
            return rescheduleJob(triggerKey, trigger);

        } else {
            return scheduler.scheduleJob(jobDetail, trigger);
        }
    }

    /**
     * 根据JobKey删除指定的Job
     */
    public boolean deleteJob(JobKey jobKey) throws SchedulerException {
        return scheduler.deleteJob(jobKey);
    }

    /**
     * 根据指定的TriggerKey，删除旧的Trigger，添加新的Trigger
     */
    private Date rescheduleJob(TriggerKey triggerKey,
                               Trigger newTrigger) throws SchedulerException {
        return scheduler.rescheduleJob(triggerKey, newTrigger);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 0. init scheduler
        scheduler = schedulerFactoryBean.getScheduler();
    }
}
