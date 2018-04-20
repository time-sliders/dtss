package com.dtss.client.enums;

/**
 * 任务执行状态
 *
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.22
 */
public enum JobExeStatusEnum {

    PROCESSING(0, "处理中", "orange"),

    SUCCESS(1, "执行成功", "green"),

    FAIL(-1, "处理失败", "red");

    private int code;

    private String name;

    private String color;

    JobExeStatusEnum(int code, String name, String color) {
        this.code = code;
        this.name = name;
        this.color = color;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public static JobExeStatusEnum getByCode(Integer status) {

        if (status == null) {
            return null;
        }

        for (JobExeStatusEnum statusEnum : values()) {
            if (statusEnum.getCode() == status) {
                return statusEnum;
            }
        }

        return null;
    }
}

