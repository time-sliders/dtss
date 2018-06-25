package com.dtss.web.vo;

/**
 * 任务列表页面请求对象
 *
 * @author luyun
 * @since 2018.01.25 23:17
 */
public class JobListAO {

    private String app;

    private String name;

    private String jobBeanName;

    private Integer triggerMode;

    private Integer type;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
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

    public Integer getTriggerMode() {
        return triggerMode;
    }

    public void setTriggerMode(Integer triggerMode) {
        this.triggerMode = triggerMode;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

}
