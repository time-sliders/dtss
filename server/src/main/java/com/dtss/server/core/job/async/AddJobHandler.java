package com.dtss.server.core.job.async;

import com.alibaba.fastjson.JSON;
import com.dtss.server.core.zk.callback.AppJobChangeNotifyManager;
import com.dtss.server.util.UUIDUtil;
import com.dtss.client.core.zk.callback.NodeCreatedCallback;
import com.dtss.client.core.zk.consts.ZooKeeperConst;
import com.dtss.client.core.zk.model.ZooKeeperPathNode;
import com.dtss.client.enums.JobTriggerMode;
import com.dtss.client.model.JobConfig;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.01
 */
@Component
public class AddJobHandler extends AbstractAsyncJobChangeHandler<JobConfig> {

    @Autowired
    private AppJobChangeNotifyManager appJobChangeNotifyManager;

    @Override
    public void execute(final JobConfig jobConfig) {

        if (jobConfig == null
                || !jobConfig.isActivity()
                || !jobConfig.getTriggerMode().equals(JobTriggerMode.AUTOMATIC.getCode())) {
            return;
        }

        String jobJsonStr = JSON.toJSONString(jobConfig);
        byte[] jobNodeData = jobJsonStr.getBytes();
        // ==>/dtss/client/APP_X
        String systemNodePath = CLIENT_ROOT + I + jobConfig.getApp();
        // ==>/dtss/client/APP_X/jobs
        String jobsNodePath = systemNodePath + JOBS_NODE_NAME;
        // ==>/dtss/client/APP_X/job_change_notify
        String jobChangeNotifyNodePath = systemNodePath + JOBS_CHANGE_NODE;
        // ==>/dtss/client/APP_X/jobs/JOB_001
        String jobNodePath = getJobNodePath(jobConfig);

        ZooKeeperPathNode dtssRootNode = new ZooKeeperPathNode(SYSTEM_ROOT, ZooKeeperConst.EMPTY_DATA, CreateMode.PERSISTENT);
        ZooKeeperPathNode clientNode = dtssRootNode.addNextPath(CLIENT_ROOT, ZooKeeperConst.EMPTY_DATA, CreateMode.PERSISTENT);
        ZooKeeperPathNode systemNode = clientNode.addNextPath(systemNodePath, ZooKeeperConst.EMPTY_DATA, CreateMode.PERSISTENT);

        ZooKeeperPathNode jobsNode = systemNode
                .addNextPath(jobsNodePath, ZooKeeperConst.EMPTY_DATA, CreateMode.PERSISTENT);
        jobsNode.addNextPath(jobNodePath, jobNodeData, CreateMode.PERSISTENT);

        /*
         * 如果是当前系统建立的第一个任务，则创建任务数据变更通知节点，并启动监视
         */
        systemNode.addNextPath(jobChangeNotifyNodePath, UUIDUtil.getNewUUIDByteArr(), CreateMode.PERSISTENT, null,
                new NodeCreatedCallback() {
                    @Override
                    public void process() {
                        appJobChangeNotifyManager.watchChangeNotify(jobConfig.getApp(), null);
                    }
                });

        zooKeeperComponent.createNodeRecursively(dtssRootNode);
    }

}
