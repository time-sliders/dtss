package com.dtss.commons;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static final String FMT_MILL_SEC = "yyyy-MM-dd HH:mm:ss:SSS";
    public static final String FMR_MILL_NO_LINE = "yyyyMMddHHmmssSSS";
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String NO_SECOND_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String yyyy_MM_dd = "yyyy-MM-dd";
    public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
    public static final String yyyy_M_d = "yyyy-M-d";
    public static final String yy_MM_dd = "yy-MM-dd";
    public static final String MM_dd = "MM-dd";
    public static final String CHINESE_DATE_FORMAT = "yyyy年MM月dd日";
    public static final String SHORT_CHINESE_DATE_FORMAT = "M月d日";
    public static final String yyyyMMdd = "yyyyMMdd";
    public static final String yyyyMd_point = "yyyy.M.d";
    public static final String yyyyMMdd_slash = "yyyy/MM/dd";
    public static final String yyyyMd_slash = "yyyy/M/d";
    public static final String hhMMss = "hhMMss";

    public static String format(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 获取一个时间的intValue->yyyyMMdd
     */
    public Integer asInt(Date date) {
        return Integer.valueOf(format(date, yyyyMMdd));
    }

    /**
     * 将一个8位日期转换为Date
     */
    public static Date asDate(Integer date) {
        try {
            String dateString = date.toString();
            if (dateString.length() != 8) {
                throw new IllegalArgumentException("date:" + date + " is invalid!");
            }
            return new SimpleDateFormat(yyyyMMdd).parse(date.toString());
        } catch (ParseException ignore) {
            return null;
        }
    }

    public static Date addDays(Date date, int add) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, add);
        return calendar.getTime();
    }

    public static int getHour(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static Date parseDate(String str, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setLenient(false);
        try {
            return dateFormat.parse(str);
        } catch (ParseException ex) {
            return null;
        }
    }

    public static long getDiffDays(Date before, Date after) {

        Calendar c1 = Calendar.getInstance();
        c1.setTime(before);
        ignoreTime(c1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(after);
        ignoreTime(c2);

        return (c2.getTimeInMillis() - c1.getTimeInMillis())
                / Millisecond.ONE_DAY;
    }

    /**
     * 将一个日期的时分秒设置为0
     *
     * @param calendar 指定的时间
     */
    public static void ignoreTime(Calendar calendar) {

        if (calendar == null) {
            return;
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * 将一个日期的时分秒设置为0
     *
     * @param date 指定的时间
     */
    public static void ignoreTime(Date date) {

        if (date == null) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        ignoreTime(calendar);
    }

}
