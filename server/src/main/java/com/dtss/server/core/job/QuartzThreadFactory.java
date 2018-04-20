package com.dtss.server.core.job;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author LuYun
 * @since 2018.04.13
 */
public class QuartzThreadFactory implements ThreadFactory {

    private static final String TASK_NAME = "任务发送线程%s";

    private AtomicLong index = new AtomicLong(0L);

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(String.format(TASK_NAME, index.getAndDecrement()));
        return t;
    }
}
