package com.dtss.client.core;

import com.alibaba.fastjson.JSON;
import com.dtss.client.consts.ZookeeperPathConst;
import com.dtss.client.core.handle.AssignedTaskHandler;
import com.dtss.client.core.zk.ZooKeeperComponent;
import com.dtss.client.core.zk.callback.NodeCreatedCallback;
import com.dtss.client.core.zk.model.ZooKeeperPathNode;
import com.dtss.client.core.zk.watcher.AbstractAppWatcher;
import com.dtss.client.core.zk.consts.ZooKeeperConst;
import com.dtss.commons.IPUtils;
import com.dtss.commons.Millisecond;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.*;

/**
 * 客户端启动器
 *
 * @author luyun
 * @since 2018.01.27 15:19
 */
public class ZookeeperClientLauncher implements InitializingBean, ZookeeperPathConst, ZooKeeperConst, SystemStateConstructor {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperClientLauncher.class);

    /**
     * 当前应用名称
     */
    private String app;

    /**
     * 应用节点
     */
    private String appNodePath;

    /**
     * 本机任务分配地址
     */
    private String localAssignNodePath;

    /**
     * zooKeeper注册地址
     */
    private String localRegistryPath;

    @Autowired
    private ZooKeeperComponent zooKeeperComponent;

    @Autowired
    private AssignedTaskHandler handler;

    /**
     * 获取任务并处理任务列表的异步线程池
     * 为了不阻塞callback
     */
    private static ThreadPoolExecutor tpe;

    static {
        tpe = new ThreadPoolExecutor(20, 40,
                Millisecond.TEN_SECONDS, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(100),
                new ThreadFactory() {
                    private static final String THREAD_NAME = "DTSS_CLIENT_JOB_THREAD";

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, THREAD_NAME);
                    }
                },
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        /*
                         * 这里为了避免zk的callback函数不阻塞，直接忽略掉数据
                         * [callback阻塞会导致所有callback被阻塞(ZK单线程处理所有callback)，
                         * 甚至导致ZK内置的心跳探测超时，比较严重]
                         */
                        logger.info("task ignored:" + JSON.toJSONString(((TaskNodeConsumer) r).cmdPath));
                    }
                });
        tpe.allowCoreThreadTimeOut(true);
    }

    public ZookeeperClientLauncher() {
    }

    public ZookeeperClientLauncher(String app) {
        this.app = app;
        newTaskNodeCreatedWatcher = new NewTaskNodeCreatedWatcher(app);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (StringUtils.isBlank(app)) {
            throw new NullPointerException("property 'app' could not be null");
        }

        /*
         * 2.注册机器节点
         */
        initState();
        zooKeeperComponent.addSystemConstructor(this);
    }

    private void initPathProperties(String app) {
        appNodePath = CLIENT_ROOT + I + app;
        String ip = IPUtils.getLocalIp();
        localAssignNodePath = appNodePath + ASSIGN_NODE_NAME + I + ip;
        localRegistryPath = appNodePath + CLIENTS_NODE_NAME + I + ip;
    }

    /**
     * 监控当前应用的assign节点
     * 一旦发现任务则立即执行
     * <p>
     * /dtss/client/APP_XXX/assign/IP_XXX/20181121043210_TASK001
     */
    private void monitorZooKeeperTaskPath() {
        ZooKeeperPathNode systemNode =
                new ZooKeeperPathNode(SYSTEM_ROOT, EMPTY_DATA, CreateMode.PERSISTENT);
        String assignNodePath = appNodePath + ASSIGN_NODE_NAME;
        systemNode
                .addNextPath(CLIENT_ROOT, EMPTY_DATA, CreateMode.PERSISTENT)
                .addNextPath(appNodePath, EMPTY_DATA, CreateMode.PERSISTENT)
                .addNextPath(assignNodePath, EMPTY_DATA, CreateMode.PERSISTENT)
                .addNextPath(localAssignNodePath, EMPTY_DATA, CreateMode.PERSISTENT, tasksNodeCreateCallback);
        zooKeeperComponent.createNodeRecursively(systemNode);
    }

    private NodeCreatedCallback tasksNodeCreateCallback = new NodeCreatedCallback() {
        @Override
        public void process() {
            getTasks();
        }
    };

    private void getTasks() {
        zooKeeperComponent.getZooKeeper()
                .getChildren(localAssignNodePath, newTaskNodeCreatedWatcher, getChildrenCallback, localAssignNodePath);
    }

    private Watcher newTaskNodeCreatedWatcher;

    class NewTaskNodeCreatedWatcher extends AbstractAppWatcher {

        NewTaskNodeCreatedWatcher(String app) {
            super(app);
        }

        @Override
        public void process(WatchedEvent event) {
            switch (event.getType()) {
                case NodeDeleted:
                    monitorZooKeeperTaskPath();
                    break;

                default:
                    getTasks();
            }
        }
    }

    private AsyncCallback.ChildrenCallback getChildrenCallback = new AsyncCallback.ChildrenCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            KeeperException.Code code = KeeperException.Code.get(rc);
            switch (code) {
                case NONODE:
                    monitorZooKeeperTaskPath();
                    break;

                case OK:
                    submitAllTask(children);
                    break;
                default:
            }
        }
    };

    /**
     * 提交所有任务到异步线程池
     *
     * @param children 任务节点列表
     */
    private void submitAllTask(List<String> children) {
        if (children == null || children.isEmpty()) {
            return;
        }

        for (String cmd : children) {
            tpe.submit(new TaskNodeConsumer(cmd));
        }
    }

    class TaskNodeConsumer implements Runnable {

        String cmdPath;

        TaskNodeConsumer(String cmdPath) {
            this.cmdPath = cmdPath;
        }

        @Override
        public void run() {
            try {
                handler.handle(localAssignNodePath + I + cmdPath, app);
            } catch (Throwable e) {
                logger.error("[DTSS]zooKeeper客户端处理任务时发生异常", e);
            }
        }
    }


    @Override
    public void initState() {

        /*
         * 初始化本地相关的path信息
         */
        initPathProperties(app);

        /*
         * 1.监控任务节点
         */
        monitorZooKeeperTaskPath();

        /*
         * 2.注册机器节点
         */
        createEphemeralNode();

    }

    private void createEphemeralNode() {

        ZooKeeperPathNode systemNode =
                new ZooKeeperPathNode(SYSTEM_ROOT, EMPTY_DATA, CreateMode.PERSISTENT);

        String clientsNodePath = appNodePath + CLIENTS_NODE_NAME;

        systemNode
                .addNextPath(CLIENT_ROOT, EMPTY_DATA, CreateMode.PERSISTENT)
                .addNextPath(appNodePath, EMPTY_DATA, CreateMode.PERSISTENT)
                .addNextPath(clientsNodePath, EMPTY_DATA, CreateMode.PERSISTENT)
                .addNextPath(localRegistryPath, EMPTY_DATA, CreateMode.EPHEMERAL);

        zooKeeperComponent.createNodeRecursively(systemNode);
    }

}
