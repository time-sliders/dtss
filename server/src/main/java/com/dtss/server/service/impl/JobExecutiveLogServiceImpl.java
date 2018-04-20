package com.dtss.server.service.impl;

import com.dtss.server.dao.JobConfigDAO;
import com.dtss.server.dao.JobExecutiveLogDAO;
import com.dtss.server.service.JobExecutiveLogService;
import com.dtss.client.model.JobExecutiveLog;
import com.dtss.client.model.query.JobExecutiveLogQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobExecutiveLogServiceImpl implements JobExecutiveLogService {

    @Autowired
    private JobExecutiveLogDAO jobExecutiveLogDAO;

    @Autowired
    private JobConfigDAO jobConfigDAO;

    @Override
    public List<JobExecutiveLog> list(JobExecutiveLogQuery query) {
        return jobExecutiveLogDAO.list(query);
    }

    @Override
    public Integer count(JobExecutiveLogQuery query) {
        return jobExecutiveLogDAO.count(query);
    }

    @Override
    public JobExecutiveLog findById(Long id) {
        return jobExecutiveLogDAO.findById(id);
    }

    @Override
    public int updateById(JobExecutiveLog updateParam) {

        if (updateParam == null || updateParam.getId() == null) {
            return 0;
        }
        return jobExecutiveLogDAO.updateById(updateParam);
    }

    @Override
    public int insert(JobExecutiveLog jobExecutiveLog) {
        return jobExecutiveLogDAO.insert(jobExecutiveLog);
    }

    @Override
    public int migrateToHis(JobExecutiveLog jobExecutiveLog) {
        return jobExecutiveLogDAO.migrateToHis(jobExecutiveLog);
    }

}