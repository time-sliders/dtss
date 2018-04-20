package com.dtss.server.core.job.sender;

import com.dtss.client.cmp.AbstractAdapter;
import com.dtss.client.enums.JobTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 分发器
 *
 * @author luyun
 * @since 2018.01.27 10:36
 */
@Component
public class SendDispatcher extends AbstractAdapter<Integer, JobSender> {

    @Override
    public void afterPropertiesSet() throws Exception {
        registerExecutor(JobTypeEnum.COMMON.getCode(),"randomJobSender");
        registerExecutor(JobTypeEnum.ABSOLUTELY_SINGLE_POINT.getCode(),"absolutelySinglePointJobSender");
        registerExecutor(JobTypeEnum.PRIOR_SINGLE_POINT.getCode(),"priorSinglePointJobSender");
        super.afterPropertiesSet();
    }
}
