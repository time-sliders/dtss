package com.dtss.client.consts;

/**
 * 系统路径常量定义
 *
 * @author luyun
 * @since 2018.01.27 11:52
 */
public interface ZookeeperPathConst {

    String SYSTEM_ROOT = "/dtss";

    String SERVER_ROOT = "/dtss/server";
    String SERVER_ELECTION = "/dtss/server/election";
    String SERVER_SERVERS = "/dtss/server/servers";

    String CLIENT_ROOT = "/dtss/client";
    String CLIENT_SYSTEM_ROOT = "/dtss/client";

    String ELECTION_NODE = "/election";
    String MASTER_NODE_NAME = "/master";
    String CLIENTS_NODE_NAME = "/clients";
    String TASKS_NODE_NAME = "/tasks";
    String ASSIGN_NODE_NAME = "/assign";
    String JOBS_NODE_NAME = "/jobs";
    String JOBS_CHANGE_NODE = "/job_change_notify";

    String I = "/";

}
