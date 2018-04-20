package com.dtss.server.core.job.async;

import com.alibaba.fastjson.JSON;
import com.dtss.server.core.job.model.JobModifyReq;
import com.dtss.server.core.zk.callback.AppJobChangeNotifyManager;
import com.dtss.server.util.UUIDUtil;
import com.dtss.client.model.JobConfig;
import com.dtss.server.core.job.model.JobChangeNotifyChangeReq;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.01
 */
@Component
public class ModifyJobHandler extends AbstractAsyncJobChangeHandler<JobModifyReq> {

    private static final Logger logger = LoggerFactory.getLogger(ModifyJobHandler.class);

    @Autowired
    private AddJobHandler addJobHandler;

    @Autowired
    private AppJobChangeNotifyManager appJobChangeNotifyManager;

    @Override
    public void execute(JobModifyReq req) {
        JobConfig jobConfig = req.getNewJobConfig();
        zooKeeperComponent.getZooKeeper()
                .getData(getJobNodePath(jobConfig), false, zkJobsNodeModifyQueryCallback, req);
    }

    private AsyncCallback.DataCallback zkJobsNodeModifyQueryCallback = new AsyncCallback.DataCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            KeeperException.Code code = KeeperException.Code.get(rc);
            JobModifyReq req = (JobModifyReq) ctx;
            switch (code) {
                case SESSIONEXPIRED:
                case CONNECTIONLOSS:
                    execute(req);
                    break;
                case OK:
                    String jobConfigJsonStr = new String(data);
                    if (StringUtils.isBlank(jobConfigJsonStr)) {
                        return;
                    }

                    JobConfig zkJobConfig = JSON.parseObject(jobConfigJsonStr, JobConfig.class);
                    if (zkJobConfig.getVersion() >= req.getNewVersion()) {
                        return;
                    }

                    String jobJsonStr = JSON.toJSONString(req.getNewJobConfig());
                    byte[] jobNodeData = jobJsonStr.getBytes();
                    zooKeeperComponent.getZooKeeper()
                            .setData(path, jobNodeData, stat.getVersion(), zkJobsNodeModifyUpdateCallback, req);
                    break;
                case NONODE:
                    addJobHandler.execute(req.getNewJobConfig());
                    break;
                default:
                    logger.info("zkJobDataCallback resp warn > path:" + path + ",code:" + code);
            }
        }
    };

    private AsyncCallback.StatCallback zkJobsNodeModifyUpdateCallback = new AsyncCallback.StatCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            KeeperException.Code code = KeeperException.Code.get(rc);
            JobModifyReq req = (JobModifyReq) ctx;
            JobConfig jobConfig = req.getNewJobConfig();
            switch (code) {
                case BADVERSION:
                case SESSIONEXPIRED:
                case CONNECTIONLOSS:
                    execute(req);
                    break;
                case OK:
                    logger.info("[DTSS]ZooKeeper任务节点数据更新成功 > 任务名称:"
                            + jobConfig.getName() + ",最新版本:" + req.getNewVersion());
                    JobChangeNotifyChangeReq notifyChangeReq = new JobChangeNotifyChangeReq();
                    notifyChangeReq.setApp(req.getNewJobConfig().getApp());
                    notifyChangeReq.setData(UUIDUtil.getNew().getBytes());
                    appJobChangeNotifyManager.update(notifyChangeReq);
                    break;
                case NONODE:
                    logger.info("[DTSS]ZooKeeper更新任务[" + jobConfig.getName() + "]时,发现任务节点["
                            + path + "]不存在,尝试新建...");
                    addJobHandler.execute(req.getNewJobConfig());
                    break;
                default:
                    logger.info("zkJobDataCallback resp warn > path:" + path + ",code:" + code);
            }
        }
    };
}
