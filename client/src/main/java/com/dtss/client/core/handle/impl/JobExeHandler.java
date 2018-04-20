package com.dtss.client.core.handle.impl;

import com.dtss.client.core.AbstractJob;
import com.dtss.client.model.ZkCmd;
import com.dtss.commons.Millisecond;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author LuYun
 * @since 2018.04.16
 */
@Component
public class JobExeHandler extends BaseJobHandler implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(JobExeHandler.class);

    @Override
    public void handle(ZkCmd cmd) {

        if (isJobExpired(cmd)) {
            failJob(cmd.getLogId(), "任务过期,取消执行");
            return;
        }

        AbstractJob job = ac.getBean(cmd.getJobBeanName(), AbstractJob.class);
        if (job == null) {
            failJob(cmd.getLogId(), "SpringBean不存在");
            return;
        }

        job.registry(cmd.getLogId());

        String result;
        try {
            result = job.execute(cmd.getParam());
        } catch (Throwable e) {
            logger.error("任务[" + cmd.getJobBeanName() + "]执行异常", e);
            result = "执行异常>" + e.getMessage();
        } finally {
            job.finish();
        }

        endJob(cmd.getLogId(), result);
    }

    private boolean isJobExpired(ZkCmd jobData) {
        return StringUtils.isBlank(jobData.getJobScheduledTime())
                || System.currentTimeMillis() - Long.valueOf(jobData.getJobScheduledTime()) > Millisecond.HALF_MINUS;

    }


}
