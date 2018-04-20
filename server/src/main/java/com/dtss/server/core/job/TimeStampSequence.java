package com.dtss.server.core.job;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class TimeStampSequence {

    private static HashMap<String, Integer> tssCache = new HashMap<String, Integer>(2);

    private static final ReentrantLock lock = new ReentrantLock();

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");

    private static final int defaultStartValue = 1;

    /**
     * 15位时间戳 ＋ 4位自增
     */
    public static String getTimeStampSequence() {

        String timestamp;
        String inc;

        lock.lock();
        try {

            timestamp = sdf.format(new Date());
            Integer value = tssCache.get(timestamp);
            if (value == null) {
                tssCache.clear();
                tssCache.put(timestamp, defaultStartValue);
                inc = String.valueOf(defaultStartValue);
            } else {
                Integer newValue = value + 1;
                tssCache.put(timestamp, newValue);
                inc = String.valueOf(newValue);
            }
        } finally {
            lock.unlock();
        }

        return timestamp + StringUtils.leftPad(inc, 4, '0');
    }

}
