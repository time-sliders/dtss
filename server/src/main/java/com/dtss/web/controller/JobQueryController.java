package com.dtss.web.controller;

import com.dtss.client.model.JobConfig;
import com.dtss.client.model.query.JobConfigQuery;
import com.dtss.server.service.JobQueryService;
import com.dtss.web.converter.JobConfigVOConverter;
import com.dtss.web.vo.BaseQuery;
import com.dtss.web.vo.JobListAO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author luyun
 * @since 2018.01.23 23:20
 */
@Controller
@RequestMapping("/job")
public class JobQueryController extends BaseController {

    @Autowired
    private JobQueryService jobQueryService;

    @Autowired
    private JobConfigVOConverter jobConfigVOConverter;

    @RequestMapping("/list")
    public String selectList(JobListAO so, BaseQuery query, Model model) {

        JobConfigQuery configQuery = buildQuery(so, query);

        List<JobConfig> list = jobQueryService.query(configQuery);
        Integer count = jobQueryService.count(configQuery);
        query.doPage(count);

        model.addAttribute("data", jobConfigVOConverter.asModelList(list));
        model.addAttribute("query", query);
        model.addAttribute("ao", so);

        return "jobInfo/list";
    }

    @RequestMapping("/view")
    public String view(@RequestParam Long id, Model model) {
        JobConfig jobConfig = jobQueryService.findById(id);
        model.addAttribute("data", jobConfig);
        return "jobInfo/view";
    }

    private JobConfigQuery buildQuery(JobListAO ao, BaseQuery baseQuery) {

        JobConfigQuery query = new JobConfigQuery();
        if(StringUtils.isNotBlank(ao.getApp())){
            query.setApp(ao.getApp());
        }
        if(StringUtils.isNotBlank(ao.getJobBeanName())){
            query.setJobBeanName(ao.getJobBeanName());
        }
        if(StringUtils.isNotBlank(ao.getName())){
            query.setName(ao.getName());
        }

        query.setTriggerMode(ao.getTriggerMode());
        query.setType(ao.getType());

        Integer page = baseQuery.getPageNo();
        if (page == null || page <= 0) {
            page = 1;
        }
        Integer rows = baseQuery.getPageSize();
        if (rows == null || rows <= 0 || rows > 100) {
            rows = 20;
        }

        query.setOffset((page - 1) * rows);
        query.setLimit(rows);
        return query;
    }
}
