package com.dtss.web.vo;


import com.dtss.client.enums.JobTriggerMode;
import com.dtss.client.enums.JobTypeEnum;

/**
 * job 列表页面VO
 *
 * @author luyun
 * @since 2018.01.23 23:36
 */
public class JobConfigVO {

    private String id;

    /**
     * JOB名称
     */
    private String name;

    /**
     * 所属应用
     */
    private String app;

    /**
     * 任务类型
     *
     * @see JobTypeEnum
     */
    private String type;

    /**
     * 触发模式
     *
     * @see JobTriggerMode
     */
    private String triggerMode;

    /**
     * JOB执行的corn表达式 或者执行时间
     */
    private String corn;

    /**
     * JOB 的 springBeanName
     */
    private String jobBeanName;

    /**
     * JOB是否激活
     */
    private Boolean activity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTriggerMode() {
        return triggerMode;
    }

    public void setTriggerMode(String triggerMode) {
        this.triggerMode = triggerMode;
    }

    public String getCorn() {
        return corn;
    }

    public void setCorn(String corn) {
        this.corn = corn;
    }

    public String getJobBeanName() {
        return jobBeanName;
    }

    public void setJobBeanName(String jobBeanName) {
        this.jobBeanName = jobBeanName;
    }

    public Boolean getActivity() {
        return activity;
    }

    public void setActivity(Boolean activity) {
        this.activity = activity;
    }
}
