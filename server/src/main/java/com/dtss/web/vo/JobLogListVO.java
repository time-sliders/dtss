package com.dtss.web.vo;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.22
 */
public class JobLogListVO {

    private Long id;

    private Long jobId;

    private String name;

    private String app;

    private String param;

    private String scheduleTime;

    private String triggerServerIp;

    private String executeClientIp;

    private String startTime;

    private String cost;

    private Integer status;

    private String statusDesc;

    private String statusColor;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
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

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }
}
