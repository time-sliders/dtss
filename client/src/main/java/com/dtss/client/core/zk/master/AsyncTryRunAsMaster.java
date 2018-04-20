package com.dtss.client.core.zk.master;

import com.dtss.client.core.zk.watcher.AbstractPathWatcher;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * 客户端尝试通过建立节点的方式，成为Master
 *
 * @param <C> 应用上下文Context
 * @author luyun
 * @version 1.0
 * @since 2018.01.14
 */
public abstract class AsyncTryRunAsMaster<C> {

    private static final Logger logger = LoggerFactory.getLogger(AsyncTryRunAsMaster.class);

    /**
     * 需要创建的路径，如/DTSS/master。
     */
    protected String masterPath;

    /**
     * 需要存储的数据，一般为当前Server的唯一标识，如IP等。
     */
    protected String masterPathData;

    /**
     * 应用上下文
     */
    protected C ctx;

    public AsyncTryRunAsMaster(String masterPath, String masterPathData, C ctx) {
        this.masterPath = masterPath;
        this.masterPathData = masterPathData;
        this.ctx = ctx;
    }

    protected abstract ZooKeeper getZookeeper();

    /**
     * 客户端尝试通过建立节点的方式，成为Master
     */
    public void tryMaster() {
        byte[] pathDataBytes = null;
        if (StringUtils.isNotBlank(masterPathData)) {
            try {
                pathDataBytes = masterPathData.getBytes("UTF-8");
            } catch (UnsupportedEncodingException ignore) {
            }
        }
        /*
         * 尝试当选为Master
         */
        getZookeeper().create(masterPath, pathDataBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL, createMasterCallback, ctx);
    }

    private AsyncCallback.StringCallback createMasterCallback = new AsyncCallback.StringCallback() {

        @Override
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)) {
                case OK:
                case NODEEXISTS:
                case CONNECTIONLOSS:
                    /*
                     * 情况2：结果未知，反查确认
                     */
                    checkMaster();
                    break;

                default:
                    /*
                     * 情况3：当选失败
                     */
            }
        }
    };


    private void checkMaster() {
        getZookeeper().getData(masterPath, false, dataCallBack, ctx);
    }

    private AsyncCallback.DataCallback dataCallBack = new AsyncCallback.DataCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            switch (KeeperException.Code.get(rc)) {
                case OK:
                    checkAndHandleMaster(data);
                    break;

                case CONNECTIONLOSS:
                    checkMaster();
                    break;

                case NONODE:
                    tryMaster();
                    break;

                default:
            }
        }
    };

    /**
     * 验证Master节点数据是否是当前节点
     * 是：调用当选成功的回调
     * 否：监控当前节点，一旦主控服务挂掉，立即尝试接管控制权
     *
     * @param data 获取到的Master节点数据
     */
    private void checkAndHandleMaster(byte[] data) {
        if (data == null) {
            // 异常情况，主节点没有数据
            throw new RuntimeException("DTSS>[" + masterPath + "]:no data!");
        }
        String masterDataStr;
        try {
            masterDataStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("DTSS>[" + masterPath + "] decode fail!");
        }

        if (masterPathData.equals(masterDataStr)) {
            /*
             * 1.当选成功，群首回调
             */
            masterCallback();

        } else {

            /*
             * 2.落选了
             */
            loseCallBack();
        }

        /*
         * 2.监控Master节点
         * 无论当前节点是不是
         * 一旦主控服务挂掉，则立即尝试接管
         */
        try {
            if (getZookeeper().exists(masterPath, new TryMasterWatcher(masterPath)) == null) {
                throw new RuntimeException("DTSS>[" + masterPath + "] not exists");
            }
        } catch (KeeperException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    class TryMasterWatcher extends AbstractPathWatcher {

        TryMasterWatcher(String path) {
            super(path);
        }

        @Override
        public void process(WatchedEvent event) {

            if (event.getState().equals(Event.KeeperState.Expired)) {
                loseCallBack();
            }

            switch (event.getType()) {
                case NodeCreated:
                case NodeDataChanged:
                case None:
                    /*
                     * None:may be SessionExpired or DisConnected or SyncConnected
                     * vying for leadership roles
                     */
                    checkMaster();
                    break;

                default:
                    logger.info("尝试竞选Leader" + event.getType() + " " + event.getState() + " " + event.getState());
                    tryMaster();
            }
        }
    }

    /**
     * 群首回调
     */
    protected abstract void masterCallback();

    /**
     * 落选回调
     */
    protected abstract void loseCallBack();

}
