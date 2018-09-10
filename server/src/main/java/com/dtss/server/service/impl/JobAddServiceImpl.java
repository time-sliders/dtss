package com.dtss.server.service.impl;

import com.dtss.client.model.JobConfig;
import com.dtss.server.core.zk.callback.AppJobChangeNotifyManager;
import com.dtss.server.dao.JobConfigDAO;
import com.dtss.server.service.JobAddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.01
 */
@Service
public class JobAddServiceImpl implements JobAddService {

    @Autowired
    private JobConfigDAO jobConfigDAO;

    @Autowired
    private AppJobChangeNotifyManager appJobChangeNotifyManager;

    @Override
    public boolean addJob(JobConfig jobConfig) {

        if (jobConfig == null) {
            return false;
        }

        jobConfig.setVersion(0L);
        int num = jobConfigDAO.insert(jobConfig);

        if (num > 0) {
            appJobChangeNotifyManager.notifyChange(jobConfig.getApp());
            return true;
        } else {
            return false;
        }
    }
}
