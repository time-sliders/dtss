package com.dtss.web.controller;

import com.dtss.client.enums.JobExeStatusEnum;
import com.dtss.client.model.JobConfig;
import com.dtss.client.model.JobExecutiveLog;
import com.dtss.server.core.job.ServerJobExecutor;
import com.dtss.server.core.job.model.JobStopResult;
import com.dtss.server.core.job.sender.stop.JobStopExecutor;
import com.dtss.server.service.JobExecutiveLogService;
import com.dtss.server.service.JobQueryService;
import com.dtss.web.consts.OptStatus;
import com.dtss.web.converter.JobLogDetailVOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author luyun
 */
@Controller
@RequestMapping("/job")
public class JobExecuteController extends BaseController implements OptStatus {

    @Autowired
    private JobQueryService jobQueryService;

    @Autowired
    private JobExecutiveLogService logQueryService;

    @Autowired
    private JobLogDetailVOConverter jobLogDetailVOConverter;

    @Autowired
    private JobExecutiveLogService jobExecutiveLogService;

    @Autowired
    private ServerJobExecutor jobExecutor;

    @Autowired
    private JobStopExecutor jobStopExecutor;

    @RequestMapping(value = "/execute", method = RequestMethod.POST)
    public String execute(@RequestParam Long id, Model model) {
        JobConfig jobConfig = jobQueryService.findById(id);
        JobExecutiveLog log = jobExecutor.executeJobWithoutLeaderCheck(jobConfig);
        model.addAttribute("data", jobLogDetailVOConverter.toDto(logQueryService.findById(log.getId())));
        return "jobInfo/log_detail";
    }

    @RequestMapping(value = "/stopExe", method = {RequestMethod.POST, RequestMethod.GET})
    public String stopExe(@RequestParam Long id, Model model) {
        JobExecutiveLog log = jobExecutiveLogService.findById(id);
        model.addAttribute("data", jobLogDetailVOConverter.toDto(logQueryService.findById(log.getId())));
        if (log.getStatus().equals(JobExeStatusEnum.PROCESSING.getCode())) {
            JobStopResult stopResult = jobStopExecutor.stop(log);
            if (stopResult.isSuccess()) {
                model.addAttribute("status", STATUS_SUCCESS);
                model.addAttribute("successMsg", stopResult.getMsg());
            } else {
                model.addAttribute("status", STATUS_FAILURE);
                model.addAttribute("errorMsg", stopResult.getMsg());
            }
        } else {
            model.addAttribute("status", STATUS_FAILURE);
            model.addAttribute("errorMsg", "非处理中的任务，不需要中止");
        }

        return "jobInfo/log_detail";
    }

}
