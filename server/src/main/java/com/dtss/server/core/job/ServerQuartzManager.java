package com.dtss.server.core.job;

import com.alibaba.fastjson.JSON;
import com.dtss.client.consts.ZookeeperPathConst;
import com.dtss.client.enums.JobTriggerMode;
import com.dtss.client.model.JobConfig;
import com.dtss.client.model.query.JobConfigQuery;
import com.dtss.server.service.JobQueryService;
import org.apache.commons.collections.MapUtils;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端Quartz Job管理器
 *
 * @author luyun
 * @since 2018.02.25 11:25
 */
@Component
public class ServerQuartzManager implements ZookeeperPathConst {

    private static final Logger logger = LoggerFactory.getLogger(ServerQuartzManager.class);

    @Autowired
    private SchedulerWrapper schedulerWrapper;

    @Autowired
    private JobQueryService jobQueryService;

    /**
     * 应用内缓存
     */
    private Map<String/*app*/, Map<Long/*jobId*/, JobConfig>> localJobCache = new ConcurrentHashMap<String, Map<Long, JobConfig>>();

    /**
     * 刷新指定的App的所有Job信息
     */
    public void refreshAllAppJob(String app) {

        JobConfigQuery query = new JobConfigQuery();
        query.setApp(app);
        query.setActivity(true);
        query.setTriggerMode(JobTriggerMode.AUTOMATIC.getCode());
        List<JobConfig> dbJobList = jobQueryService.query(query);
        Map<Long/*jobId*/, JobConfig> localCachedJobMap = localJobCache.get(app);
        boolean isDBEmpty = CollectionUtils.isEmpty(dbJobList);
        boolean isMemEmpty = MapUtils.isEmpty(localCachedJobMap);

        if (isDBEmpty && !isMemEmpty) {
            deleteAllMemJob(localCachedJobMap);

        } else if (!isDBEmpty && isMemEmpty) {
            addAllDBJobToMem(dbJobList);

        } else if (!isDBEmpty) {
            // 差异对比
            // 删除本地缓存存在但是ZK已经不存在的数据
            for (Map.Entry<Long, JobConfig> entry : localCachedJobMap.entrySet()) {
                JobConfig localJobConfig = entry.getValue();
                boolean isDBExistsJob = false;
                Long memJobId = localJobConfig.getId();
                for (JobConfig dbJob : dbJobList) {
                    if (dbJob.getId().equals(memJobId)) {
                        isDBExistsJob = true;
                    }
                }

                if (!isDBExistsJob) {
                    deleteLocalJob(localJobConfig);
                }
            }

            // DB存在，本地有数据，或者没有数据时，更新本地数据
            for (JobConfig dbJob : dbJobList) {
                updateLocalCache(dbJob);
            }
        }
    }

    private void addAllDBJobToMem(List<JobConfig> dbJobList) {
        for (JobConfig jobConfig : dbJobList) {
            updateLocalCache(jobConfig);
        }
    }

    public void updateLocalCache(JobConfig jobConfig) {

        String app = jobConfig.getApp();

        if (!jobConfig.isActivity()
                || !jobConfig.getTriggerMode().equals(JobTriggerMode.AUTOMATIC.getCode())) {
            return;
        }

        Long jobId = jobConfig.getId();
        Map<Long/*jobId*/, JobConfig> localJobConfigMap = localJobCache.get(app);
        if (localJobConfigMap == null) {
            localJobConfigMap = new ConcurrentHashMap<Long, JobConfig>();
            localJobCache.put(app, localJobConfigMap);
        }

        JobConfig localJobConfig = localJobConfigMap.get(jobId);
        if (localJobConfig != null && localJobConfig.getVersion() > jobConfig.getVersion()) {
            return;
        }

        try {
            JobDetail jobDetail = schedulerWrapper.createJobDetailByJobConfig(jobConfig, ServerQuartzTriggerJob.class);
            Trigger trigger = schedulerWrapper.createCronTrigger(jobConfig);
            schedulerWrapper.scheduleJob(jobDetail, trigger);
            logger.info("[DTSS]定制任务到本地成功 > app: " + jobConfig.getApp() + ", name: " + jobConfig.getName());
        } catch (SchedulerException e) {
            logger.error("[DTSS][Quartz]定制任务到本地异常:jobConfig:" + JSON.toJSONString(jobConfig), e);
            return;
        }

        localJobConfigMap.put(jobConfig.getId(), jobConfig);
        logger.info("[DTSS][Quartz]定制任务到本地成功:[" + app + "][" + jobConfig.getName() + "]");
    }


    private void deleteAllMemJob(Map<Long, JobConfig> localCachedJobMap) {
        for (Iterator<Map.Entry<Long, JobConfig>> i = localCachedJobMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<Long, JobConfig> entry = i.next();
            JobConfig jobConfig = entry.getValue();
            if (deleteLocalJob(jobConfig)) {
                i.remove();
                logger.info("[DTSS][Quartz]移除任务[" + jobConfig.getName() + "-" + jobConfig.getJobBeanName() + "]成功");
            } else {
                logger.info("[DTSS][Quartz]移除任务[" + jobConfig.getName() + "-" + jobConfig.getJobBeanName() + "]失败!!!");
            }
        }
    }

    public boolean deleteLocalJob(JobConfig jobConfig) {
        try {
            schedulerWrapper.deleteJob(schedulerWrapper.getJobKeyByJobConfig(jobConfig));
            localJobCache.get(jobConfig.getApp()).remove(jobConfig.getId());
            return true;
        } catch (Throwable e) {
            logger.error("dtss delete job failed:" + JSON.toJSONString(jobConfig), e);
            return false;
        }
    }

}
