package com.dtss.client.core.handle.impl;

import com.dtss.client.core.AbstractJob;
import com.dtss.client.core.handle.JobHandler;
import com.dtss.client.facade.JobLogFacade;
import com.dtss.client.model.JobExecutiveLog;
import com.dtss.client.model.ZkCmd;
import com.dtss.client.enums.JobExeStatusEnum;
import com.dtss.commons.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author LuYun
 * @since 2018.04.16
 */
@Component
public class JobStopHandler extends AbstractJob implements JobHandler {

    private static final Logger logger = LoggerFactory.getLogger(JobStopHandler.class);

    @Autowired
    private JobLogFacade jobLogFacade;

    @Override
    public void handle(ZkCmd cmd) {
        if (terminate(cmd.getLogId())) {
            logger.info("已发送任务结束命令:" + cmd.getLogId());
            return;
        }

        logger.info("尝试中止任务时,当前机器并未运行该任务");

        /*
         * 如果任务记录是处理中，则改为失败
         */
        Result<JobExecutiveLog> result = jobLogFacade.findById(cmd.getLogId());
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return;
        }
        JobExecutiveLog log = result.getData();
        if (log.getStatus() == null || JobExeStatusEnum.PROCESSING.getCode() == log.getStatus()) {
            JobExecutiveLog logUpdate = new JobExecutiveLog();
            logUpdate.setId(log.getId());
            logUpdate.setStatus(JobExeStatusEnum.FAIL.getCode());
            logUpdate.setInnerMsg("任务非正常结束,以失败处理");
            jobLogFacade.endLog(logUpdate);
        }
    }

    @Override
    public String execute(String param) {
        throw new RuntimeException("not support method");
    }
}
