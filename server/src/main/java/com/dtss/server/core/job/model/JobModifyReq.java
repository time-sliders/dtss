package com.dtss.server.core.job.model;

import com.dtss.client.model.JobConfig;

/**
 * @author luyun
 * @since 2018.02.25 15:16
 */
public class JobModifyReq {

    /**
     * 新的任务版本号
     */
    private Long newVersion;

    /**
     * 更新后的JOB
     */
    private JobConfig newJobConfig;

    public JobConfig getNewJobConfig() {
        return newJobConfig;
    }

    public void setNewJobConfig(JobConfig newJobConfig) {
        this.newJobConfig = newJobConfig;
    }

    public Long getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(Long newVersion) {
        this.newVersion = newVersion;
    }
}
