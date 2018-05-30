package com.dtss.server.core.zk;

import com.dtss.client.cmp.SpringApplicationContextAware;
import com.dtss.client.consts.ZookeeperPathConst;
import com.dtss.client.core.zk.master.AsyncTryRunAsMaster;
import com.dtss.commons.IPUtils;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author luyun
 * @since 2018.01.20 13:01
 */
public class ZookeeperLeaderLatch extends AsyncTryRunAsMaster<String> implements ZookeeperPathConst {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperLeaderLatch.class);

    private static AtomicBoolean hasLeaderShip = new AtomicBoolean(false);

    public static final String LOCAL_IP = IPUtils.getLocalIp();

    private static ZookeeperLeaderLatch instance;

    public static ZookeeperLeaderLatch getInstance() {
        if (instance == null) {
            initialInstance();
        }
        return instance;
    }

    private ZookeeperLeaderLatch(String masterPath) {
        super(masterPath, LOCAL_IP, null);
    }

    private static synchronized void initialInstance() {
        if (instance != null) {
            return;
        }
        String masterPath = SERVER_ELECTION + MASTER_NODE_NAME;
        instance = new ZookeeperLeaderLatch(masterPath);
    }

    @Override
    protected ZooKeeper getZookeeper() {
        return SpringApplicationContextAware.getZkCmp().getZooKeeper();
    }

    public static boolean hasLeaderShip() {
        return hasLeaderShip.get();
    }

    @Override
    protected void masterCallback() {
        if (hasLeaderShip.compareAndSet(false, true)) {
            logger.info("DTSS>[" + LOCAL_IP + "]成功当选为Leader");
        } else {
            logger.info("DTSS>[" + LOCAL_IP + "]确认当选");
        }
    }

    @Override
    protected void loseCallBack() {
        if (hasLeaderShip.compareAndSet(true, false)) {
            logger.info("DTSS>[" + LOCAL_IP + "]失去了Leader权限");
        }
    }
}
