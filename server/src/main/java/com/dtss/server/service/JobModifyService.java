package com.dtss.server.service;

import com.dtss.client.model.JobConfig;
import com.dtss.commons.Result;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.01
 */
public interface JobModifyService {

    Result<Void> updateById(JobConfig jobConfig);

}
