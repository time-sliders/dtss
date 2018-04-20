package com.dtss.server.core.job;

import com.alibaba.fastjson.JSON;
import com.dtss.client.consts.ZookeeperPathConst;
import com.dtss.client.core.zk.ZooKeeperComponent;
import com.dtss.client.enums.JobTriggerMode;
import com.dtss.client.model.JobConfig;
import org.apache.commons.collections.MapUtils;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
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
    private ZooKeeperComponent zooKeeperComponent;

    /**
     * 应用内缓存
     */
    private Map<String/*app*/, Map<String/*jobId*/, JobConfig>> localJobCache = new ConcurrentHashMap<String, Map<String, JobConfig>>();

    /**
     * 刷新指定的App的所有Job信息
     */
    public void refreshAllAppJob(String app, List<String> zkRegisteredJobList) {

        Map<String/*jobId*/, JobConfig> localCachedJobMap = localJobCache.get(app);
        boolean isZkEmpty = CollectionUtils.isEmpty(zkRegisteredJobList);
        boolean isLocalEmpty = MapUtils.isEmpty(localCachedJobMap);

        if (isZkEmpty && !isLocalEmpty) {
            deleteAllLocalJob(localCachedJobMap);

        } else if (!isZkEmpty && isLocalEmpty) {
            addAllZkJobToLocal(app, zkRegisteredJobList);

        } else if (!isZkEmpty) {
            // 差异对比
            // 删除本地缓存存在但是ZK已经不存在的数据
            for (Map.Entry<String, JobConfig> entry : localCachedJobMap.entrySet()) {
                JobConfig localJobConfig = entry.getValue();
                boolean isZkExistsJob = false;
                String localJobId = localJobConfig.getId();
                for (String zkJobId : zkRegisteredJobList) {
                    if (zkJobId.equals(localJobId)) {
                        isZkExistsJob = true;
                    }
                }

                if (!isZkExistsJob) {
                    deleteLocalJob(localJobConfig);
                }
            }

            // ZK存在，本地有数据，或者没有数据时，更新本地数据
            for (String jobId : zkRegisteredJobList) {
                addOrUpdateLocalJob(app, getJobNodePath(app, jobId));
            }
        }
    }

    private void addAllZkJobToLocal(String app, List<String> zkRegisteredJobList) {
        for (String jobId : zkRegisteredJobList) {
            addOrUpdateLocalJob(app, getJobNodePath(app, jobId));
        }
    }


    private void addOrUpdateLocalJob(String app, String zkJobConfigPath) {
        zooKeeperComponent.getZooKeeper()
                .getData(zkJobConfigPath, false, zkJobDataCallback, app);
    }

    private AsyncCallback.DataCallback zkJobDataCallback = new AsyncCallback.DataCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            KeeperException.Code code = KeeperException.Code.get(rc);
            String app = (String) ctx;
            switch (code) {
                case CONNECTIONLOSS:
                    logger.info("[DTSS]ZooKeeper获取任务节点数据时连接丢失,重新查询...");
                    addOrUpdateLocalJob(app, path);
                    break;
                case OK:
                    if (data == null) {
                        logger.info("[DTSS]ZooKeeper获取任务节点[" + path + "]数据时异常,节点数据为空");
                        return;
                    }
                    JobConfig zkJobConfig;
                    try {
                        String zkJobDataStr = new String(data, "UTF-8");
                        zkJobConfig = JSON.parseObject(zkJobDataStr, JobConfig.class);
                    } catch (UnsupportedEncodingException e) {
                        logger.info("[DTSS]ZooKeeper任务节点[" + path + "]数据解码失败,请检查!!!!", e);
                        return;
                    }
                    updateLocalCache(app, zkJobConfig);
                    break;
                case NONODE:
                    logger.info("[DTSS]ZooKeeper获取任务节点[" + path + "]时,节点不存在!!");
                    break;
                default:
                    logger.info("zkJobDataCallback resp warn > path:" + path + ",code:" + code);
            }
        }
    };

    private void updateLocalCache(String app, JobConfig zkJobConfig) {

        if (zkJobConfig == null || !zkJobConfig.isActivity()
                || !zkJobConfig.getTriggerMode().equals(JobTriggerMode.AUTOMATIC.getCode())) {
            return;
        }

        String jobId = zkJobConfig.getId();
        Map<String/*jobId*/, JobConfig> localJobConfigMap = localJobCache.get(app);
        if (localJobConfigMap == null) {
            localJobConfigMap = new ConcurrentHashMap<String, JobConfig>();
            localJobCache.put(app, localJobConfigMap);
        }

        JobConfig localJobConfig = localJobConfigMap.get(jobId);
        if (localJobConfig != null && localJobConfig.getVersion() > zkJobConfig.getVersion()) {
            return;
        }

        try {
            JobDetail jobDetail = schedulerWrapper.createJobDetailByJobConfig(zkJobConfig, ServerQuartzTriggerJob.class);
            Trigger trigger = schedulerWrapper.createCronTrigger(zkJobConfig);
            schedulerWrapper.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            logger.error("[DTSS][Quartz]定制任务到本地异常:jobConfig:" + JSON.toJSONString(zkJobConfig), e);
            return;
        }

        localJobConfigMap.put(zkJobConfig.getId(), zkJobConfig);
        logger.info("[DTSS][Quartz]定制任务到本地成功:[" + app + "][" + zkJobConfig.getName() + "]");
    }


    private void deleteAllLocalJob(Map<String, JobConfig> localCachedJobMap) {
        for (Iterator<Map.Entry<String, JobConfig>> i = localCachedJobMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, JobConfig> entry = i.next();
            JobConfig jobConfig = entry.getValue();
            if (deleteLocalJob(jobConfig)) {
                i.remove();
                logger.info("[DTSS][Quartz]移除任务[" + jobConfig.getName() + "-" + jobConfig.getJobBeanName()
                        + "]成功");
            } else {
                logger.info("[DTSS][Quartz]移除任务[" + jobConfig.getName() + "-" + jobConfig.getJobBeanName()
                        + "]失败!!!");
            }
        }
    }

    private boolean deleteLocalJob(JobConfig jobConfig) {
        try {
            schedulerWrapper.deleteJob(schedulerWrapper.getJobKeyByJobConfig(jobConfig));
            localJobCache.get(jobConfig.getApp()).remove(jobConfig.getId());
            return true;
        } catch (Throwable e) {
            logger.error("dtss delete job failed:" + JSON.toJSONString(jobConfig), e);
            return false;
        }
    }

    private String getJobNodePath(String app, String jobId) {
        return CLIENT_SYSTEM_ROOT + I + app + JOBS_NODE_NAME + I + jobId;
    }

}
