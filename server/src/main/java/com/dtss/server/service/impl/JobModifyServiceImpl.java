package com.dtss.server.service.impl;

import com.dtss.commons.Result;
import com.dtss.server.core.job.model.JobChangeNotifyChangeReq;
import com.dtss.server.core.zk.callback.AppJobChangeNotifyManager;
import com.dtss.server.dao.JobConfigDAO;
import com.dtss.server.service.JobModifyService;
import com.dtss.client.model.JobConfig;
import com.dtss.server.util.UUIDUtil;
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
    private AppJobChangeNotifyManager appJobChangeNotifyManager;

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

        appJobChangeNotifyManager.notifyChange(dbJobConfig.getApp());

        return Result.buildSucc(null);
    }
}