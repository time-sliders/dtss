package com.dtss.client.enums;

/**
 * @author LuYun
 * @since 2018.04.02
 */
public enum OptType {

    EXE(1, "执行"),

    TEST(2, "测试"),

    STOP(3, "中断");

    Integer code;

    String name;

    OptType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
