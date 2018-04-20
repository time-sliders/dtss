package com.dtss.server.core.zk;

import com.dtss.client.consts.ZookeeperPathConst;
import com.dtss.client.core.zk.callback.NodeCreatedCallback;

/**
 * 服务器端，选举节点创建成功之后的回调函数
 *
 * @author luyun
 * @since 2018.01.20 11:58
 */
public class ServerElectionNodeCreatedCallback
        extends NodeCreatedCallback implements ZookeeperPathConst {

    @Override
    public void process() {
        //一旦发现选举节点存在，则服务器尝试竞选Leader
        ZookeeperLeaderLatch.getInstance().tryMaster();
    }
}