package com.dtss.web.converter;


import com.dtss.client.enums.JobTriggerMode;
import com.dtss.client.enums.JobTypeEnum;
import com.dtss.client.model.JobConfig;
import com.dtss.commons.AbstractObjectConverter;
import com.dtss.web.vo.JobConfigVO;
import org.springframework.stereotype.Component;

/**
 * @author luyun
 * @since 2018.01.25 23:28
 */
@Component
public class JobConfigVOConverter extends AbstractObjectConverter<JobConfigVO, JobConfig> {

    @Override
    protected JobConfig onBuildDto(JobConfigVO model) {
        throw new RuntimeException("not support method");
    }

    @Override
    protected JobConfigVO onBuildModel(JobConfig domain) {
        JobConfigVO model = new JobConfigVO();
        model.setId(domain.getId());
        model.setApp(domain.getApp());
        model.setName(domain.getName());
        model.setActivity(domain.isActivity());
        model.setJobBeanName(domain.getJobBeanName());
        model.setType(JobTypeEnum.getNameByCode(domain.getType()));
        model.setCorn(domain.getCron());
        model.setTriggerMode(JobTriggerMode.getNameByCode(domain.getTriggerMode()));
        return model;
    }
}
