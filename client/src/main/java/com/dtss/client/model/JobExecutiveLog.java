package com.dtss.client.model;

import com.dtss.client.enums.JobExeStatusEnum;

import java.io.Serializable;
import java.util.Date;

public class JobExecutiveLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

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
     * JOB名称
     */
    private String name;

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
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date finishTime;

    /**
     * job执行参数
     */
    private String param;

    /**
     * 任务执行状态
     *
     * @see JobExeStatusEnum
     */
    private Integer status;

    /**
     * 内部异常信息 任务调度框架内部处理信息
     */
    private String innerMsg;

    /**
     * job执行结果
     */
    private String jobExeResult;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date modifyTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getInnerMsg() {
        return innerMsg;
    }

    public void setInnerMsg(String innerMsg) {
        this.innerMsg = innerMsg;
    }

    public String getJobExeResult() {
        return jobExeResult;
    }

    public void setJobExeResult(String jobExeResult) {
        this.jobExeResult = jobExeResult;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }
}