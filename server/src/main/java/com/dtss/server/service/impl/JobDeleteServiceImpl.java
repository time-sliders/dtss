package com.dtss.server.service.impl;

import com.dtss.client.model.JobConfig;
import com.dtss.server.core.job.async.DeleteJobHandler;
import com.dtss.server.dao.JobConfigDAO;
import com.dtss.server.service.JobDeleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.01
 */
@Service
public class JobDeleteServiceImpl implements JobDeleteService {

    @Autowired
    private JobConfigDAO jobConfigDAO;

    @Autowired
    private DeleteJobHandler deleteJobHandler;

    @Override
    public boolean deleteJob(String id) {
        JobConfig jobConfig = jobConfigDAO.findById(id);
        if (jobConfig == null) {
            return true;
        }

        int num = jobConfigDAO.deleteById(id);
        if (num > 0) {
            deleteJobHandler.submitAsyncTask(jobConfig);
            return true;
        } else {
            return false;
        }
    }
}
