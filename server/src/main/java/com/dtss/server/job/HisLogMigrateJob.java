package com.dtss.server.job;

import com.dtss.commons.Millisecond;
import com.dtss.commons.OrderBy;
import com.dtss.commons.RedisComponent;
import com.dtss.commons.concurrent.Counter;
import com.dtss.server.service.JobExecutiveLogService;
import com.dtss.client.core.AbstractJob;
import com.dtss.client.model.JobExecutiveLog;
import com.dtss.client.model.query.JobExecutiveLogQuery;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 历史日志迁移
 *
 * @author LuYun
 * @since 2018.04.08
 */
@Component
public class HisLogMigrateJob extends AbstractJob {

    private static final Logger logger = LoggerFactory.getLogger(HisLogMigrateJob.class);

    @Autowired
    private JobExecutiveLogService jobExecutiveLogService;

    @Autowired
    private RedisComponent redisComponent;

    private static final String REDIS_LOCK_KEY = "dtss.HisLogMigrateJob";

    @Override
    public String execute(String param) {

        logger.info("start dtss.HisLogMigrateJob, param :" + param);
        String result = null;
        boolean isLocked = false;
        try {
            if (!(isLocked = redisComponent.acquireLock(REDIS_LOCK_KEY, Millisecond.TEN_MINUS))) {
                result = "请求并发";

            } else if (StringUtils.isNotBlank(param)) {
                result = "暂不支持参数模式";

            } else {
                result = migrateHisLog();
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            result = "处理异常:" + e.getMessage();
        } finally {
            if (isLocked) {
                redisComponent.releaseLock(REDIS_LOCK_KEY);
            }
            logger.info("end dtss.HisLogMigrateJob, param :" + param + ", result :" + result);
        }

        return result;
    }

    private String migrateHisLog() {

        int limit = 500;
        JobExecutiveLogQuery query = new JobExecutiveLogQuery();
        query.setLimit(limit);
        query.setOffset(0);
        Date startTime = getLastMigrateDate();

        query.setOrderBy(new OrderBy("create_time", OrderBy.ASC));
        Counter counter = new Counter();
        while (true) {

            query.setEltCreateTime(startTime);
            List<JobExecutiveLog> logList = jobExecutiveLogService.list(query);
            if (CollectionUtils.isEmpty(logList)) {
                break;
            }

            for (JobExecutiveLog log : logList) {
                counter.allPlus();
                if (jobExecutiveLogService.migrateToHis(log) > 0) {
                    counter.successPlus();
                } else {
                    counter.failPlus();
                }
            }

            if (limit > logList.size()) {
                break;
            }

            startTime = logList.get(logList.size() - 1).getCreateTime();
        }

        return counter.toString();
    }

    private Date getLastMigrateDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        return calendar.getTime();
    }
}
