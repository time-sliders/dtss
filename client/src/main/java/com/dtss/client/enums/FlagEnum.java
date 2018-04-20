package com.dtss.client.enums;

/**
 * 通用的Flag字段
 *
 * @author luyun
 * @since 2018.01.20 19:58
 */
public enum FlagEnum {

    YES(1),

    NO(0);

    int code;

    FlagEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
