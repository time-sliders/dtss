package com.dtss.client.core.handle.impl;

import com.dtss.client.core.AbstractJob;
import com.dtss.client.core.handle.JobHandler;
import com.dtss.client.model.ZkCmd;
import org.springframework.stereotype.Component;

/**
 * 测试任务是否可以执行
 *
 * @author LuYun
 * @since 2018.04.16
 */
@Component
public class JobTestHandler extends BaseJobHandler implements JobHandler {

    @Override
    public void handle(ZkCmd cmd) {

        AbstractJob job = ac.getBean(cmd.getJobBeanName(), AbstractJob.class);
        if (job == null) {
            failJob(cmd.getLogId(), "SpringBean不存在");
        } else {
            endJob(cmd.getLogId(), "JOB测试成功");
        }
    }
}
