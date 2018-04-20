package com.dtss.server.job;

import com.dtss.client.core.AbstractJob;
import com.dtss.client.enums.JobExeStatusEnum;
import com.dtss.client.model.JobExecutiveLog;
import com.dtss.client.model.query.JobExecutiveLogQuery;
import com.dtss.commons.Millisecond;
import com.dtss.commons.OrderBy;
import com.dtss.commons.RedisComponent;
import com.dtss.commons.concurrent.Counter;
import com.dtss.server.service.JobExecutiveLogService;
import com.dtss.server.service.JobLogService;
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
 * 状态纠正JOB
 *
 * @author LuYun
 * @since 2018.04.08
 */
@Component
public class StatusCorrectJob extends AbstractJob {

    private static final Logger logger = LoggerFactory.getLogger(StatusCorrectJob.class);

    @Autowired
    private JobExecutiveLogService jobExecutiveLogService;

    @Autowired
    private JobLogService jobLogService;

    @Autowired
    private RedisComponent redisComponent;

    private static final String REDIS_LOCK_KEY = "dtss.StatusCorrectJob";

    private static final String JOB_MSG = "未在1天内结束,以失败处理";

    @Override
    public String execute(String param) {

        logger.info("start dtss.StatusCorrectJob, param :" + param);
        String result = null;
        boolean isLocked = false;
        try {
            if (!(isLocked = redisComponent.acquireLock(REDIS_LOCK_KEY, Millisecond.TEN_MINUS))) {
                result = "请求并发";

            } else if (StringUtils.isNotBlank(param)) {
                result = "暂不支持参数模式";

            } else {
                result = correctStatus();
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            result = "处理异常:" + e.getMessage();
        } finally {
            if (isLocked) {
                redisComponent.releaseLock(REDIS_LOCK_KEY);
            }
            logger.info("end dtss.StatusCorrectJob, param :" + param + ", result :" + result);
        }

        return result;
    }

    private String correctStatus() {

        int limit = 500;
        JobExecutiveLogQuery query = new JobExecutiveLogQuery();
        query.setLimit(limit);
        query.setOffset(0);
        query.setStatus(JobExeStatusEnum.PROCESSING.getCode());
        Date startTime = getLastCorrectDate();
        query.setOrderBy(new OrderBy("create_time", OrderBy.DESC));

        Counter counter = new Counter();
        while (true) {
            query.setEltCreateTime(startTime);
            List<JobExecutiveLog> logList = jobExecutiveLogService.list(query);
            if (CollectionUtils.isEmpty(logList)) {
                break;
            }

            for (JobExecutiveLog log : logList) {
                counter.allPlus();
                counter.successPlus();
                jobLogService.innerFail(log.getId(), JOB_MSG);
            }

            if (limit > logList.size()) {
                break;
            }

            startTime = logList.get(logList.size() - 1).getCreateTime();
        }

        return counter.toString();
    }

    private Date getLastCorrectDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTime();
    }
}
