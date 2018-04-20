package com.dtss.web.converter;

import com.dtss.client.enums.JobExeStatusEnum;
import com.dtss.client.model.JobExecutiveLog;
import com.dtss.commons.AbstractObjectConverter;
import com.dtss.commons.DateUtil;
import com.dtss.web.util.CostTimeShowUtil;
import com.dtss.web.vo.JobLogListVO;
import org.springframework.stereotype.Component;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.22
 */
@Component
public class JobLogListVOConverter extends AbstractObjectConverter<JobExecutiveLog, JobLogListVO> {
    @Override
    protected JobLogListVO onBuildDto(JobExecutiveLog model) {
        JobLogListVO vo = new JobLogListVO();

        vo.setId(model.getId());
        vo.setJobId(model.getJobId());

        vo.setName(model.getName());
        vo.setApp(model.getApp());
        vo.setScheduleTime(String.valueOf(model.getScheduleTime()));
        vo.setTriggerServerIp(model.getTriggerServerIp());
        vo.setExecuteClientIp(model.getExecuteClientIp());
        vo.setStartTime(DateUtil.format(model.getStartTime(), DateUtil.DEFAULT_FORMAT));
        vo.setCost(CostTimeShowUtil.format(model.getStartTime(),model.getFinishTime()));
        JobExeStatusEnum statusEnum = JobExeStatusEnum.getByCode(model.getStatus());
        vo.setStatus(statusEnum.getCode());
        vo.setStatusDesc(statusEnum.getName());
        vo.setStatusColor(statusEnum.getColor());
        return vo;
    }

    @Override
    protected JobExecutiveLog onBuildModel(JobLogListVO domain) {
        return null;
    }
}
