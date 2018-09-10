package com.dtss.server.core.job.sender.util;

import com.dtss.client.consts.CmdConst;
import com.dtss.client.consts.ZookeeperPathConst;

/**
 * @author LuYun
 * @since 2018.04.16
 */
public class JobPathUtil implements ZookeeperPathConst, CmdConst {

    private static final String LINE = "_";

    public static String buildTaskAssignNode(String app, Long jobId, String scheduleTime, String clientIp) {
        String taskKey = EXE_CMD_PREFIX + scheduleTime + LINE + jobId;
        return getTaskAssignPath(app, clientIp) + I + taskKey;
    }

    public static String getTaskAssignPath(String app, String clientIp) {
        return CLIENT_ROOT + I + app + ASSIGN_NODE_NAME + I + clientIp;
    }
}
