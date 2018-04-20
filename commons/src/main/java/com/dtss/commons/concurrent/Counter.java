package com.dtss.commons.concurrent;

/**
 * 计数器
 * <p>
 * 子线程成多线程操作该对象
 * </p>
 * thread-safe
 */
public class Counter {

    private int success = 0;
    private int fail = 0;
    private int all = 0;

    public synchronized void successPlus() {
        success++;
    }

    public int getSuccess() {
        return success;
    }

    public synchronized void failPlus() {
        fail++;
    }

    public int getFail() {
        return fail;
    }

    public int getAll() {
        return all;
    }

    public synchronized void allPlus() {
        all++;
    }

    public boolean isFinish() {
        synchronized (this) {
            return (success + fail) >= all;
        }
    }

    @Override
    public String toString() {
        return "Counter [success=" + success + ", fail=" + fail + ", all="
                + all + "]";
    }

    public void reset() {
        success = 0;
        fail = 0;
        all = 0;
    }
}
