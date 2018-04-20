package com.dtss.client.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 卢云(luyun)
 * @since 2018.03.22
 */
public abstract class AbstractJob {

    private ThreadLocal<Long/*logId*/> threadLocalLogId = new ThreadLocal<Long>();

    private ReentrantLock lock = new ReentrantLock();

    /**
     * 任务终止标志
     * 多线程间共享的内存通信,允许A线程中止B线程任务
     * 另一方面，这个Map也做为处理中的任务列表来使用，只有当任务运行时，该map才会有对应的logId
     */
    private static Map<Long/*logId*/, Boolean/*terminateFlag*/> terminatedFlagMap
            = new ConcurrentHashMap<Long, Boolean>();

    /**
     * 判断是否继续执行任务
     */
    protected boolean isTerminated() {
        Long logId = threadLocalLogId.get();
        if (logId == null) {
            throw new RuntimeException("threadLocal logId must not be null");
        }
        Boolean isTerminated = terminatedFlagMap.get(logId);
        return isTerminated != null && isTerminated;
    }

    public abstract String execute(String param);


    public void registry(Long logId) {
        lock.lock();
        try {
            if (logId == null) {
                throw new RuntimeException("logId must not be null");
            }
            threadLocalLogId.set(logId);
            terminatedFlagMap.put(logId, false);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 中断任务
     * 支持传餐的方式中断一个logId
     */
    protected boolean terminate(Long logId) {
        lock.lock();
        try {
            if (logId == null && (logId = threadLocalLogId.get()) == null) {
                return false;
            }

            if (terminatedFlagMap.containsKey(logId)) {
                //只有有数据的时候才设置，避免意外导致内存无法释放
                terminatedFlagMap.put(logId, true);
                return true;
            }

            return false;
        } finally {
            lock.unlock();
        }
    }

    public void finish() {
        lock.lock();
        try {
            Long logId = threadLocalLogId.get();
            if (logId == null) {
                throw new RuntimeException("finish logId must not be null");
            }
            terminatedFlagMap.remove(logId);
            threadLocalLogId.remove();
        } finally {
            lock.unlock();
        }
    }
}
