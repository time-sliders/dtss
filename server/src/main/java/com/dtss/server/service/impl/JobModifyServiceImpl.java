package com.dtss.server.service.impl;

import com.dtss.commons.Result;
import com.dtss.server.core.job.async.DeleteJobHandler;
import com.dtss.server.core.job.async.ModifyJobHandler;
import com.dtss.server.core.job.model.JobModifyReq;
import com.dtss.server.dao.JobConfigDAO;
import com.dtss.server.service.JobModifyService;
import com.dtss.client.enums.JobTriggerMode;
import com.dtss.client.model.JobConfig;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.01
 */
@Service
public class JobModifyServiceImpl implements JobModifyService {

    @Autowired
    private JobConfigDAO jobConfigDAO;

    @Autowired
    private ModifyJobHandler modifyJobHandler;

    @Autowired
    private DeleteJobHandler deleteJobHandler;

    @Override
    public Result<Void> updateById(JobConfig jobConfig) {

        if (jobConfig == null || jobConfig.getId() == null || jobConfig.getVersion() == null
                || StringUtils.isBlank(jobConfig.getApp())
                || StringUtils.isBlank(jobConfig.getJobBeanName())) {
            return Result.buildFail(null, "参数错误");
        }

        int num = jobConfigDAO.updateById(jobConfig);
        if (num <= 0) {
            return Result.buildFail(null, "数据已经被别人更新过了，请刷新页面重试");
        }

        JobConfig dbJobConfig = jobConfigDAO.findById(jobConfig.getId());
        if (dbJobConfig.getVersion().compareTo(jobConfig.getVersion() + 1) > 0) {
            // 数据库数据已经被其他线程更新掉了
            return Result.buildSucc(null);
        }

        sendZkModifyReq(dbJobConfig);

        return Result.buildSucc(null);
    }

    public void sendZkModifyReq(JobConfig jobConfig) {

        if (!jobConfig.isActivity()
                || JobTriggerMode.PASSIVE.getCode().equals(jobConfig.getTriggerMode())) {
            deleteJobHandler.submitAsyncTask(jobConfig);

        } else {
            JobModifyReq req = new JobModifyReq();
            req.setNewVersion(jobConfig.getVersion());
            req.setNewJobConfig(jobConfig);
            modifyJobHandler.submitAsyncTask(req);
        }
    }
}