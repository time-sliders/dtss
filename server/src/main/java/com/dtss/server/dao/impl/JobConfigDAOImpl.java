package com.dtss.server.dao.impl;

import com.dtss.server.dao.JobConfigDAO;
import com.dtss.client.enums.JobTriggerMode;
import com.dtss.client.model.JobConfig;
import com.dtss.client.model.query.JobConfigQuery;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

@Repository
public class JobConfigDAOImpl extends SqlSessionDaoSupport implements JobConfigDAO {


    @Override
    public List<JobConfig> query(JobConfigQuery query) {

        Assert.notNull(query);

        query.decorate();

        return this.getSqlSession().selectList("JobConfig.QUERY", query);
    }

    @Override
    public Integer count(JobConfigQuery query) {

        Assert.notNull(query);

        return this.getSqlSession().selectOne("JobConfig.COUNT", query);
    }

    @Override
    public JobConfig findById(Long id) {

        Assert.notNull(id, "id不能为空");

        return this.getSqlSession().selectOne("JobConfig.FIND_BY_ID", id);
    }

    @Override
    public int updateById(JobConfig updateParam) {

        Assert.notNull(updateParam);
        Assert.notNull(updateParam.getId(), "id不能为空");

        updateParam.setCreateTime(null);

        Date now = new Date();
        updateParam.setModifyTime(now);
        updateParam.setLastModifyTimeLong(now.getTime());

        return this.getSqlSession().update("JobConfig.UPDATE_BY_ID", updateParam);
    }

    @Override
    public int insert(JobConfig jobConfig) {

        checkParamForInsert(jobConfig);

        jobConfig.setVersion(0L);
        Date now = new Date();
        jobConfig.setCreateTime(now);
        jobConfig.setLastModifyTimeLong(now.getTime());
        jobConfig.setModifyTime(null);

        return this.getSqlSession().insert("JobConfig.INSERT", jobConfig);
    }

    @Override
    public int deleteById(Long id) {

        Assert.notNull(id, "id不能为空");

        return this.getSqlSession().delete("JobConfig.DELETE_BY_ID", id);
    }

    private void checkParamForInsert(JobConfig jobConfig) {

        Assert.notNull(jobConfig);

        Assert.notNull(jobConfig.getApp());
        Assert.notNull(jobConfig.getType());
        Assert.notNull(jobConfig.getJobBeanName());

        if (!JobTriggerMode.PASSIVE.getCode().equals(jobConfig.getTriggerMode())) {
            Assert.notNull(jobConfig.getCron());
        }
    }


}