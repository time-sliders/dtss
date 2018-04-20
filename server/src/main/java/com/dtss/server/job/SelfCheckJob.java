package com.dtss.server.job;

import com.dtss.commons.Millisecond;
import com.dtss.commons.OrderBy;
import com.dtss.commons.RedisComponent;
import com.dtss.commons.concurrent.AbstractTaskProvider;
import com.dtss.commons.concurrent.Counter;
import com.dtss.commons.concurrent.DefaultResultConsumer;
import com.dtss.commons.concurrent.EnhanceCompletionService;
import com.dtss.server.service.JobModifyService;
import com.dtss.server.service.JobQueryService;
import com.dtss.client.core.AbstractJob;
import com.dtss.client.model.JobConfig;
import com.dtss.client.model.query.JobConfigQuery;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;

/**
 * Dtss自检JOB
 * 定时检测最近更新过的任务，确保DB与Zookeeper的数据一致
 *
 * @author LuYun
 * @since 2018.03.30
 */
@Component
public class SelfCheckJob extends AbstractJob implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SelfCheckJob.class);

    @Autowired
    private JobQueryService jobQueryService;

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private JobModifyService jobModifyService;

    private static final String REDIS_LOCK_KEY = "dtss.SelfCheckJob";

    private ThreadPoolExecutor tpe;

    @Override
    public String execute(String param) {
        logger.info("start dtss.SelfCheckJob, param :" + param);
        String result = null;
        boolean isLocked = false;
        try {
            if (!(isLocked = redisComponent.acquireLock(REDIS_LOCK_KEY, Millisecond.TEN_MINUS))) {
                result = "请求并发";

            } else if (StringUtils.isNotBlank(param)) {
                result = "暂不支持参数模式";

            } else {
                result = correctLastTenMinJob();
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            result = "处理异常:" + e.getMessage();
        } finally {
            if (isLocked) {
                redisComponent.releaseLock(REDIS_LOCK_KEY);
            }
            logger.info("end dtss.SelfCheckJob, param :" + param + ", result :" + result);
        }

        return result;
    }

    private String correctLastTenMinJob() {
        Counter counter = new Counter();
        EnhanceCompletionService<Boolean, Counter> ecs =
                new EnhanceCompletionService<Boolean, Counter>(tpe, new DefaultResultConsumer(counter));
        ecs.execute(new SelfCheckTaskProvider(ecs));
        return counter.toString();
    }

    private class SelfCheckTaskProvider extends AbstractTaskProvider<Boolean, Counter> {
        SelfCheckTaskProvider(EnhanceCompletionService<Boolean, Counter> ecs) {
            super(ecs);
        }

        @Override
        public void offerTasks() {

            JobConfigQuery query = new JobConfigQuery();
            query.setOrderBy(new OrderBy("last_modify_time_long", OrderBy.ASC));
            Long tenMinBefore = System.currentTimeMillis() - Millisecond.ONE_HOUR;
            query.setEgtLastModifyTimeLong(tenMinBefore);
            query.setOffset(0);
            int limit = 100;
            query.setLimit(limit);

            while (true) {

                List<JobConfig> jobConfigList = jobQueryService.query(query);
                if (CollectionUtils.isEmpty(jobConfigList)) {
                    break;
                }

                for (JobConfig jobConfig : jobConfigList) {
                    ecs.submit(new JobCheckTask(jobConfig));
                }

                if (jobConfigList.size() < limit) {
                    break;
                }
                query.setEgtLastModifyTimeLong(jobConfigList.get(jobConfigList.size() - 1).getLastModifyTimeLong());
            }
        }
    }

    private class JobCheckTask implements Callable<Boolean> {
        private JobConfig jobConfig;

        JobCheckTask(JobConfig jobConfig) {
            this.jobConfig = jobConfig;
        }

        @Override
        public Boolean call() throws Exception {
            jobModifyService.sendZkModifyReq(jobConfig);
            return true;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        tpe = new ThreadPoolExecutor(4, 4, 10, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(1000), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    tpe.getQueue().put(r);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
        tpe.allowCoreThreadTimeOut(true);
    }


}
