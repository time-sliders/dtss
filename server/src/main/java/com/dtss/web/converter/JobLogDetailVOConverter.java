package com.dtss.web.converter;

import com.dtss.client.enums.JobExeStatusEnum;
import com.dtss.client.model.JobExecutiveLog;
import com.dtss.commons.AbstractObjectConverter;
import com.dtss.commons.DateUtil;
import com.dtss.web.vo.JobLogDetailVO;
import org.springframework.stereotype.Component;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.23
 */
@Component
public class JobLogDetailVOConverter extends AbstractObjectConverter<JobExecutiveLog, JobLogDetailVO> {

    @Override
    protected JobLogDetailVO onBuildDto(JobExecutiveLog model) {

        JobLogDetailVO vo = new JobLogDetailVO();
        vo.setId(model.getId());
        vo.setApp(model.getApp());
        vo.setTriggerServerIp(model.getTriggerServerIp());
        vo.setExecuteClientIp(model.getExecuteClientIp());
        vo.setJobBeanName(model.getJobBeanName());
        if (model.getFinishTime() != null) {
            vo.setFinishTime(DateUtil.format(model.getFinishTime(), DateUtil.DEFAULT_FORMAT));
        }
        vo.setStartTime(DateUtil.format(model.getStartTime(), DateUtil.DEFAULT_FORMAT));
        vo.setInnerMsg(model.getInnerMsg());
        vo.setJobExeResult(model.getJobExeResult());
        vo.setJobId(model.getJobId());
        vo.setParam(model.getParam());
        vo.setSliceIndex(model.getSliceIndex());
        vo.setName(model.getName());
        JobExeStatusEnum statusEnum = JobExeStatusEnum.getByCode(model.getStatus());
        vo.setStatus(statusEnum.getName());
        vo.setStatusColor(statusEnum.getColor());

        return vo;
    }

    @Override
    protected JobExecutiveLog onBuildModel(JobLogDetailVO domain) {
        return null;
    }
}
