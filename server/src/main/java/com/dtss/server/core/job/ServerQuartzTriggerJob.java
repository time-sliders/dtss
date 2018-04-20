package com.dtss.server.core.job;

import com.dtss.client.cmp.SpringApplicationContextAware;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

/**
 * @author LuYun
 * @since 2018.03.30
 */
public class ServerQuartzTriggerJob implements Job {

    private ServerJobExecutor executor;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (executor == null) {
            ApplicationContext ac = SpringApplicationContextAware.getApplicationContext();
            executor = ac.getBean("serverJobExecutor", ServerJobExecutor.class);
        }
        executor.execute(context);
    }
}
