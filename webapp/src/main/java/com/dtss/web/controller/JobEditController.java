package com.dtss.web.controller;

import com.dtss.client.model.JobConfig;
import com.dtss.commons.DateUtil;
import com.dtss.commons.Result;
import com.dtss.web.consts.OptStatus;
import com.dtss.server.core.job.ServerJobExecutor;
import com.dtss.server.service.JobModifyService;
import com.dtss.server.service.JobQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;


/**
 * @author luyun
 * @since 2018.01.27 00:58
 */
@Controller
@RequestMapping("/job")
public class JobEditController extends BaseController implements OptStatus {

    @Autowired
    private JobModifyService jobModifyService;

    @Autowired
    private JobQueryService jobQueryService;

    @Autowired
    private ServerJobExecutor jobExecutor;

    @RequestMapping("/toedit")
    public String toEdit(@RequestParam String id, Model model) {
        JobConfig jobConfig = jobQueryService.findById(id);
        model.addAttribute("data", jobConfig);
        return "jobInfo/edit";
    }

    @RequestMapping("/edit")
    public String edit(HttpServletRequest req, JobConfig jobConfig, Model model) {
        if (jobConfig.isActivity() == null) {
            jobConfig.setActivity(false);
        }

        Result<Void> result = jobModifyService.updateById(jobConfig);
        if (result.isSuccess()) {
            JobConfig newData = jobQueryService.findById(jobConfig.getId());
            model.addAttribute("data", newData);
            model.addAttribute("status", STATUS_SUCCESS);
            model.addAttribute("successMsg", "最新更新时间:" + DateUtil.format(newData.getModifyTime(), DateUtil.DEFAULT_FORMAT));
        } else {
            model.addAttribute("status", STATUS_FAILURE);
            model.addAttribute("errorMsg", result.getErrorMsg());
        }
        return "jobInfo/edit";
    }


    @RequestMapping("/editAndExe")
    public String editAndExe(JobConfig jobConfig, Model model) {
        if (jobConfig.isActivity() == null) {
            jobConfig.setActivity(false);
        }

        Result<Void> result = jobModifyService.updateById(jobConfig);
        if (result.isSuccess()) {
            JobConfig newData = jobQueryService.findById(jobConfig.getId());
            jobExecutor.executeJobWithoutLeaderCheck(newData);
            model.addAttribute("data", newData);
            model.addAttribute("status", STATUS_SUCCESS);
            model.addAttribute("successMsg", "已发送任务执行,任务最新更新时间:"
                    + DateUtil.format(newData.getModifyTime(), DateUtil.DEFAULT_FORMAT));
        } else {
            model.addAttribute("status", STATUS_FAILURE);
            model.addAttribute("errorMsg", result.getErrorMsg());
        }
        return "jobInfo/edit";
    }

    @RequestMapping("/editAndTest")
    public String editAndTest(JobConfig jobConfig, Model model) {
        if (jobConfig.isActivity() == null) {
            jobConfig.setActivity(false);
        }

        Result<Void> result = jobModifyService.updateById(jobConfig);
        if (result.isSuccess()) {
            JobConfig newData = jobQueryService.findById(jobConfig.getId());
            jobExecutor.testJob(newData);
            model.addAttribute("data", newData);
            model.addAttribute("status", STATUS_SUCCESS);
            model.addAttribute("successMsg", "已发送测试执行,任务最新更新时间:"
                    + DateUtil.format(newData.getModifyTime(), DateUtil.DEFAULT_FORMAT));
        } else {
            model.addAttribute("status", STATUS_FAILURE);
            model.addAttribute("errorMsg", result.getErrorMsg());
        }
        return "jobInfo/edit";
    }

}
