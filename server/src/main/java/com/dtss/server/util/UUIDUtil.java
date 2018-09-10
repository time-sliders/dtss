package com.dtss.server.util;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.02
 */
public class UUIDUtil {

    private static final String MIDDLE_LINE = "-";
    private static final String BLANK = "";
    private static final String DEFAULT_CHARSET = "utf-8";

    public static String getNew() {
        return UUID.randomUUID().toString().replaceAll(MIDDLE_LINE, BLANK);
    }

    public static byte[] getNewUUIDByteArr() {
        String uuid = getNew();
        try {
            return uuid.getBytes(DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
