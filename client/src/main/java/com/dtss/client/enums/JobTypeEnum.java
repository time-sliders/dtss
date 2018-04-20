package com.dtss.client.enums;

/**
 * JOB类型
 *
 * @author luyun
 * @since 2017.11.15
 */
public enum JobTypeEnum {

    /**
     * 普通分布式任务
     * 根据指定corn表达式
     * 在分布式客户端，自动定时执行的任务
     */
    COMMON(1, "分布式任务"),

    /**
     * 绝对单点任务
     * 只能在某一台服务器上执行的任务
     * 一旦指定服务器不可达，则任务执行失败
     */
    ABSOLUTELY_SINGLE_POINT(7, "绝对单点任务"),

    /**
     * 单点优先任务
     * 优先某一个单点执行
     * 如果指定服务器不可达，则自动将任务分派到其他可执行机器
     */
    PRIOR_SINGLE_POINT(8, "单点优先任务"),

    /**
     * 参数分片任务
     * 通过多个执行参数，分拆任务给子服务器
     * 缺点：参数配置错误会造成重复处理或漏处理
     */
    SLICE_BY_PARAM(4, "参数分片任务"),

    /**
     * 广播任务
     * 任务需要分发到所有客户端服务器执行
     * 如内存数据删除等
     */
    BROADCAST(6, "广播任务");

    /**
     * 任务依赖
     * 后续触发任务
     */

    private Integer code;

    private String name;

    JobTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (JobTypeEnum e : values()) {
            if (e.code.equals(code)) {
                return e.name;
            }
        }

        return null;
    }
}