package com.dtss.client.model.query;

import com.dtss.client.enums.JobTriggerMode;
import com.dtss.client.enums.JobTypeEnum;
import com.dtss.commons.Query;

import java.io.Serializable;

public class JobConfigQuery extends Query implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * id > gtId;
     */
    private Long gtId;

    /**
     * JOB名称
     */
    private String name;

    /**
     * JOB所属应用
     */
    private String app;

    /**
     * 任务类型
     *
     * @see JobTypeEnum
     */
    private Integer type;

    /**
     * 触发模式
     *
     * @see JobTriggerMode
     */
    private Integer triggerMode;

    /**
     * JOB 的 springBeanName
     */
    private String jobBeanName;

    /**
     * JOB是否激活
     */
    private Boolean isActivity;

    private Long version;

    /**
     * lastModifyTimeLong >= egtLastModifyTimeLong
     */
    private Long egtLastModifyTimeLong;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGtId() {
        return gtId;
    }

    public void setGtId(Long gtId) {
        this.gtId = gtId;
    }

    public Boolean isActivity() {
        return isActivity;
    }

    public void setActivity(Boolean isActivity) {
        this.isActivity = isActivity;
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getEgtLastModifyTimeLong() {
        return egtLastModifyTimeLong;
    }

    public void setEgtLastModifyTimeLong(Long egtLastModifyTimeLong) {
        this.egtLastModifyTimeLong = egtLastModifyTimeLong;
    }
}