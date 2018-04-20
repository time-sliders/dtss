package com.dtss.client.core.handle;

import com.dtss.client.model.ZkCmd;

/**
 * @author LuYun
 * @since 2018.04.16
 */
public interface JobHandler {

    /**
     * 处理指令
     *
     * @param cmd  命令
     */
    void handle(ZkCmd cmd);

}
