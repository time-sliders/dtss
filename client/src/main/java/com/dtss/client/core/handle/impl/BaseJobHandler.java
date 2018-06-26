package com.dtss.client.core.handle.impl;

import com.dtss.client.core.handle.JobHandler;
import com.dtss.client.enums.JobExeStatusEnum;
import com.dtss.client.facade.JobLogFacade;
import com.dtss.client.model.JobExecutiveLog;
import com.dtss.commons.IPUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author LuYun
 * @since 2018.04.16
 */
public abstract class BaseJobHandler implements JobHandler, ApplicationContextAware {

    protected ApplicationContext ac;

    @Autowired
    protected JobLogFacade jobLogFacade;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ac = applicationContext;
    }

    protected void startProcessing(Long logId) {
        JobExecutiveLog log = new JobExecutiveLog();
        log.setId(logId);
        log.setExecuteClientIp(IPUtils.getLocalIp());
        log.setStatus(JobExeStatusEnum.PROCESSING.getCode());
        jobLogFacade.endLog(log);
    }

    protected void failJob(Long logId, String innerMsg) {
        JobExecutiveLog log = new JobExecutiveLog();
        log.setId(logId);
        log.setExecuteClientIp(IPUtils.getLocalIp());
        log.setInnerMsg(innerMsg);
        log.setStatus(JobExeStatusEnum.FAIL.getCode());
        jobLogFacade.endLog(log);
    }

    protected void endJob(Long logId, String jobExeResult) {
        JobExecutiveLog log = new JobExecutiveLog();
        log.setId(logId);
        log.setExecuteClientIp(IPUtils.getLocalIp());
        log.setJobExeResult(jobExeResult);
        log.setStatus(JobExeStatusEnum.SUCCESS.getCode());
        jobLogFacade.endLog(log);
    }

}
