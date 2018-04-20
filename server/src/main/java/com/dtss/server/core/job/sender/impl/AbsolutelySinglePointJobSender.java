package com.dtss.server.core.job.sender.impl;

import com.dtss.client.model.JobConfig;
import com.dtss.client.model.JobExecutiveLog;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 绝对单点任务分发器
 *
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.01.29
 */
@Component
public class AbsolutelySinglePointJobSender extends AbstractJobSender {

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
            jobLogService.innerFail(log.getId(),
                    "未配置单点");
            return null;
        }

        String[] ipArr = clientIps.split("\\|");
        for (String ip : ipArr) {
            if (availableClientIpList.contains(ip)) {
                return ip;
            }
        }

        jobLogService.innerFail(log.getId(),
                "指定的单点并未存活,任务执行失败");
        return null;
    }
}