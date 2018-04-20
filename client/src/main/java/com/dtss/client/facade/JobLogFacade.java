package com.dtss.client.facade;

import com.dtss.client.model.JobExecutiveLog;
import com.dtss.commons.Result;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.22
 */
public interface JobLogFacade {

    Result<JobExecutiveLog> findById(Long id);

    /**
     * 结束
     */
    Result<Boolean> endLog(JobExecutiveLog log);

    /**
     * 为job新增一条内部信息
     *
     * @param logId 日志ID
     * @param msg   内部信息
     */
    Result<Boolean> addInnerMsg(Long logId, String msg);

}
