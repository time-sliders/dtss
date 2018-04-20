package com.dtss.server.dao.impl;

import com.dtss.client.model.JobExecutiveLog;
import com.dtss.client.model.query.JobExecutiveLogQuery;
import com.dtss.server.dao.JobExecutiveLogDAO;
import org.apache.commons.lang.StringUtils;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

@Repository
public class JobExecutiveLogDAOImpl extends SqlSessionDaoSupport implements JobExecutiveLogDAO {

    @Override
    public List<JobExecutiveLog> list(JobExecutiveLogQuery query) {

        Assert.notNull(query);

        query.decorate();

        return this.getSqlSession().selectList("JobExecutiveLog.QUERY", query);
    }

    @Override
    public Integer count(JobExecutiveLogQuery query) {

        Assert.notNull(query);

        return (Integer) this.getSqlSession().selectOne("JobExecutiveLog.COUNT", query);
    }

    @Override
    public JobExecutiveLog findById(Long id) {

        Assert.notNull(id, "id不能为空");

        return (JobExecutiveLog) this.getSqlSession().selectOne("JobExecutiveLog.FIND_BY_ID", id);
    }

    @Override
    public int updateById(JobExecutiveLog updateParam) {

        Assert.notNull(updateParam);
        Assert.notNull(updateParam.getId(), "id不能为空");

        updateParam.setCreateTime(null);
        updateParam.setModifyTime(new Date());
        adjustVarcharLength(updateParam);


        return this.getSqlSession().update("JobExecutiveLog.UPDATE_BY_ID", updateParam);
    }

    @Override
    public int insert(JobExecutiveLog jobExecutiveLog) {

        checkParamForInsert(jobExecutiveLog);

        adjustVarcharLength(jobExecutiveLog);
        jobExecutiveLog.setCreateTime(new Date());
        jobExecutiveLog.setModifyTime(null);

        return this.getSqlSession().insert("JobExecutiveLog.INSERT", jobExecutiveLog);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, isolation = Isolation.READ_COMMITTED)
    @Override
    public int migrateToHis(JobExecutiveLog jobExecutiveLog) {

        Assert.notNull(jobExecutiveLog);
        Assert.notNull(jobExecutiveLog.getId());

        int num = this.getSqlSession().delete("JobExecutiveLog.DELETE_BY_ID", jobExecutiveLog.getId());
        if (num > 0) {
            this.getSqlSession().insert("JobExecutiveLog.INSERT_HIS",jobExecutiveLog);
        }
        return num;
    }


    private void adjustVarcharLength(JobExecutiveLog jobExecutiveLog) {
        if (jobExecutiveLog == null) {
            return;
        }

        String innerMsg = jobExecutiveLog.getInnerMsg();
        if (StringUtils.isNotBlank(innerMsg) && innerMsg.length() > 128) {
            innerMsg = StringUtils.left(innerMsg, 128);
            jobExecutiveLog.setInnerMsg(innerMsg);
        }

        String param = jobExecutiveLog.getInnerMsg();
        if (StringUtils.isNotBlank(param) && param.length() > 512) {
            param = StringUtils.left(param, 512);
            jobExecutiveLog.setParam(param);
        }

        String jobExecuteResult = jobExecutiveLog.getJobExeResult();
        if (StringUtils.isNotBlank(jobExecuteResult) && jobExecuteResult.length() > 512) {
            jobExecuteResult = StringUtils.left(jobExecuteResult, 512);
            jobExecutiveLog.setJobExeResult(jobExecuteResult);
        }
    }

    private void checkParamForInsert(JobExecutiveLog jobExecutiveLog) {

        Assert.notNull(jobExecutiveLog);
        Assert.notNull(jobExecutiveLog.getApp());
        Assert.notNull(jobExecutiveLog.getJobBeanName());
        Assert.notNull(jobExecutiveLog.getJobId());
        Assert.notNull(jobExecutiveLog.getScheduleTime());
        Assert.notNull(jobExecutiveLog.getSliceIndex());
        Assert.notNull(jobExecutiveLog.getTriggerServerIp());

    }


}