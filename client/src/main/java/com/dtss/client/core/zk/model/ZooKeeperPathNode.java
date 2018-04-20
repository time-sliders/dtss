package com.dtss.client.core.zk.model;

import com.dtss.client.core.zk.callback.NodeCreatedCallback;
import org.apache.commons.collections.CollectionUtils;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luyun
 * @since 2018.01.20
 */
public class ZooKeeperPathNode {

    /**
     * 当前节点完整路径
     */
    private String currentPath;

    /**
     * 当前节点数据
     */
    private byte[] currentData;

    /**
     * 节点建立成功之后的回调函数 节点已经存在的时候，也会调用
     */
    private NodeCreatedCallback callback;

    /**
     * 真实的建立成功之后的回调，存在create动作
     * 如果已经存在，则不会调用
     */
    private NodeCreatedCallback okCallback;

    /**
     * 创建模式
     */
    private CreateMode createMode;

    /**
     * 子节点
     */
    private List<ZooKeeperPathNode> nextPathNodeList = null;

    public ZooKeeperPathNode(String currentPath, byte[] currentData, CreateMode createMode) {
        this.currentPath = currentPath;
        this.currentData = currentData;
        this.createMode = createMode;
    }

    public ZooKeeperPathNode(String currentPath, byte[] currentData, CreateMode createMode, NodeCreatedCallback callback) {
        this.currentPath = currentPath;
        this.currentData = currentData;
        this.createMode = createMode;
        this.callback = callback;
    }

    public ZooKeeperPathNode addNextPath(String path, byte[] data, CreateMode createMode) {
        if (nextPathNodeList == null) {
            nextPathNodeList = new ArrayList<ZooKeeperPathNode>();
        }
        ZooKeeperPathNode nextPathNode = new ZooKeeperPathNode(path, data, createMode);
        nextPathNodeList.add(nextPathNode);
        return nextPathNode;
    }

    public ZooKeeperPathNode addNextPath(String path, byte[] data, CreateMode createMode, NodeCreatedCallback callback) {
        ZooKeeperPathNode nextPathNode = addNextPath(path, data, createMode);
        callback.setCurrentNode(nextPathNode);
        nextPathNode.callback = callback;
        return nextPathNode;
    }

    public ZooKeeperPathNode addNextPath(String path, byte[] data, CreateMode createMode,
                                         NodeCreatedCallback callback, NodeCreatedCallback okCallback) {
        ZooKeeperPathNode nextPathNode = addNextPath(path, data, createMode);
        if (callback != null) {
            callback.setCurrentNode(nextPathNode);
        }
        if (okCallback != null) {
            okCallback.setCurrentNode(nextPathNode);
        }
        nextPathNode.callback = callback;
        nextPathNode.okCallback = okCallback;
        return nextPathNode;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public boolean hasNext() {
        return CollectionUtils.isNotEmpty(nextPathNodeList);
    }

    public List<ZooKeeperPathNode> getNextPathNodeList() {
        return nextPathNodeList;
    }

    public byte[] getCurrentData() {
        return currentData;
    }

    public void okOrExistsCallback() {
        if (callback != null) callback.process();
    }

    public void okCallback() {
        if (okCallback != null) okCallback.process();
    }

    public CreateMode getCreateMode() {
        return createMode;
    }
}
