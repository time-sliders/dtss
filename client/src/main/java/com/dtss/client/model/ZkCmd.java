package com.dtss.client.model;

import com.dtss.client.enums.JobTypeEnum;
import com.dtss.client.enums.OptType;

/**
 * @author luyun
 * @since 2018.01.27 13:21
 */
public class ZkCmd {

    /**
     * 操作类型
     *
     * @see OptType
     */
    private Integer optType;

    /**
     * 任务ID
     */
    private Long jobId;

    /**
     * 日志Id
     */
    private Long logId;

    /**
     * 任务类型
     *
     * @see JobTypeEnum
     */
    private Integer jobType;

    /**
     * 当前Job触发时间
     * 数据格式：yyyyMMddHHmmss
     */
    private String jobScheduledTime;

    /**
     * JobBeanName
     */
    private String jobBeanName;

    /**
     * 执行参数
     */
    private String param;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Integer getOptType() {
        return optType;
    }

    public void setOptType(Integer optType) {
        this.optType = optType;
    }

    public String getJobScheduledTime() {
        return jobScheduledTime;
    }

    public void setJobScheduledTime(String jobScheduledTime) {
        this.jobScheduledTime = jobScheduledTime;
    }

    public Integer getJobType() {
        return jobType;
    }

    public void setJobType(Integer jobType) {
        this.jobType = jobType;
    }

    public String getJobBeanName() {
        return jobBeanName;
    }

    public void setJobBeanName(String jobBeanName) {
        this.jobBeanName = jobBeanName;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }
}
