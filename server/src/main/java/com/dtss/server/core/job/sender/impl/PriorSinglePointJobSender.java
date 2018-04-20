package com.dtss.server.core.job.sender.impl;

import com.dtss.client.model.JobConfig;
import com.dtss.client.model.JobExecutiveLog;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * [单点优先]job发送器
 *
 * @author luyun
 * @since 2018.01.29 22:08
 */
@Component
public class PriorSinglePointJobSender extends AbstractJobSender {

    @Override
    protected String getClientIp(JobConfig jobConfig, JobExecutiveLog log) {

        List<String> availableClientIpList = clientSystemNodeWatcher.getAllClientList(jobConfig.getApp());
        if (CollectionUtils.isEmpty(availableClientIpList)) {
            jobLogService.innerFail(log.getId(),
                    "没有存活的客户端,任务执行失败");
            return null;
        }

        String clientIps = jobConfig.getClientIp();
        if (StringUtils.isBlank(clientIps)) {
            return clientSystemNodeWatcher.getClientRandomly(jobConfig.getApp());
        }

        String[] ipArr = clientIps.split("\\|");
        for (String ip : ipArr) {
            if (availableClientIpList.contains(ip)) {
                return ip;
            }
        }

        String ip = clientSystemNodeWatcher.getClientRandomly(jobConfig.getApp());
        jobLogService.addInnerMsg(log.getId(),
                "所有优先执行的单点均未存活,以存活的["+ip+"]来执行任务");
        return ip;
    }
}
