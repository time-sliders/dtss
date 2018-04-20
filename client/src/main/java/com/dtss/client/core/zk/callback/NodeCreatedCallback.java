package com.dtss.client.core.zk.callback;

import com.dtss.client.core.zk.model.ZooKeeperPathNode;

/**
 * ZooKeeper Node节点创建成功之后的回调函数
 *
 * @author luyun
 * @since 2018.01.20
 */
public abstract class NodeCreatedCallback {

    protected ZooKeeperPathNode currentNode;

    public NodeCreatedCallback() {
    }

    /**
     * 节点创建成功回调
     */
    public abstract void process();

    public void setCurrentNode(ZooKeeperPathNode currentNode) {
        this.currentNode = currentNode;
    }


    public ZooKeeperPathNode getCurrentNode() {
        return currentNode;
    }


}
