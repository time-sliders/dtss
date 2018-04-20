package com.dtss.web.util;

import com.dtss.commons.Millisecond;

import java.util.Date;

/**
 * @author LuYun
 * @since 2018.04.08
 */
public class CostTimeShowUtil {

    private static final String DAY_SUFFIX = " 天 ";
    private static final String HOUR_SUFFIX = " 小时 ";
    private static final String MINUS_SUFFIX = " 分 ";
    private static final String SECONDS_SUFFIX = " 秒";

    public static String format(Date startTime, Date endTime) {

        if (startTime == null || endTime == null || startTime.after(endTime)) {
            return null;
        }

        Long costTimeMills = endTime.getTime() - startTime.getTime();
        long day = costTimeMills / Millisecond.ONE_DAY;

        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day).append(DAY_SUFFIX);
        }

        long left = costTimeMills - Millisecond.ONE_DAY * day;
        long hour = left / Millisecond.ONE_HOUR;
        if (hour > 0) {
            sb.append(hour).append(HOUR_SUFFIX);
        }

        left = left - Millisecond.ONE_HOUR * hour;
        long minus = left / Millisecond.ONE_MINUS;
        if (minus > 0) {
            sb.append(minus).append(MINUS_SUFFIX);
        }

        left = left - Millisecond.ONE_MINUS * minus;
        sb.append(left / Millisecond.ONE_SECONDS).append(SECONDS_SUFFIX);
        return sb.toString();
    }

}
