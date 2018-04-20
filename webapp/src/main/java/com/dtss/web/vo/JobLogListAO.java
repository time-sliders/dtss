package com.dtss.web.vo;

/**
 * 任务列表页面请求对象
 *
 * @author luyun
 * @since 2018.01.25 23:17
 */
public class JobLogListAO {

    private String app;

    private String name;

    private String jobBeanName;

    /**
     * id < ltId
     */
    private Long ltId;

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

    public Long getLtId() {
        return ltId;
    }

    public void setLtId(Long ltId) {
        this.ltId = ltId;
    }
}
