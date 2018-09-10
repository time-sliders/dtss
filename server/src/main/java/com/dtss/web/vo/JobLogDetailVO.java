package com.dtss.web.vo;

import com.dtss.client.enums.JobExeStatusEnum;

public class JobLogDetailVO {

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
    private String startTime;

    /**
     * 结束时间
     */
    private String finishTime;

    /**
     * job执行参数
     */
    private String param;

    /**
     * 任务执行状态
     *
     * @see JobExeStatusEnum
     */
    private String status;

    private String statusColor;

    /**
     * 内部异常信息 任务调度框架内部处理信息
     */
    private String innerMsg;

    /**
     * job执行结果
     */
    private String jobExeResult;


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

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
}