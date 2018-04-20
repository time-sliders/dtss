package com.dtss.server.core.job.watch;

import com.alibaba.fastjson.JSON;
import com.dtss.client.consts.ZookeeperPathConst;
import com.dtss.client.core.zk.ZooKeeperComponent;
import com.dtss.client.core.zk.callback.NodeCreatedCallback;
import com.dtss.client.core.zk.consts.ZooKeeperConst;
import com.dtss.client.core.zk.model.ZooKeeperPathNode;
import com.dtss.client.core.zk.watcher.AbstractAppWatcher;
import com.dtss.server.core.job.ServerQuartzManager;
import com.dtss.server.core.zk.callback.AppJobChangeNotifyManager;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 客户端系统节点监视器
 *
 * @author luyun
 * @since 2018.01.28 12:03
 */
@Component
public class ClientSystemNodeWatcher implements Watcher, ZookeeperPathConst, ZooKeeperConst {

    private static final Logger logger = LoggerFactory.getLogger(ClientSystemNodeWatcher.class);

    @Autowired
    private ZooKeeperComponent zooKeeperComponent;

    @Autowired
    private ServerQuartzManager serverQuartzManager;

    @Autowired
    private AppJobChangeNotifyManager appJobChangeNotifyManager;

    /**
     * 各个应用的存活客户端IP列表
     * 一写多读
     */
    private Map<String/*app*/, List<String>/*clientIpList*/> systemInfoMap = new HashMap<String, List<String>>();


    //～～～～～～～～～～～～～～～～～～～～/dtss/client～～～～～～～～～～～～～～～～～～～～

