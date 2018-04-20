package com.dtss.server.core.job.sender.impl;

import com.alibaba.fastjson.JSON;
import com.dtss.server.core.job.sender.JobSender;
import com.dtss.server.core.job.watch.ClientSystemNodeWatcher;
import com.dtss.server.service.JobExecutiveLogService;
import com.dtss.server.service.JobLogService;
import com.dtss.client.consts.ZookeeperPathConst;
import com.dtss.client.core.zk.ZooKeeperComponent;
import com.dtss.client.core.zk.callback.NodeCreatedCallback;
import com.dtss.client.core.zk.consts.ZooKeeperConst;
import com.dtss.client.core.zk.model.ZooKeeperPathNode;
import com.dtss.client.model.JobConfig;
import com.dtss.client.model.JobExecutiveLog;
import com.dtss.client.model.ZkCmd;
import com.dtss.server.core.job.sender.util.JobPathUtil;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author luyun
 * @since 2018.01.28 11:53
 */
public abstract class AbstractJobSender implements JobSender, ZookeeperPathConst, ZooKeeperConst {

    private static final Logger logger = LoggerFactory.getLogger(AbstractJobSender.class);

    @Autowired
    private ZooKeeperComponent zooKeeperComponent;

    @Autowired
    protected ClientSystemNodeWatcher clientSystemNodeWatcher;

    @Autowired
    protected JobExecutiveLogService jobExecutiveLogService;

    @Autowired
    protected JobLogService jobLogService;

    /**
     * 发送任务
     * <p>
     * 分发任务发送任务时，直接将任务发送到/dtss/client/SYS_XXX/tasks/TASK_0001 即可
     */
    @Override
    public void send(String jobScheduledTime, JobConfig jobConfig, JobExecutiveLog log, Integer optType) {

        String app = jobConfig.getApp();

        String client = getClientIp(jobConfig, log);
        if (client == null) {
            return;
        }

        updateJobClientIp(log, client);

        // 任务节点路径
        String taskNodePath = JobPathUtil.buildTaskAssignNode(app, jobConfig.getId(), jobScheduledTime, client);

        // 组织任务节点数据
        ZkCmd zkCmd = new ZkCmd();
        zkCmd.setJobId(jobConfig.getId());
        zkCmd.setLogId(log.getId());
        zkCmd.setJobScheduledTime(jobScheduledTime);
        zkCmd.setJobType(jobConfig.getType());
        zkCmd.setJobBeanName(jobConfig.getJobBeanName());
        zkCmd.setParam(jobConfig.getParam());
        zkCmd.setOptType(optType);
        byte[] taskNodeData = JSON.toJSON(zkCmd).toString().getBytes();

        createTaskNode(taskNodePath, taskNodeData, zkCmd);
    }

    private void updateJobClientIp(JobExecutiveLog log, String client) {
        JobExecutiveLog logUpdate = new JobExecutiveLog();
        logUpdate.setId(log.getId());
        logUpdate.setExecuteClientIp(client);
        jobExecutiveLogService.updateById(logUpdate);
    }

    /**
     * 获取执行此次任务服务器的IP
     * 各子类根据不同的策略实现
     */
    protected abstract String getClientIp(JobConfig jobConfig, JobExecutiveLog log);

    private void createTaskNode(String path, byte[] data, ZkCmd zkCmd) {
        zooKeeperComponent.getZooKeeper()
                .create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        /*
                         * 注意分配的任务节点是持久节点
                         * 为了防止某一个机器网络分片之后，任务不会丢失，
                         * Server有一个重新分配未执行任务的机会
                         */
                        CreateMode.PERSISTENT,
                        createTaskNodeCallBack, zkCmd);
    }

    private AsyncCallback.StringCallback createTaskNodeCallBack = new AsyncCallback.StringCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, String name) {
            KeeperException.Code code = KeeperException.Code.get(rc);
            ZkCmd zkCmd = (ZkCmd) ctx;
            byte[] data = JSON.toJSON(ctx).toString().getBytes();
            switch (code) {
                case SESSIONEXPIRED:
                case CONNECTIONLOSS:
                    createTaskNode(path, data, zkCmd);
                    break;
                case NONODE:
                    tryCreateAllPath(path, data, zkCmd);
                    break;
                case OK:
                    logger.info("[DTSS]已通知任务[" + zkCmd.getJobBeanName() + "]执行, zkNode: " + path + "");
                    break;
                default:
                    logger.error("[DTSS]zooKeeper创建任务节点[" + path + "]失败,ZooKeeper返回码:" + code);
            }
        }

        /**
         * 尝试递归创建所有节点
         *
         * /dtss/client/app_XXX/assign/IP_XXX/TASK_001
         *  @param path 节点信息
         * @param data 节点数据
         */
        private void tryCreateAllPath(final String path, final byte[] data, ZkCmd zkCmd) {

            String[] nodeNameArray = path.split(I);
            String appName = nodeNameArray[3];
            String clientIp = nodeNameArray[5];
            String taskName = nodeNameArray[6];

            String appNodePath = CLIENT_ROOT + I + appName;
            String assignNodePath = appNodePath + ASSIGN_NODE_NAME;
            String ipNodePath = assignNodePath + I + clientIp;
            String taskNodePath = ipNodePath + I + taskName;

            ZooKeeperPathNode systemNode =
                    new ZooKeeperPathNode(SYSTEM_ROOT, EMPTY_DATA, CreateMode.PERSISTENT);

            systemNode
                    .addNextPath(CLIENT_ROOT, EMPTY_DATA, CreateMode.PERSISTENT)
                    .addNextPath(appNodePath, EMPTY_DATA, CreateMode.PERSISTENT)
                    .addNextPath(assignNodePath, EMPTY_DATA, CreateMode.PERSISTENT)
                    .addNextPath(ipNodePath, EMPTY_DATA, CreateMode.PERSISTENT,
                            new IpNodeCreateCallback(taskNodePath, data, zkCmd));

            zooKeeperComponent.createNodeRecursively(systemNode);
        }
    };


    class IpNodeCreateCallback extends NodeCreatedCallback {

        private String taskPath;
        private byte[] data;
        private ZkCmd zkCmd;

        IpNodeCreateCallback(String taskPath, byte[] data, ZkCmd zkCmd) {
            this.taskPath = taskPath;
            this.data = data;
            this.zkCmd = zkCmd;
        }

        @Override
        public void process() {
            createTaskNode(taskPath, data, zkCmd);
        }
    }

}
