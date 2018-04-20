package com.dtss.server.core.job.async;

import com.alibaba.fastjson.JSON;
import com.dtss.client.consts.ZookeeperPathConst;
import com.dtss.client.core.zk.ZooKeeperComponent;
import com.dtss.client.model.JobConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 抽象的异步job数据变动处理器
 *
 * @param <R> 处理对象
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.01
 */
public abstract class AbstractAsyncJobChangeHandler<R> implements ZookeeperPathConst {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAsyncJobChangeHandler.class);

    private static ThreadPoolExecutor tpe;

    @Autowired
    ZooKeeperComponent zooKeeperComponent;

    static {
        tpe = new ThreadPoolExecutor(4, 8, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(200),
                new ThreadFactory() {
                    private static final String THREAD_NAME_PREFIX = "[JobChangeHandleThread-%s]";
                    private AtomicLong idx = new AtomicLong(0L);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, String.format(THREAD_NAME_PREFIX, idx.incrementAndGet()));
                    }
                },
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        logger.warn("async job change handler ignored req > " + JSON.toJSONString(r));
                    }
                });
        tpe.allowCoreThreadTimeOut(false);
        logger.info("AsyncJobChangeHandler tpe init success!");
    }

    /**
     * 处理业务数据
     */
    public abstract void execute(R r);

    /**
     * 提交异步线程处理
     */
    public void submitAsyncTask(R r) {
        tpe.submit(new AsyncTask(r));
    }

    private class AsyncTask implements Runnable {
        private R r;

        AsyncTask(R r) {
            this.r = r;
        }

        @Override
        public void run() {
            try {
                execute(r);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    String getJobNodePath(JobConfig jobConfig) {
        String systemNodePath = CLIENT_ROOT + I + jobConfig.getApp();
        String jobsNodePath = systemNodePath + JOBS_NODE_NAME;
        return jobsNodePath + I + jobConfig.getId();
    }


}
