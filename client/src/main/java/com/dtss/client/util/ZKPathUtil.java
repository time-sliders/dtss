package com.dtss.client.util;

import com.dtss.client.consts.ZookeeperPathConst;
import com.dtss.client.core.zk.consts.ZooKeeperConst;
import com.dtss.commons.IPUtils;

/**
 * 客户端节点工具类
 *
 * @author LuYun
 * @since 2018.04.10
 */
public class ZKPathUtil implements ZookeeperPathConst, ZooKeeperConst {

    /**
     * 获取接入客户端系统跟节点
     */
    public static String getClientAppNodePath(String app) {
        return CLIENT_ROOT + I + app;
    }

    /**
     * 获取接入客户端系统的任务分配节点
     */
    public static String getClientTaskAssignNodePath(String app) {
        return getClientAppNodePath(app) + I + ASSIGN_NODE_NAME;
    }

    /**
     * 获取接入客户端系统的任务分配节点
     */
    public static String getLocalClientTaskAssignNodePath(String app) {
        return getClientTaskAssignNodePath(app) + I + IPUtils.getLocalIp();
    }

    /**
     * 获取接入客户端系统的注册节点
     */
    public static String getClientRegistryNodePath(String app) {
        return getClientAppNodePath(app) + I + CLIENTS_NODE_NAME + I + IPUtils.getLocalIp();
    }

}
