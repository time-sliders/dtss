package com.dtss.client.model;

import com.dtss.client.enums.JobTriggerMode;
import com.dtss.client.enums.JobTypeEnum;

import java.io.Serializable;
import java.util.Date;

/**
 * JOB配置
 *
 * @author luyun
 * @since 2017.11.15
 */
public class JobConfig implements Serializable {

    private static final long serialVersionUID = 1L;

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
    private Integer type;

    /**
     * 触发模式
     *
     * @see JobTriggerMode
     */
    private Integer triggerMode;

    /**
     * JOB执行的corn表达式
     */
    private String cron;

    /**
     * JOB 的 springBeanName
     */
    private String jobBeanName;

    /**
     * 客户端IP
     * 特殊情况下指定客户端IP执行
     * 如 单点优先,绝对单点等情况
     */
    private String clientIp;

    /**
     * JOB是否激活
     */
    private Boolean isActivity;

    /**
     * JOB描述
     */
    private String description;

    /**
     * JOB执行参数
     */
    private String param;

    /**
     * 开发人员手机
     */
    private String ownerPhone;

    /**
     * 版本号
     * System.currentTimeMillis()
     */
    private Long version;

    /**
     * 最后一次的更新时间
     * 检测程序会定时查询10分钟以内更新过的任务
     * 与ZK进行状态对比
     */
    private Long lastModifyTimeLong;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 结束时间
     */
    private Date modifyTime;

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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getTriggerMode() {
        return triggerMode;
    }

    public void setTriggerMode(Integer triggerMode) {
        this.triggerMode = triggerMode;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getJobBeanName() {
        return jobBeanName;
    }

    public void setJobBeanName(String jobBeanName) {
        this.jobBeanName = jobBeanName;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Boolean isActivity() {
        return isActivity;
    }

    public void setActivity(Boolean isActivity) {
        this.isActivity = isActivity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getLastModifyTimeLong() {
        return lastModifyTimeLong;
    }

    public void setLastModifyTimeLong(Long lastModifyTimeLong) {
        this.lastModifyTimeLong = lastModifyTimeLong;
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
