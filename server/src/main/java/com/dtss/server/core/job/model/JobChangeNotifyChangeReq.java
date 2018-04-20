package com.dtss.server.core.job.model;

import org.apache.zookeeper.data.Stat;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.05
 */
public class JobChangeNotifyChangeReq {

    /**
     * 应用
     */
    private String app;

    /**
     * 更新到的数据
     */
    private byte[] data;

    /**
     * zk状态
     */
    private Stat stat;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Stat getStat() {
        return stat;
    }

    public void setStat(Stat stat) {
        this.stat = stat;
    }
}
