package com.dtss.web.controller;

import com.dtss.client.model.JobExecutiveLog;
import com.dtss.client.model.query.JobExecutiveLogQuery;
import com.dtss.commons.OrderBy;
import com.dtss.server.service.JobExecutiveLogService;
import com.dtss.web.converter.JobLogDetailVOConverter;
import com.dtss.web.converter.JobLogListVOConverter;
import com.dtss.web.vo.BaseQuery;
import com.dtss.web.vo.JobLogListAO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * job日志组件
 *
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.22
 */
@Controller
@RequestMapping("/job")
public class JobLogController extends BaseController {

    @Autowired
    private JobExecutiveLogService jobExecutiveLogService;

    @Autowired
    private JobLogListVOConverter logListVOConverter;

    @Autowired
    private JobLogDetailVOConverter jobLogDetailVOConverter;

    @RequestMapping(value = "/queryJobLog", method = {RequestMethod.POST, RequestMethod.GET})
    public String queryLogList(JobLogListAO so, BaseQuery query, Model model) {
        JobExecutiveLogQuery dbQuery = buildQuery(so, query);
        List<JobExecutiveLog> logList = jobExecutiveLogService.list(dbQuery);

        Integer count = jobExecutiveLogService.count(dbQuery);
        query.doPage(count);

        model.addAttribute("data", logListVOConverter.asDtoList(logList));
        model.addAttribute("query", query);
        model.addAttribute("ao", so);
        return "jobInfo/log";
    }

    @RequestMapping("/logDetail")
    public String logDetail(@RequestParam Long id, Model model) {
        JobExecutiveLog log = jobExecutiveLogService.findById(id);
        model.addAttribute("data", jobLogDetailVOConverter.toDto(log));
        return "jobInfo/log_detail";
    }

    private JobExecutiveLogQuery buildQuery(JobLogListAO ao, BaseQuery baseQuery) {

        JobExecutiveLogQuery query = new JobExecutiveLogQuery();
        if (StringUtils.isNotBlank(ao.getApp())) {
            query.setApp(ao.getApp());
        }
        if (StringUtils.isNotBlank(ao.getJobBeanName())) {
            query.setJobBeanName(ao.getJobBeanName());
        }
        if (StringUtils.isNotBlank(ao.getName())) {
            query.setName(ao.getName());
        }

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
        query.setOrderBy(new OrderBy("id", OrderBy.DESC));
        return query;
    }

}
