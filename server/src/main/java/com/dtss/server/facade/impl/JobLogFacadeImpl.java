package com.dtss.server.facade.impl;

import com.dtss.commons.Result;
import com.dtss.server.service.JobLogService;
import com.dtss.client.facade.JobLogFacade;
import com.dtss.client.model.JobExecutiveLog;
import com.dtss.server.service.JobExecutiveLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.22
 */
@Service
public class JobLogFacadeImpl implements JobLogFacade {

    private static final Logger logger = LoggerFactory.getLogger(JobLogFacadeImpl.class);

    @Autowired
    private JobLogService jobLogService;

    @Autowired
    private JobExecutiveLogService jobExecutiveLogService;

    @Override
    public Result<JobExecutiveLog> findById(Long id) {
        try {
            return Result.buildSucc(jobExecutiveLogService.findById(id));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.buildFail(null, "查询数据异常");
        }
    }

    @Override
    public Result<Boolean> endLog(JobExecutiveLog log) {

        try {
            jobLogService.endLog(log);
            return Result.buildSucc(true);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return Result.buildFail(null, "结束任务日志失败");
        }
    }

    @Override
    public Result<Boolean> addInnerMsg(Long logId, String msg) {
        try {
            jobLogService.addInnerMsg(logId, msg);
            return Result.buildSucc(true);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return Result.buildFail(null, "操作失败");
        }
    }
}