    public void startWatch() {
        zooKeeperComponent.getZooKeeper().
                getChildren(CLIENT_ROOT, this, clientChildrenCallback, null);
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case NodeDeleted:
                logger.info("[DTSS]接入应用父节点[" + event.getPath() + "]被意外删除,尝试重建...");
                tryCreateClientNode();
                break;
            case NodeChildrenChanged:
                logger.info("[DTSS]发现新系统加入或移除,开始加载系统列表");
            default:
                logger.info("ClientSystemNodeWatcher " + event.getState() + "|" + event.getType());
                startWatch();
        }
    }

    private AsyncCallback.ChildrenCallback clientChildrenCallback = new AsyncCallback.ChildrenCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            KeeperException.Code code = KeeperException.Code.get(rc);
            switch (code) {
                case OK:
                    int num = children == null || children.isEmpty() ? 0 : children.size();
                    initialSystemMap(children);
                    logger.info("[DTSS]已接入系统总数:" + num);
                    break;
                case NONODE:
                    tryCreateClientNode();
                    break;
                default:
            }
        }
    };

    private void initialSystemMap(List<String> children) {

        if (children == null || children.size() == 0) {
            return;
        }

        for (String app : children) {
            if (systemInfoMap.get(app) != null) {
                continue;
            }

            // 监控应用的注册节点
            startAppClientsWatch(app);

            // 监控应用的JOB配置节点
            startAppJobsWatch(app);

            // 监控应用的job_change_notify更新通知节点
            appJobChangeNotifyManager.watchChangeNotify(app, null);
        }
    }

    //～～～～～～～～～～～～～～～～～～～～/dtss/client/APP_X/clients～～～～～～～～～～～～～～～～～～～～
    private void startAppClientsWatch(String app) {
        zooKeeperComponent.getZooKeeper()
                .getChildren(CLIENT_SYSTEM_ROOT + I + app + CLIENTS_NODE_NAME,
                        new AppClientsNodeWatcher(app),
                        appClientsChildrenCallback, app);
    }

    class AppClientsNodeWatcher extends AbstractAppWatcher {

        AppClientsNodeWatcher(String app) {
            super(app);
        }

        @Override
        public void process(WatchedEvent event) {
            switch (event.getType()) {
                case NodeDeleted:
                    logger.info("[DTSS]应用[" + app + "]的客户端注册节点[" + event.getPath() + "]被意外删除,尝试重建...");
                    tryCreateClientsNode(app);
                    break;
                case NodeChildrenChanged:
                    logger.info("[DTSS]应用[" + app + "]有机器加入或停止,开始刷新客户端节点列表...");
                default:
                    startAppClientsWatch(app);
            }
        }
    }

    private AsyncCallback.ChildrenCallback appClientsChildrenCallback = new AsyncCallback.ChildrenCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            String app = (String) ctx;
            KeeperException.Code code = KeeperException.Code.get(rc);
            switch (code) {
                case OK:
                    int num = children == null || children.isEmpty() ? 0 : children.size();
                    systemInfoMap.put(app, children);
                    // 监视每一个任务的数据变化
                    logger.info("[DTSS]刷新应用[" + app + "]存活节点信息的本地缓存数据成功,存活节点总量:" + num
                            + ",存活节点:" + JSON.toJSONString(children));
                    break;
                case NONODE:
                    logger.info("[DTSS]在客户端应用[" + app + "]暂无存活客户端节点");
                    break;
                default:
            }
        }
    };

    /**
     * 尝试创建/dtss/client/systems节点
     */
    private void tryCreateClientNode() {

        ZooKeeperPathNode systemNode =
                new ZooKeeperPathNode(SYSTEM_ROOT, EMPTY_DATA, CreateMode.PERSISTENT);

        systemNode
                .addNextPath(CLIENT_ROOT, EMPTY_DATA, CreateMode.PERSISTENT, null,
                        new NodeCreatedCallback() {
                            @Override
                            public void process() {
                                startWatch();
                            }
                        });

        zooKeeperComponent.createNodeRecursively(systemNode);
    }

    /**
     * 尝试创建/dtss/client/systems/APP_XXX/clients节点
     */
    private void tryCreateClientsNode(final String app) {

        ZooKeeperPathNode systemNode =
                new ZooKeeperPathNode(SYSTEM_ROOT, EMPTY_DATA, CreateMode.PERSISTENT);

        String appPath = CLIENT_SYSTEM_ROOT + I + app;
        String appClientsPath = appPath + CLIENTS_NODE_NAME;

        systemNode
                .addNextPath(CLIENT_ROOT, EMPTY_DATA, CreateMode.PERSISTENT)
                .addNextPath(CLIENT_SYSTEM_ROOT, EMPTY_DATA, CreateMode.PERSISTENT)
                .addNextPath(appPath, EMPTY_DATA, CreateMode.PERSISTENT)
                .addNextPath(appClientsPath, EMPTY_DATA, CreateMode.PERSISTENT,
                        new NodeCreatedCallback() {
                            @Override
                            public void process() {
                                startAppClientsWatch(app);
                            }
                        });

        zooKeeperComponent.createNodeRecursively(systemNode);
    }

    //～～～～～～～～～～～～～～～～～～～～/dtss/client/APP_X/jobs～～～～～～～～～～～～～～～～～～～～
    public void startAppJobsWatch(String app) {
        zooKeeperComponent.getZooKeeper()
                .getChildren(CLIENT_SYSTEM_ROOT + I + app + JOBS_NODE_NAME,
                        new AppJobsNodeWatcher(app),
                        appJobsChildrenCallback, app);
    }

    class AppJobsNodeWatcher extends AbstractAppWatcher {

        AppJobsNodeWatcher(String app) {
            super(app);
        }

        @Override
        public void process(WatchedEvent event) {
            switch (event.getType()) {
                case NodeDeleted:
                    logger.warn("[DTSS]应用[" + app + "]的任务注册父节点[" + event.getPath() + "]被意外删除,尝试重建...");
                    tryCreateJobsNode(app);
                    break;
                case NodeChildrenChanged:
                    logger.info("[DTSS]应用[" + app + "]的有新任务加入或移除,开始刷新任务数据");
                default:
                    startAppJobsWatch(app);
            }
        }
    }

    private AsyncCallback.ChildrenCallback appJobsChildrenCallback = new AsyncCallback.ChildrenCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            String app = (String) ctx;
            switch (KeeperException.Code.get(rc)) {
                case OK:
                    int num = children == null || children.isEmpty() ? 0 : children.size();
                    logger.info("[DTSS]开始刷新应用[" + app + "]的本地任务缓存数据, 任务总量:" + num
                            + ",任务列表:" + JSON.toJSONString(children));
                    serverQuartzManager.refreshAllAppJob(app, children);
                    break;
                case NONODE:
                    tryCreateJobsNode((String) ctx);
                    break;
                default:
            }
        }
    };

    /**
     * 尝试创建/dtss/client/systems/APP_XXX/jobs节点
     */
    private void tryCreateJobsNode(final String app) {

        ZooKeeperPathNode systemNode =
                new ZooKeeperPathNode(SYSTEM_ROOT, EMPTY_DATA, CreateMode.PERSISTENT);

        String appPath = CLIENT_SYSTEM_ROOT + I + app;
        String appJobsPath = appPath + JOBS_NODE_NAME;

        systemNode
                .addNextPath(CLIENT_ROOT, EMPTY_DATA, CreateMode.PERSISTENT)
                .addNextPath(CLIENT_SYSTEM_ROOT, EMPTY_DATA, CreateMode.PERSISTENT)
                .addNextPath(appPath, EMPTY_DATA, CreateMode.PERSISTENT)
                .addNextPath(appJobsPath, EMPTY_DATA, CreateMode.PERSISTENT, null,
                        new NodeCreatedCallback() {
                            @Override
                            public void process() {
                                startAppJobsWatch(app);
                            }
                        });

        zooKeeperComponent.createNodeRecursively(systemNode);
    }

    /**
     * 从当前注册的客户端列表中，随机获取一个
     */
    public String getClientRandomly(String app) {

        if (StringUtils.isEmpty(app) || systemInfoMap.size() == 0) {
            return null;
        }

        List<String> appClientsList = systemInfoMap.get(app);
        if (appClientsList == null || appClientsList.size() == 0) {
            return null;
        }

        int size = appClientsList.size();
        Random random = new Random();
        int i = random.nextInt(size);
        return appClientsList.get(i);
    }

    public List<String> getAllClientList(String app) {
        if (StringUtils.isBlank(app)) {
            return null;
        }
        return systemInfoMap.get(app);
    }



}
