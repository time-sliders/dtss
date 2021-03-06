package com.dtss.server.service;

import com.dtss.client.model.JobConfig;

/**
 *
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.01
 */
public interface JobAddService {

    /**
     * 新增一条记录
     */
    boolean addJob(JobConfig jobConfig);
}
