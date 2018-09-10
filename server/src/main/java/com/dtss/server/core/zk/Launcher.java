package com.dtss.server.core.zk;

import com.dtss.client.consts.ZookeeperPathConst;
import com.dtss.client.core.SystemStateConstructor;
import com.dtss.client.core.zk.ZooKeeperComponent;
import com.dtss.client.core.zk.consts.ZooKeeperConst;
import com.dtss.client.core.zk.model.ZooKeeperPathNode;
import com.dtss.client.enums.JobTriggerMode;
import com.dtss.client.model.JobConfig;
import com.dtss.client.model.query.JobConfigQuery;
import com.dtss.commons.IPUtils;
import com.dtss.commons.OrderBy;
import com.dtss.server.core.job.ServerQuartzManager;
import com.dtss.server.core.job.watch.ClientSystemNodeWatcher;
import com.dtss.server.service.JobQueryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Dtss服务器端启动程序
 *
 * @author luyun
 * @since 2018.01.20
 */
@Component
@DependsOn("zooKeeperComponent")
public class Launcher implements InitializingBean, ZookeeperPathConst, SystemStateConstructor {

    @Autowired
    private ZooKeeperComponent zooKeeperComponent;

    @Autowired
    private JobQueryService jobQueryService;

    @Autowired
    private ClientSystemNodeWatcher clientSystemNodeWatcher;

    @Autowired
    private ServerQuartzManager serverQuartzManager;

    @Override
    public void afterPropertiesSet() {
        initState();
        zooKeeperComponent.addSystemConstructor(this);
    }

    public void initState() {

        /*
         * 竞选Leader
         */
        startLeaderElection();

        /*
         * 集群注册
         */
        createServerRegistryNode();

        /*
         * 根据数据库任务数据初始化系统
         */
        initialJobFromDB();
    }

    private void initialJobFromDB() {
        JobConfigQuery query = new JobConfigQuery();
        Long gtId = 0L;
        query.setActivity(true);
        query.setTriggerMode(JobTriggerMode.AUTOMATIC.getCode());
        query.setOrderBy(new OrderBy("id", OrderBy.ASC));
        query.setOffset(0);
        int limit = 500;
        query.setLimit(limit);

        while (true) {
            query.setGtId(gtId);
            List<JobConfig> jobList = jobQueryService.query(query);
            if (CollectionUtils.isEmpty(jobList)) {
                break;
            }
            for (JobConfig job : jobList) {
                clientSystemNodeWatcher.watchApp(job.getApp());
                serverQuartzManager.updateLocalCache(job);
            }
            if (jobList.size() < limit) {
                break;
            }
            gtId = jobList.get(jobList.size() - 1).getId();
        }
    }

    /**
     * 竞选leader
     */
    private void startLeaderElection() {
        // 系统节点
        ZooKeeperPathNode dtssRootNode = new ZooKeeperPathNode(SYSTEM_ROOT, ZooKeeperConst.EMPTY_DATA, CreateMode.PERSISTENT);
        // 服务器端ROOT节点
        ZooKeeperPathNode serverNode = dtssRootNode.addNextPath(SERVER_ROOT, ZooKeeperConst.EMPTY_DATA, CreateMode.PERSISTENT);
        // Server主控服务选举节点
        serverNode.addNextPath(SERVER_ELECTION, ZooKeeperConst.EMPTY_DATA, CreateMode.PERSISTENT,
                new ServerElectionNodeCreatedCallback());
        // 递归创建目录
        zooKeeperComponent.createNodeRecursively(dtssRootNode);
    }

    private void createServerRegistryNode() {
        String nodePath = SERVER_SERVERS + I + IPUtils.getLocalIp();
        // 系统节点
        ZooKeeperPathNode dtssRootNode = new ZooKeeperPathNode(SYSTEM_ROOT, ZooKeeperConst.EMPTY_DATA, CreateMode.PERSISTENT);
        // 服务器端ROOT节点
        ZooKeeperPathNode serverNode = dtssRootNode.addNextPath(SERVER_ROOT, ZooKeeperConst.EMPTY_DATA, CreateMode.PERSISTENT);
        // 服务器列表节点
        ZooKeeperPathNode serversNode = serverNode.addNextPath(SERVER_SERVERS, ZooKeeperConst.EMPTY_DATA, CreateMode.PERSISTENT);
        // 机器IP节点
        serversNode.addNextPath(nodePath, ZooKeeperConst.EMPTY_DATA, CreateMode.EPHEMERAL);
        // 递归创建目录
        zooKeeperComponent.createNodeRecursively(dtssRootNode);
    }
}
