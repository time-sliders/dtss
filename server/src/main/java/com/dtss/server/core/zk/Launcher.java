package com.dtss.server.core.zk;

import com.dtss.commons.IPUtils;
import com.dtss.server.core.job.watch.ClientSystemNodeWatcher;
import com.dtss.client.consts.ZookeeperPathConst;
import com.dtss.client.core.SystemStateConstructor;
import com.dtss.client.core.zk.ZooKeeperComponent;
import com.dtss.client.core.zk.consts.ZooKeeperConst;
import com.dtss.client.core.zk.model.ZooKeeperPathNode;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Dtss服务器端启动程序
 *
 * @author luyun
 * @since 2018.01.20
 */
@Component
@DependsOn("zooKeeperComponent")
public class Launcher implements InitializingBean, ZookeeperPathConst, SystemStateConstructor {

    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    @Autowired
    private ZooKeeperComponent zooKeeperComponent;

    @Autowired
    private ClientSystemNodeWatcher clientSystemNodeWatcher;

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
         * 监听客户端列表节点的变化
         */
        clientSystemNodeWatcher.startWatch();
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
