package com.dtss.server.core.zk.callback;

import com.dtss.server.core.job.ServerQuartzManager;
import com.dtss.client.consts.ZookeeperPathConst;
import com.dtss.client.core.zk.ZooKeeperComponent;
import com.dtss.client.core.zk.callback.NodeCreatedCallback;
import com.dtss.client.core.zk.consts.ZooKeeperConst;
import com.dtss.client.core.zk.model.ZooKeeperPathNode;
import com.dtss.client.core.zk.watcher.AbstractAppWatcher;
import com.dtss.server.core.job.model.JobChangeNotifyChangeReq;
import com.dtss.server.core.job.watch.ClientSystemNodeWatcher;
import com.dtss.server.util.UUIDUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.02
 */
@Component
public class AppJobChangeNotifyManager implements ZookeeperPathConst {

    private static final Logger logger = LoggerFactory.getLogger(AppJobChangeNotifyManager.class);

    @Autowired
    private ZooKeeperComponent zooKeeperComponent;

    @Autowired
    private ServerQuartzManager serverQuartzManager;

    @Autowired
    private ClientSystemNodeWatcher clientSystemNodeWatcher;

    //～～～～～～～～～～～～～～～～～～～createChangeNotifyNode～～～～～～～～～～～～～～～～～～～
    private void createChangeNotifyNode(final String app) {

        // ==>/dtss/client/APP_X
        String systemNodePath = CLIENT_ROOT + I + app;
        ZooKeeperPathNode dtssRootNode = new ZooKeeperPathNode(SYSTEM_ROOT, ZooKeeperConst.EMPTY_DATA, CreateMode.PERSISTENT);
        ZooKeeperPathNode clientNode = dtssRootNode.addNextPath(CLIENT_ROOT, ZooKeeperConst.EMPTY_DATA, CreateMode.PERSISTENT);
        ZooKeeperPathNode systemNode = clientNode.addNextPath(systemNodePath, ZooKeeperConst.EMPTY_DATA, CreateMode.PERSISTENT);

        /*
         * 如果是当前系统建立的第一个任务，则创建任务数据变更通知节点，并启动监视
         */
        // ==>/dtss/client/APP_X/job_change_notify
        String jobChangeNotifyNodePath = systemNodePath + JOBS_CHANGE_NODE;
        systemNode.addNextPath(jobChangeNotifyNodePath, UUIDUtil.getNewUUIDByteArr(), CreateMode.PERSISTENT, null,
                new NodeCreatedCallback() {
                    @Override
                    public void process() {
                        watchChangeNotify(app, null);
                    }
                });
        zooKeeperComponent.createNodeRecursively(dtssRootNode);
    }

    //～～～～～～～～～～～～～～～～～～～watchChangeNotify～～～～～～～～～～～～～～～～～～～

    public void watchChangeNotify(String app, Watcher watcher) {
        if (watcher == null) {
            watcher = new AppChangeNotifyWatcher(app);
        }
        zooKeeperComponent.getZooKeeper()
                .getData(getJobChangeNotifyNodePath(app), watcher, appJobChangeNotifyCreateDataCallback, app);
    }

    private AsyncCallback.DataCallback appJobChangeNotifyCreateDataCallback =
            new AsyncCallback.DataCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                    String app = (String) ctx;
                    KeeperException.Code code = KeeperException.Code.get(rc);
                    switch (code) {
                        case NONODE:
                            logger.info("[DTSS]监控[" + app + "][" + path + "]时,节点不存在,尝试创建节点");
                            createChangeNotifyNode(app);
                            break;
                        case OK:
                            logger.info("[DTSS]监控[" + app + "][" + path + "]成功,节点数据[" + new String(data) + "]");
                            break;
                        default:
                    }
                }
            };

    class AppChangeNotifyWatcher extends AbstractAppWatcher {

        AppChangeNotifyWatcher(String app) {
            super(app);
        }

        @Override
        public void process(WatchedEvent event) {
            switch (event.getType()) {
                case NodeDeleted:
                    createChangeNotifyNode(app);
                    break;
                case NodeDataChanged:
                    serverQuartzManager.refreshAllAppJob(app);
                default:
                    watchChangeNotify(app, this);
            }
        }
    }

    //～～～～～～～～～～～～～～～～～～～update～～～～～～～～～～～～～～～～～～～

    public void notifyChange(String app){

        clientSystemNodeWatcher.watchApp(app);

        JobChangeNotifyChangeReq req = new JobChangeNotifyChangeReq();
        req.setApp(app);
        req.setData(UUIDUtil.getNew().getBytes());
        update(req);
    }

    private void update(JobChangeNotifyChangeReq req) {
        zooKeeperComponent.getZooKeeper()
                .getData(getJobChangeNotifyNodePath(req.getApp()), false, appJobChangeNotifyUpdateDataCallback, req);
    }

    private AsyncCallback.DataCallback appJobChangeNotifyUpdateDataCallback =
            new AsyncCallback.DataCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                    KeeperException.Code code = KeeperException.Code.get(rc);
                    JobChangeNotifyChangeReq req = (JobChangeNotifyChangeReq) ctx;
                    switch (code) {
                        case SESSIONEXPIRED:
                        case CONNECTIONLOSS:
                            update(req);
                            break;
                        case OK:
                            logger.info("[DTSS]ZooKeeper已获取到修改通知节点[" + path + "]的信息,当前版本["
                                    + stat.getVersion() + "],即将执行更新...");
                            doUpdate(path, req, stat);
                            break;
                        case NONODE:
                            logger.info("[DTSS]ZooKeeper查询通知节点时,发现节点[" + path + "]不存在,尝试新建...");
                            createChangeNotifyNode(req.getApp());
                            break;
                        case BADVERSION:
                            break;
                        default:
                            logger.info("appJobChangeNotifyUpdateDataCallback resp warn > path:" + path + ",code:" + code);
                    }
                }

            };

    private void doUpdate(String path, JobChangeNotifyChangeReq req, Stat stat) {
        req.setStat(stat);
        zooKeeperComponent.getZooKeeper()
                .setData(path, req.getData(), stat.getVersion(), appJobChangeNotifyUpdateStatCallback, req);
    }

    private AsyncCallback.StatCallback appJobChangeNotifyUpdateStatCallback = new AsyncCallback.StatCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            KeeperException.Code code = KeeperException.Code.get(rc);
            JobChangeNotifyChangeReq req = (JobChangeNotifyChangeReq) ctx;
            switch (code) {
                case SESSIONEXPIRED:
                case CONNECTIONLOSS:
                    doUpdate(path, req, req.getStat());
                    break;
                case BADVERSION:
                    logger.info("[DTSS]ZooKeeper修改通知节点[" + path + "]时,发现版本过期,已经被其他线程修改过,操作取消");
                    break;
                case OK:
                    logger.info("[DTSS]ZooKeeper通知节点[" + path + "]更新成功,当前版本[" + stat.getVersion() + "]");
                    break;
                case NONODE:
                    logger.info("[DTSS]ZooKeeper更新通知节点时,发现节点[" + path + "]不存在,尝试新建...");
                    createChangeNotifyNode(req.getApp());
                    break;
                default:
            }
        }
    };

    //～～～～～～～～～～～～～～～～～～～other～～～～～～～～～～～～～～～～～～～
    private String getJobChangeNotifyNodePath(String app) {
        return CLIENT_ROOT + I + app + JOBS_CHANGE_NODE;
    }
}
