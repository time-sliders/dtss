package com.dtss.client.core.zk;

import com.dtss.client.core.SystemStateConstructor;
import com.dtss.client.core.zk.model.ZooKeeperPathNode;
import com.dtss.commons.MillisecondInt;
import org.apache.commons.collections.CollectionUtils;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * zk最底层实现
 *
 * @author luyun
 * @since 2018.01.27 11:18
 */
public class ZooKeeperComponent implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperComponent.class);

    private ZooKeeper zooKeeper;

    private String connectString;

    private Integer sessionTimeOut;

    private List<SystemStateConstructor> systemStateConstructorList;

    public ZooKeeperComponent() {
        connectString = "127.0.0.1";
        sessionTimeOut = MillisecondInt.SIX_SECONDS;
    }

    public ZooKeeperComponent(String connectString, int sessionTimeOut) {
        this.connectString = connectString;
        this.sessionTimeOut = sessionTimeOut;
    }

    /**
     * 递归创建节点，以及节点的子节点信息
     *
     * @param node 节点信息
     */
    public void createNodeRecursively(ZooKeeperPathNode node) {
        zooKeeper.create(node.getCurrentPath(),
                node.getCurrentData(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                node.getCreateMode(), createNodeCallBack, node);
    }

    private AsyncCallback.StringCallback createNodeCallBack = new AsyncCallback.StringCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, String name) {
            ZooKeeperPathNode currentNode = (ZooKeeperPathNode) ctx;
            switch (KeeperException.Code.get(rc)) {
                case SESSIONEXPIRED:
                case CONNECTIONLOSS:
                    logger.info("[DTSS]ZooKeeper创建节点[" + path + "]时连接丢失,开始重试...");
                    ZooKeeperPathNode node = (ZooKeeperPathNode) ctx;
                    createNodeRecursively(node);
                    break;
                case OK:
                    logger.info("[DTSS]ZooKeeper创建节点[" + path + "]成功.");
                    currentNode.okCallback();
                case NODEEXISTS:
                    if (currentNode.hasNext()) {
                        for (ZooKeeperPathNode nextPathNode : currentNode.getNextPathNodeList()) {
                            createNodeRecursively(nextPathNode);
                        }
                    }
                    currentNode.okOrExistsCallback();
                    break;
                default:
                    logger.error("[DTSS]ZooKeeper创建节点[" + path + "]失败,返回码:" + KeeperException.Code.get(rc));
            }
        }
    };

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initialZooKeeper();
    }

    private void initialZooKeeper() throws IOException {
        if (zooKeeper != null) {
            try {
                zooKeeper.close();
            } catch (InterruptedException e) {
                logger.info("close zooKeeper occur InterruptedException", e);
            }
        }

        zooKeeper = new ZooKeeper(connectString, sessionTimeOut, new Watcher() {
            @Override
            public void process(WatchedEvent event) {

                switch (event.getState()) {
                    case Expired:
                        /*
                         * zookeeper明确超时
                         * 此时cluster已经将当前zookeeper对应的sessionId移除
                         * client必须要重新建立zookeeper以及做以下三件事情:
                         * 1.recreating ephemeral nodes
                         * 2.vying for leadership roles
                         * 3.reconstructing published state
                         *
                         * notice:Library writers should be conscious of the severity of the
                         * expired state and not try to recover from it
                         */
                        try {
                            initialZooKeeper();
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                        break;
                    case SyncConnected:
                        break;
                    case Disconnected:
                        //disconnected之后zooKeeper会自动重连这里不用管
                        break;
                    default:
                        logger.info("[DTSS]ZooKeeper触发事件>Path[" + event.getPath()
                                + "]Type[" + event.getType() + "]State[" + event.getState() + "]");
                }
            }
        });

        reconstructingSystemState();
    }

    private void reconstructingSystemState() {
        if (CollectionUtils.isEmpty(systemStateConstructorList)) {
            return;
        }
        for (SystemStateConstructor systemStateConstructor : systemStateConstructorList) {
            systemStateConstructor.initState();
        }
    }

    /**
     * 增加临时节点创建器
     * 当zookeeper连接断开需要重建zookeeper
     * 此时需要对系统所有临时节点进行重建
     */
    public synchronized void addSystemConstructor(SystemStateConstructor systemStateConstructor) {
        if (systemStateConstructor == null) {
            return;
        }
        if (systemStateConstructorList == null) {
            systemStateConstructorList = new ArrayList<SystemStateConstructor>();
        }
        systemStateConstructorList.add(systemStateConstructor);
    }
}
