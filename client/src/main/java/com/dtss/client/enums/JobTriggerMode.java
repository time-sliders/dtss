package com.dtss.client.enums;

/**
 * JOB触发模式
 *
 * @author 卢云(luyun)
 * @since 2018.01.29
 */
public enum JobTriggerMode {

    /**
     * 自动触发模式
     * 需要配置Corn表达式，服务器会根据配置的时间，自动触发JOB
     */
    AUTOMATIC(1, "自动触发"),

    /**
     * 被动触发
     * 无需配置Corn
     * 一般由人为手动触发
     * 或者在JOB依赖的情况下，由上一个JOB执行完毕之后触发
     */
    PASSIVE(2, "被动执行");

    private Integer code;

    private String name;

    JobTriggerMode(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(Integer triggerMode) {

        if (triggerMode == null) {
            return null;
        }

        for (JobTriggerMode mode : values()) {
            if (mode.code.equals(triggerMode)) {
                return mode.name;
            }
        }

        return null;
    }
}
