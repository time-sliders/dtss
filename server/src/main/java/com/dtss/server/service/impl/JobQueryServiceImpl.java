package com.dtss.server.service.impl;

import com.dtss.server.dao.JobConfigDAO;
import com.dtss.server.service.JobQueryService;
import com.dtss.client.model.JobConfig;
import com.dtss.client.model.query.JobConfigQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author luyun
 * @since 2018.02.10 16:12
 */
@Service
public class JobQueryServiceImpl implements JobQueryService {

    @Autowired
    private JobConfigDAO jobConfigDAO;

    @Override
    public List<JobConfig> query(JobConfigQuery query) {
        return jobConfigDAO.query(query);
    }

    @Override
    public Integer count(JobConfigQuery query) {
        return jobConfigDAO.count(query);
    }

    @Override
    public JobConfig findById(String id) {
        return jobConfigDAO.findById(id);
    }
}
