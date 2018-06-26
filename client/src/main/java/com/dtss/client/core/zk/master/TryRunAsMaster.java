package com.dtss.client.core.zk.master;

import com.dtss.commons.Millisecond;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * 客户端尝试通过建立节点的方式，成为MainServer
 *
 * @author luyun
 * @version 1.0
 * @since 2018.01.14
 */
public class TryRunAsMaster {

    private static final Logger logger = LoggerFactory.getLogger(TryRunAsMaster.class);

    /**
     * * 客户端尝试通过建立节点的方式，成为Master
     *
     * @param zooKeeper zooKeeper客户端链接
     * @param path      需要创建的路径，如/dtss/master。
     * @param pathData  需要存储的数据，一般为当前Server的唯一标识，如IP等。
     * @return true if success
     * @see AsyncTryRunAsMaster
     */
    @Deprecated
    public static boolean tryMaster(ZooKeeper zooKeeper, String path, String pathData)
            throws UnsupportedEncodingException, InterruptedException, KeeperException {

        // 当前主机是否当选为Leader
        boolean result = false;
        // 是否已经有主机当选为Leader了，本机或者其他机器都可以
        boolean hasLeader = false;
        byte[] pathDataBytes = null;
        if (StringUtils.isNotBlank(pathData)) {
            pathDataBytes = pathData.getBytes("UTF-8");
        }

        while (true) {
            try {
                zooKeeper.create(path, pathDataBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            } catch (KeeperException.ConnectionLossException ignore) {
                /*
                 * 1.ConnectionLossException 异常发生于客户端与服务器失去连接时，一般常由于网络原因导致，如网络分区或服务器故障。
                 * 当这个异常发生时，客户端并不知道服务器是在处理前丢失了请求信息，还是在处理后客户端未收到响应消息，
                 * zooKeeper的会内部进行尝试重连操作，这里需要反查服务器数据来确定是否需要重新提交
                 */
            } catch (KeeperException.NodeExistsException ignore) {
                /*
                 * 2.NodeExistsException 节点已经存在，这里同样无需处理
                 */
            } catch (KeeperException.NoNodeException e) {
                /*
                 * 节点所在父节点不存在
                 */
                logger.info("请先创建父节点:" + path);
                throw e;
            }

            /*
             * 反查结果
             */
            while (true) {
                try {
                    Stat stat = new Stat();
                    byte[] zooKeeperDataBytes = zooKeeper.getData(path, false, stat);
                    String zooKeeperData = new String(zooKeeperDataBytes, "UTF-8");
                    hasLeader = StringUtils.isNotBlank(zooKeeperData);
                    result = zooKeeperData.equals(pathData);
                    break;
                } catch (KeeperException.NoNodeException e) {
                    // 节点创建失败，可能是之前创建节点服务器处理前丢失信息导致，直接重试
                    break;
                } catch (KeeperException.ConnectionLossException e) {
                    // 连接失败，直接重试
                    logger.info("ZooKeeper getNodeData occur KeeperException.ConnectionLossException>Thread_Sleep_ONE_SECONDS to CONTINUE.....");
                    Thread.sleep(Millisecond.ONE_SECONDS);
                }
            }

            /*
             * 结果判断
             */
            if (hasLeader) {
                break;
            }
        }


        return result;
    }

}
