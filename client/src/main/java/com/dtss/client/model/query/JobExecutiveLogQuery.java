package com.dtss.client.model.query;

import com.dtss.commons.Query;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.20
 */
public class JobExecutiveLogQuery extends Query implements Serializable{

    private static final long serialVersionUID = 3235921847985515791L;

    private Long id;

    /**
     * id < ltId
     */
    private Long ltId;

    /**
     * JOB ID
     */
    private Long jobId;

    /**
     * JOB所属应用
     */
    private String app;

    /**
     * 触发时间
     */
    private Long scheduleTime;

    /**
     * 任务分片索引
     */
    private Integer sliceIndex;

    /**
     * JOB名称
     */
    private String name;

    private Integer status;

    /**
     * JOB 的 springBeanName
     */
    private String jobBeanName;

    /**
     * 触发服务器IP
     */
    private String triggerServerIp;

    /**
     * 执行客户端IP
     */
    private String executeClientIp;

    /**
     * create_time <= eltCreateTime
     */
    private Date eltCreateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLtId() {
        return ltId;
    }

    public void setLtId(Long ltId) {
        this.ltId = ltId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Long getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(Long scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public Integer getSliceIndex() {
        return sliceIndex;
    }

    public void setSliceIndex(Integer sliceIndex) {
        this.sliceIndex = sliceIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJobBeanName() {
        return jobBeanName;
    }

    public void setJobBeanName(String jobBeanName) {
        this.jobBeanName = jobBeanName;
    }

    public String getTriggerServerIp() {
        return triggerServerIp;
    }

    public void setTriggerServerIp(String triggerServerIp) {
        this.triggerServerIp = triggerServerIp;
    }

    public String getExecuteClientIp() {
        return executeClientIp;
    }

    public void setExecuteClientIp(String executeClientIp) {
        this.executeClientIp = executeClientIp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getEltCreateTime() {
        return eltCreateTime;
    }

    public void setEltCreateTime(Date eltCreateTime) {
        this.eltCreateTime = eltCreateTime;
    }
}
